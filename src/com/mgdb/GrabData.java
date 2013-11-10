package com.mgdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.JSONObject;
import org.sqlite.SQLiteJDBCLoader;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.mgdb.datatypes.Person;
import com.mgdb.datatypes.PersonFunnel;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class GrabData {
    static String getUrl = "http://genealogy.math.ndsu.nodak.edu/id.php?id=";
    //static String file = "/Users/hzhao/work/git/math/MGDB/output/mgdb_info";
    static String file = "/home/taojiang/work/git/MGDB/output/mgdb_info";
    static String notExistStr = "You have specified an ID that does not exist in the database. Please back up and try again.";
    
    public static void main(String[] args) throws IOException {
		if (args != null && args.length > 1) {
			int start = Integer.parseInt(args[0]);
			int end = Integer.parseInt(args[1]);
			String suffix = start + "_" + end;
			long currentTime = System.currentTimeMillis();
			for (int i = start; i < end; i++) {
				ArrayList<String> pageInfo = new ArrayList<String>();
				doGet(getUrl, i, pageInfo);
				if(pageInfo.size() != 0) {
				    if (pageInfo.get(0).contains(notExistStr)) {
				        System.out.println("The ID " + i + " doesn't exist on the website."); 
				    } else {
				        Person person = new Person(i);
				        person.parseInfo(pageInfo);   
				        System.out.println(person.toString());
				        if(person.getName() != null && person.getName().length() > 0) {
				            try {
				                writeFile(file + "_" + suffix + ".txt", person.toJSON());
				            } catch (Exception ex) {
				                ex.printStackTrace();
				            }
				        } else {
				            System.out.println("Empty username for ID " + i);
				        }
				    }
				} else {
				    System.out.println("Failed to parse pageInfo for ID : " + i);
				}
			}
			System.out.println("Total cost : "+ (System.currentTimeMillis() - currentTime));
        } else {
            System.out.println("2 or more parameters is required");
        }
        //recursiveAncestors(person);
        //recursiveDecendents(person);
        //BloomFilter<Person> friends = BloomFilter.create(new PersonFunnel(), 200000, 0.000001);
        //insertPerson(friends);    
        //testBloomFilter(friends);
    }
    
    public static void writeFile(String file, JSONObject object) throws Exception{
        //FileWriter fstream = new FileWriter(file, true);
        FileWriterWithEncoding fstream = new FileWriterWithEncoding(file, "UTF-8", true);
        //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(object.toString());
        out.newLine();
        out.close();
    }
    
    public static void insertPerson(BloomFilter<Person> friends) {
        ODatabaseDocumentTx database = new ODatabaseDocumentTx("remote:localhost/mgdb").open("admin","admin");
        System.out.println(String.format("running in %s mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java"));
        Connection conn = null;
        try {
          Class.forName("org.sqlite.JDBC");
          String db = "/Users/hzhao/work/svn/math-genealogy-db/MGDB";//TODO : hardcoding
          conn = DriverManager.getConnection("jdbc:sqlite:" + db);
          Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery( "SELECT * FROM PERSON" );
          while ( rs.next() ) {
              int spid = -1;
              String sname = "";
              int onlineDescendants = -1;
              try {
                  spid = Integer.parseInt(rs.getString("pID"));
                  sname = rs.getString("name");
                  onlineDescendants = Integer.parseInt(rs.getString("onlineDescendants"));
              }catch(Exception ex) {
                  System.out.println(ex.getMessage());
              }
              System.out.println("pid : " + spid + " name " + sname);
              
              Person p = new Person(spid);
              friends.put(p);
              
              try {
                  ODocument doc = new ODocument("person");
                  doc.field("pID", spid);
                  doc.field("name", sname);
                  doc.field("onlineDescendants", onlineDescendants);
                  doc.save();
              }catch(Exception ex) {
                  System.out.println(ex.getMessage());
              }
          }
          rs.close();
          stmt.close();
          if(conn != null)
              conn.close();
        } catch ( Exception e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          System.exit(0);
        } finally {
            database.close();
        }
    }
    
    public static void testBloomFilter(BloomFilter<Person> friends) {
        for(int i = 136155; i < 150000; i++) {
            Person p = new Person(i);
            if(!friends.mightContain(p)) {
                System.out.print(i + " ");
            }
        }  
    }

    public static void doGet(String urlString, int values, List<String> pageInfo) {
        HttpURLConnection connection = null;
        try {
            String getURL = urlString + values;
            URL getUrl = new URL(getURL);
            connection = (HttpURLConnection) getUrl.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String lines;
            while ((lines = reader.readLine()) != null){
                String each = new String(lines.getBytes(), "UTF-8");
                pageInfo.add(each);
            }
            reader.close();
        }catch(Exception ex) {
            ex.printStackTrace();
        }finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
    
    public static void recursiveAncestors(Person person) throws IOException {
        List<Integer> advisorIds = person.getAdvisorsIDs();
        Queue<Integer> advisorsQueue = new LinkedBlockingQueue<Integer>();
        Map<Integer, Person> advisorMap = new HashMap<Integer, Person>();
        advisorMap.put(person.getID(), person);
        for(int advisor : advisorIds) {
            Person each = new Person(advisor);
            List<String> lines = new ArrayList<String>();
            doGet(getUrl, advisor, lines);
            each.parseInfo(lines);
            System.out.println(each.toString());
            advisorMap.put(each.getID(), each);
            List<Integer> ids = each.getAdvisorsIDs();
            if(ids != null && ids.size() > 0) {
                for(int i : ids) {
                    advisorsQueue.offer(i);
                }
            }
        }
        recursiveAncestorsHelper(advisorMap, advisorsQueue);
    }
    
    private static void recursiveAncestorsHelper(Map<Integer, Person> persons, Queue<Integer> queue) throws IOException {
        while(!queue.isEmpty()) {
            int id = queue.poll();      
            if(!persons.containsKey(id)) {
                Person each = new Person(id);
                List<String> lines = new ArrayList<String>();
                doGet(getUrl, id, lines);
                each.parseInfo(lines);
                persons.put(each.getID(), each);
                System.out.println(each.toString());
                List<Integer> ids = each.getAdvisorsIDs();
                if(ids != null && ids.size() > 0) {
                    for(int i : ids) {
                        queue.offer(i);
                    }
                }   
            }
        }
    }
    
    public static void recursiveDecendents(Person person) throws IOException {
        List<Integer> studentsIds = person.getStudentIDs();
        Queue<Integer> studentsQueue = new LinkedBlockingQueue<Integer>();
        Map<Integer, Person> studentsMap = new HashMap<Integer, Person>();
        studentsMap.put(person.getID(), person);
        for(int studentId : studentsIds) {
            Person each = new Person(studentId);
            List<String> lines = new ArrayList<String>();
            doGet(getUrl, studentId, lines);
            each.parseInfo(lines);
            System.out.println(each.toString());
            studentsMap.put(each.getID(), each);
            List<Integer> ids = each.getStudentIDs();
            if(ids != null && ids.size() > 0) {
                for(int i : ids) {
                    studentsQueue.offer(i);
                }
            }
        }
        recursiveDecendentsHelper(studentsMap, studentsQueue);
    }
    
    private static void recursiveDecendentsHelper(Map<Integer, Person> persons, Queue<Integer> queue) throws IOException {
        while(!queue.isEmpty()) {
            int id = queue.poll();      
            if(!persons.containsKey(id)) {
                Person each = new Person(id);
                List<String> lines = new ArrayList<String>();
                doGet(getUrl, id, lines);
                each.parseInfo(lines);
                persons.put(each.getID(), each);
                System.out.println(each.toString());
                List<Integer> ids = each.getStudentIDs();
                if(ids != null && ids.size() > 0) {
                    for(int i : ids) {
                        queue.offer(i);
                    }
                }   
            }
        }
    }
}
