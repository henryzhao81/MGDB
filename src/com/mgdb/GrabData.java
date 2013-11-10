package com.mgdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
				// ArrayList<String> info = extractInfo(pageInfo);
				if (pageInfo.get(0).contains(notExistStr)) {
					System.out.println("The ID " + i + " doesn't exist on the website."); 
				} else {
					Person person = new Person(i);
					person.parseInfo(pageInfo);
					System.out.println(person.toString());
					try {
						writeFile(file + "_" + suffix + ".json",
								person.toJSON());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			System.out.println("Total cost : "
					+ (System.currentTimeMillis() - currentTime));
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
        FileWriter fstream = new FileWriter(file, true);
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

    public static void doGet(String urlString, int values, List<String> pageInfo) throws IOException{
        String getURL = urlString + values;
        URL getUrl = new URL(getURL);
        HttpURLConnection connection = (HttpURLConnection) getUrl
                .openConnection();
        connection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        String lines;
        while ((lines = reader.readLine()) != null){
            pageInfo.add(lines);
        }
        reader.close();

        connection.disconnect();
    }
    
//    public static void doPost(String urlString, String values) throws IOException{       
//        URL postUrl = new URL(urlString);
//
//        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
//
//        connection.setDoOutput(true);
//        connection.setDoInput(true);
//        connection.setRequestMethod("POST");
//        connection.setUseCaches(false);
//        connection.setInstanceFollowRedirects(true);
//        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//        connection.connect();        
//        
//        DataOutputStream out = new DataOutputStream(connection
//                .getOutputStream());
//        out.writeBytes(values); 
//        out.flush();
//        out.close(); // flush and close
//        
//        int responseCode = connection.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + urlString);
//        System.out.println("Post parameters : " + values);
//        System.out.println("Response Code : " + responseCode);
//        
//        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
//        String line="";
//        while ((line = reader.readLine()) != null){
//            System.out.println(line);
//        }
//        reader.close();
//        connection.disconnect();
//    }
    
//    public static ArrayList<String> extractInfo(ArrayList<String> pageInfo) throws UnsupportedEncodingException {
//        String name = "", institution="", year="", dissertation="", advisors="", students="", studNo="0";
//        ArrayList<String> info = new ArrayList<String>();
//        
//        int i=0;
//        int size = pageInfo.size();
//        
//        while((i<size) && (!pageInfo.get(i).contains("h2 style=")) ){
//            i++;
//        }
//        
//        while(i<size) {
//            String line = pageInfo.get(i);
//            
//            if (line.contains("h2 style=")) {
//                i +=1;
//                line = pageInfo.get(i);
//                name = line.split("</h2>")[0];
//            }
//            
//            //Get year and university
//            if (line.contains("#006633; margin-left: 0.5em\">")){
//                String inst_year = line.split("#006633; margin-left: 0.5em\">")[1];
//                institution += inst_year.split("</span>")[0]+ ";";
//                year += inst_year.split("</span>")[1].trim() + ";";
//
///*              if len(self.institution[len(self.institution)-1]) == 0:
//                    self.institution[len(self.institution)-1] = None
//                if len(self.year[len(self.year)-1]) == 0:
//                    self.year[len(self.year)-1] = None*/
//            }
//            
//            //Get dissertation title
//            if (line.contains("thesisTitle")) {
//                i += 2;
//                line = pageInfo.get(i);
//                if (line.split("</span></div>").length != 0)
//                   dissertation += line.split("</span></div>")[0] + ";";
///*              if len(self.dissertation[len(self.dissertation)-1]) == 0:
//                    self.dissertation[len(self.dissertation)-1] = None*/
//            }
//            
//            // Get all advisors
//            if (line.contains("<p style=\"text-align: center; line-height: 2.75ex\">")) {
//                int len = line.split("a href=\"id.php\\?id=").length;
//                String[] advisor_id = line.split("a href=\"id.php\\?id=");
//                
//                int j=1; 
//                while(j<len) {
//                    advisors += advisor_id[j].split("\">")[0] + ";";
//                    j++;
//                }
//            }
//            
//            //Get students
//            if (line.contains("<td><a href=\"id.php?id=")) {
//                students +=line.split("a href=\"id.php\\?id=")[1].split("\">")[0]+";";              
//                //students += line.split("a href=\"id.php?")[1].split("id=")[1].split("\">")[0]+";";
//            }
//            
//            // Get number of descendants
//            // Uses only '</a> and ' as search string and not '>students</a> and '
//            // because 'students' can change to 'student' !
//            if (line.contains("According to our current on-line database")) 
//                studNo = line.split("</a> and ")[1].split("<a href=")[0];
//
//            if (line.contains("No students known."))
//                studNo = "0";
//
//            if (line.contains("If you have additional information or"))
//                break;
//            
//            i++;
//        }   
//        info.add(name);
//        info.add(institution);
//        info.add(year);
//        info.add(dissertation);
//        info.add(advisors);
//        info.add(students);
//        info.add(studNo);
//        
//        return info;
//    }
    
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