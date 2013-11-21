package com.mgdb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mgdb.database.DatabaseConnection;

public class Importer {
    static String file = "/home/taojiang/work/git/MGDB/output/mgdb_info";
    public Importer() {}

    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 1) {
            String suffix = args[0];
            int batch = Integer.parseInt(args[1]);
            Importer impo = new Importer();
            impo.constructTables();
            impo.readPersonFromFile(file + "_" + suffix + ".txt", batch);
            impo.readDissertationFromFile(file + "_" + suffix + ".txt", batch);
        }else {
            System.out.println("2 or more parameters is required");
        }
    }

    public void readPersonFromFile(String file, int batch) throws Exception {
        InputStreamReader instream = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader in = new BufferedReader(instream);
        String strLine;
        List<JSONObject> objList = new ArrayList<JSONObject>(batch);
        int index = 0;
        while ((strLine = in.readLine()) != null) {
            if(index == batch) {
                this.importPerson(objList);
                index = 0;
                objList.clear();
            }
            if(strLine != null && strLine.length() > 0) {
                JSONObject object = null;
                try {
                    object = new JSONObject(strLine);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                objList.add(object);
            } else {
                System.out.println("Failed to pasre line");
            }
            index++;
        }
        if(objList.size() > 0) {
            this.importPerson(objList);
        }
        in.close();
        instream.close();
    }
    
    public void readDissertationFromFile(String file, int batch) throws Exception {
        InputStreamReader instream = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader in = new BufferedReader(instream);
        String strLine;
        List<JSONObject> objList = new ArrayList<JSONObject>(batch);
        int index = 0;
        while ((strLine = in.readLine()) != null) {
            if(index == batch) {
                this.importDissertation(objList);
                index = 0;
                objList.clear();
            }
            if(strLine != null && strLine.length() > 0) {
                JSONObject object = null;
                try {
                    object = new JSONObject(strLine);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                objList.add(object);
            } else {
                System.out.println("Failed to pasre line");
            }
            index++;
        }
        if(objList.size() > 0) {
            this.importDissertation(objList);
        }
        in.close();
        instream.close();
    }

    private void importPerson(List<JSONObject> list) {
      //  ODatabaseDocumentTx database = new ODatabaseDocumentTx("remote:localhost/mgdb").open("admin","admin");
		DatabaseConnection db = new DatabaseConnection();
    	Connection conn = db.getConnection();

		try {
			for (JSONObject obj : list) {
				System.out.println(obj.toString());
				constructPerson(obj, conn);
			}
			conn.close();
			System.out.println("===== person batch finish ====");
			// database.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
    }
    
    private void importDissertation(List<JSONObject> list) {
		// ODatabaseDocumentTx database = new
		// ODatabaseDocumentTx("remote:localhost/mgdb").open("admin","admin");
    	DatabaseConnection db = new DatabaseConnection();
    	Connection conn = db.getConnection();

		try {
			for (JSONObject obj : list) {
				System.out.println(obj.toString());
				constructDissertation(obj, conn);
			}
			conn.close();
			System.out.println("===== dissertation batch finish ====");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
    }
    
/*    private void constructPerson(JSONObject object) {
        ODocument doc = new ODocument("person");
        int pid = -1;
        String name = "NULL";
        int students = 0;
        try {
            pid = object.getInt("id");
            name = object.getString("name");
            JSONArray array = object.getJSONArray("students");
            if(array != null)
                students = array.length();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        doc.field("pID", pid);
        doc.field("name", name);   
        doc.field("onlineDescendants", students);
        doc.save();
    }*/
    
	private void constructPerson(JSONObject object, Connection conn) {
/*		Connection conn = null;*/
		PreparedStatement prep = null;
		int pid = -1;
		String name = "NULL";
		int students = 0;
		try {
			pid = object.getInt("id");
			name = object.getString("name");
			students = object.getInt("studentsNum");

			prep = conn.prepareStatement("insert into person values(?,?,?);");
			prep.setInt(1, pid);
			prep.setString(2, name);
			prep.setInt(3, students);
			prep.execute();
			prep.close();
		} catch(Exception ex) {
            ex.printStackTrace();
        }
		System.out.println("Records created successfully");
	}
    
    /*private void constructDissertation(JSONObject object) {
        JSONArray dissertArr = null;
        try {
            dissertArr = object.getJSONArray("disserations");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        if(dissertArr != null && dissertArr.length() > 0) {
            try {
                for(int i = 0; i < dissertArr.length(); i++) {
                    String str = dissertArr.getString(i);
                    ODocument doc = new ODocument("dissertation");
                    doc.field("title", str);
                    int pid = object.getInt("id");                   
                    String strQuery = "select from person where pID = '" + pid + "'";
                    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(strQuery);
                    List<ODocument> results = ODatabaseRecordThreadLocal.INSTANCE.get().command(query).execute();
                    if(results != null && results.size() > 0) {
                        doc.field("author", results.get(0));
                    }
                    JSONArray uniArr = object.getJSONArray("institutions");
                    if(uniArr != null && uniArr.length() > 0) {
                        //doc.field("university", uniArr.toString());
                        StringBuffer uniBuffer = new StringBuffer();
                        for(int j = 0; j < uniArr.length(); j++) {
                            uniBuffer.append(uniArr.getString(j));
                        }
                        doc.field("university", uniBuffer.toString());
                    }
                    JSONArray years = object.getJSONArray("gradYears");
                    if(years != null && years.length() > 0) {
                        StringBuffer ybuf = new StringBuffer();
                        for(int j = 0; j < years.length(); j++) {
                            JSONObject each = years.getJSONObject(j);
                            int yearStr = each.getInt("year");
                            ybuf.append(yearStr);
                        }
                        doc.field("year", ybuf.toString());
                    }
                    doc.save();
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }*/
	
	private void constructDissertation(JSONObject object, Connection conn) {
		PreparedStatement prep = null;	
        JSONArray dissertArr = null;
        try {
            dissertArr = object.getJSONArray("Dissertation Info");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        if(dissertArr != null && dissertArr.length() > 0) {
            try {
                for(int i = 0; i < dissertArr.length(); i++) {                	
                	JSONObject each = dissertArr.getJSONObject(i);
                    String title = each.getString("dissertation");
                    int author = object.getInt("id");            
                    String university = each.getString("institution");
                    String year = each.getString("year");
                    
                    prep = conn.prepareStatement("insert into dissertation (author, title, university, year) values(?,?,?,?) ;", Statement.RETURN_GENERATED_KEYS);
        			prep.setInt(1, author);
        			prep.setString(2, title);
        			prep.setString(3, university);
        			prep.setString(4, year);
        			prep.execute();
        			prep.close();
        			
        			int student = 0;
        			ResultSet rs = conn.prepareStatement("SELECT currval('dissertation_did_seq');").executeQuery();
        			if (rs.next()) {
        				student = rs.getInt(1);
        			}
        			
        			String advisors = each.getString("advisors");
        			if (!advisors.equals("")) {
        			    String[] tmp = advisors.split(","); 
        			    int len = tmp.length;
        			
        				int advOrder = 1;
                        for(int j = 0; j < len; j++) {
                            int advId = Integer.valueOf(tmp[j]);
                            prep = conn.prepareStatement("insert into advised values(?,?,?);");
                            prep.setInt(1, student);
                			prep.setInt(2, advOrder);
                			prep.setInt(3, advId);
                			prep.execute();
                			prep.close();
                            advOrder += 1;
                        }
                 
        			}
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
	public void constructTables() {
		DatabaseConnection db = new DatabaseConnection();
    	Connection conn = db.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = " CREATE TABLE IF NOT EXISTS person "
					+ " (pid integer NOT NULL, name character varying(255), "
                    + " onlinedescendants integer, CONSTRAINT person_pkey PRIMARY KEY (pid))";
			stmt.executeUpdate(sql);

			sql = " CREATE TABLE IF NOT EXISTS dissertation "
					+ " (did serial NOT NULL, author integer, title text, university text, year character varying(255),"
					+ " CONSTRAINT dissertation_pkey PRIMARY KEY (did ), "
					+ " CONSTRAINT dissertation_author_fkey FOREIGN KEY (author) "
					+ " REFERENCES person (pid) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE)";
			stmt.executeUpdate(sql);

			sql = " CREATE TABLE IF NOT EXISTS advised "
					+ " (student integer NOT NULL, advisororder integer, advisor integer NOT NULL,"
					+ " CONSTRAINT advised_pkey PRIMARY KEY (student , advisor ), "
					+ " CONSTRAINT advised_advisor_fkey FOREIGN KEY (advisor) "
                    + " REFERENCES person (pid) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE, "
					+ " CONSTRAINT advised_student_fkey FOREIGN KEY (student) "
                    + " REFERENCES dissertation (did) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE, "
                    + " CONSTRAINT advised_advisororder_check CHECK (advisororder > 0))";
			stmt.executeUpdate(sql);

/*			sql = " CREATE TRIGGER IF NOT EXISTS delPerson AFTER DELETE ON dissertation FOR EACH ROW "
					+ " BEGIN "
					+ " DELETE FROM person WHERE OLD.author = pID; " + " END";
			stmt.executeUpdate(sql);*/

			stmt.close();
			conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Records created successfully");
	}
}
