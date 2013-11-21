package com.mgdb.datatypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Person {
    int ID;
    String name;
    List<String> institutions;
    List<String> gradYears;
    List<String> dissertations;
    List<String> advisorsIDs;
    Map<Integer,ArrayList<String>> dissInfo;
    List<Integer> studentIDs;
    int studentsSum;
    
    public void setStudentsSum(int studentsSum) {
		this.studentsSum = studentsSum;
	}

	public Person(int id) {
        this.ID = id;
    }
    
    public void setID(int iD) {
        ID = iD;
    }
    
    public int getID() {
        return ID;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public List<String> getInstitutions() {
        return institutions;
    }

    public List<String> getDissertations() {
        return dissertations;
    }

    public List<String> getAdvisorsIDs() {
        return advisorsIDs;
    }

    public List<Integer> getStudentIDs() {
        return studentIDs;
    }

    public int getStudentsSum() {
        return studentsSum;
    }
    
    public void parseInfo(List<String> lines) {
        int i = 0;
        int size = lines.size();
        
        int order = 1;
        while((i<size) && (!lines.get(i).contains("h2 style=")) ){
            i++;
        }
        while(i<size) {
            String line = lines.get(i);
            if (line.contains("h2 style=")) {
                i +=1;
                line = lines.get(i);
                name = line.split("</h2>")[0].trim();
                name = StringEscapeUtils.unescapeHtml(name);
            }
            
            //Get year and university
            if (line.contains("#006633; margin-left: 0.5em\">")){
            	 if(institutions != null && institutions.size()>0){
            		setDissInfo(order);
            		order += 1;
                 }
            	 
                String inst_year = line.split("#006633; margin-left: 0.5em\">")[1];
                String strInstitution = inst_year.split("</span>")[0];
                strInstitution = StringEscapeUtils.unescapeHtml(strInstitution);
                if(institutions == null) institutions = new ArrayList<String>();
                institutions.add(strInstitution);
                
                String strYear = inst_year.split("</span>")[1].trim();
                if(gradYears == null) gradYears = new ArrayList<String>();
                gradYears.add(strYear);
              }
            
            //Get dissertation title
            if (line.contains("thesisTitle")) {
                i += 2;
                line = lines.get(i);
                if(dissertations == null) dissertations = new ArrayList<String>();
                if (line.split("</span></div>").length != 0) {
                   String strDissertation = line.split("</span></div>")[0];
                   strDissertation = StringEscapeUtils.unescapeHtml(strDissertation);                   
                   dissertations.add(strDissertation);
                } else
                	dissertations.add("");
            }
            
            // Get all advisors
            if (line.contains("<p style=\"text-align: center; line-height: 2.75ex\">")) {                              
                int len = line.split("a href=\"id.php\\?id=").length;
                
                String[] advisor_id = line.split("a href=\"id.php\\?id=");                
                int j=1; 
                StringBuffer buffer = new StringBuffer();
                while(j<len) {
                    int advisorId = Integer.parseInt(advisor_id[j].split("\">")[0]);
                    buffer.append(advisorId);
                    buffer.append(",");
                    j++;
                }
                if(advisorsIDs == null) advisorsIDs = new ArrayList<String>();
                advisorsIDs.add(buffer.toString());
            }
            
            //Get students
            if (line.contains("<td><a href=\"id.php?id=")) {
                int student = Integer.parseInt(line.split("a href=\"id.php\\?id=")[1].split("\">")[0]);
                if(studentIDs == null) studentIDs = new ArrayList<Integer>();
                studentIDs.add(student);
            }
            // Get number of descendants
            // Uses only '</a> and ' as search string and not '>students</a> and '
            // because 'students' can change to 'student' !
            if (line.contains("According to our current on-line database")) 
                studentsSum = Integer.parseInt(line.split("</a> and ")[1].split("<a href=")[0].trim());
            if (line.contains("No students known."))
                studentsSum = 0;
            if (line.contains("If you have additional information or"))
                break;
            i++;
        }
        if(institutions != null && institutions.size()>0){
        	setDissInfo(order);
         }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("name : " + name + ", id : " + ID + " \n");

        buffer.append("Dissertation Info: ");
        if(dissInfo != null && dissInfo.size() > 0) {
            Iterator<Integer> itr = dissInfo.keySet().iterator();
            while(itr.hasNext()) {
                int order = itr.next();
                ArrayList<String> info = dissInfo.get(order);
                buffer.append(order + "-");
                buffer.append("institution: " + info.get(0) + ", year: " + info.get(1)+", ");
                buffer.append("dissertation: " + info.get(2) + ", advisors: " + info.get(3));
                buffer.append("; ");
            }
        }
        buffer.append("\n");

        buffer.append("students: ");
        if(studentIDs != null && studentIDs.size() > 0) {
            for(int student : studentIDs) {
                buffer.append(student);
                buffer.append(",");
            }
        }
        buffer.append("\n");
        buffer.append("Total students: " + studentsSum);
        return buffer.toString();
    }
    
    public JSONObject toJSON() throws Exception {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("id", ID);
        
        if(studentIDs != null && studentIDs.size() > 0) {
            JSONArray arr = new JSONArray();
            for(int student : studentIDs) {
                arr.put(student);
            }
            object.put("students", arr);
        }
        object.put("studentsNum", studentsSum);

        if(dissInfo != null && dissInfo.size() > 0) {
            JSONArray arr = new JSONArray();
            Iterator<Integer> itr = dissInfo.keySet().iterator();
            while(itr.hasNext()) {
            	int pid = itr.next();
            	ArrayList<String> info = dissInfo.get(pid);
                JSONObject sub = new JSONObject();
                sub.put("institution", info.get(0));
                sub.put("year", info.get(1));
                sub.put("dissertation", info.get(2));
                sub.put("advisors", info.get(3));
                arr.put(sub);
            }
            object.put("Dissertation Info", arr);
        }
        return object;
    }
    
	public void setDissInfo(int order) {
		int len = institutions.size();
		ArrayList<String> info = new ArrayList<String>();
		info.add(institutions.get(len - 1));
		info.add(gradYears.get(len - 1));
		info.add(dissertations.get(len - 1));
		if (advisorsIDs == null || advisorsIDs.size() < len) {
			if (advisorsIDs == null)
				advisorsIDs = new ArrayList<String>();
			advisorsIDs.add("");
		}
		info.add(advisorsIDs.get(advisorsIDs.size() - 1));
		if (dissInfo == null)
			dissInfo = new HashMap<Integer, ArrayList<String>>();
		dissInfo.put(order, info);
	}
	
	public void fromJson(JSONObject obj) throws Exception{
        this.ID = obj.getInt("id");
        this.name = obj.getString("name");
        if(obj.has("students")) {
            JSONArray students = obj.getJSONArray("students");
            if(students != null && students.length() > 0) {
                this.studentIDs = new ArrayList<Integer>();
                int size = students.length();
                for(int i = 0; i < size; i++) {
                    this.studentIDs.add(students.getInt(i));
                }
            }
        }
        if(obj.has("Dissertation Info")) {
            JSONArray dissertInfos = obj.getJSONArray("Dissertation Info"); 
            if(dissertInfos != null && dissertInfos.length() > 0) {
                this.advisorsIDs = new ArrayList<String>();
                int size = dissertInfos.length();
                for(int i = 0; i < size; i++) {
                    JSONObject dissert = dissertInfos.getJSONObject(i);
                    if(dissert.has("advisors")) {
                        String advisors = dissert.getString("advisors");
                        String[] parts = advisors.split(",");
                        for(String part : parts) {
                            if(part != null && (part.trim()).length() > 0) {
                                this.advisorsIDs.add(part);
                            }
                        }
                    }
                }
            }
        }
    }
	
	public void fromDB(ResultSet rs) throws SQLException {
		try {
			this.ID = rs.getInt("pid");
			this.name = rs.getString("name");
			this.advisorsIDs = new ArrayList<String>();
			this.advisorsIDs.add(rs.getString("advisor"));
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
}
