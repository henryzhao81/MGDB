/**
********************************
Copyright 2013 Proteus Digital Health, Inc.
********************************

CONFIDENTIAL INFORMATION OF PROTEUS DIGITAL HEALTH, INC.

Author : hzhao@proteusdh.com
Nov 2, 2013
*/

package com.mgdb.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class Person {
    int ID;
    String name;
    List<String> institutions;
    Map<String, Integer> gradYears; //String : institution,  Integer : year
    List<String> dissertations;
    List<Integer> advisorsIDs;
    List<Integer> studentIDs;
    int studentsSum;
    
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

    public Map<String, Integer> getGradYears() {
        return gradYears;
    }

    public List<String> getDissertations() {
        return dissertations;
    }

    public List<Integer> getAdvisorsIDs() {
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
        while((i<size) && (!lines.get(i).contains("h2 style=")) ){
            i++;
        }
        while(i<size) {
            String line = lines.get(i);
            if (line.contains("h2 style=")) {
                i +=1;
                line = lines.get(i);
                name = line.split("</h2>")[0];
                System.out.println("name is " + name);
            }
            //Get year and university
            if (line.contains("#006633; margin-left: 0.5em\">")){
                String inst_year = line.split("#006633; margin-left: 0.5em\">")[1];
                String strInstitution = inst_year.split("</span>")[0];
                System.out.println("strInstitution is " + strInstitution);
                if(institutions == null) institutions = new ArrayList<String>();
                institutions.add(strInstitution);
                int year = -1;
                try {
                    year = Integer.parseInt(inst_year.split("</span>")[1].trim());
                }catch(Exception ex) {
                    System.out.println("no year found");
                }
                if(gradYears == null) gradYears = new HashMap<String, Integer>();
                if(!gradYears.containsKey(strInstitution)) {
                    gradYears.put(strInstitution, year);
                }
            }
            //Get dissertation title
            if (line.contains("thesisTitle")) {
                i += 2;
                line = lines.get(i);
                if (line.split("</span></div>").length != 0) {
                   String strDissertation = line.split("</span></div>")[0];
                   System.out.println("strInstitution is " + strDissertation);
                   if(dissertations == null) dissertations = new ArrayList<String>();
                   dissertations.add(strDissertation);
                }
            }
            // Get all advisors
            if (line.contains("<p style=\"text-align: center; line-height: 2.75ex\">")) {
                int len = line.split("a href=\"id.php\\?id=").length;
                String[] advisor_id = line.split("a href=\"id.php\\?id=");                
                int j=1; 
                while(j<len) {
                    int advisorId = Integer.parseInt(advisor_id[j].split("\">")[0]);
                    if(advisorsIDs == null) advisorsIDs = new ArrayList<Integer>();
                    advisorsIDs.add(advisorId);
                    j++;
                }
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
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("name : " + name + " id : " + ID + " \n");
        buffer.append("institutions : ");
        if(institutions != null && institutions.size() > 0) {
            for(String institution : institutions) {
                buffer.append(institution);
                buffer.append(" , ");
            }
        }
        buffer.append("\n");
        buffer.append("gradYears : ");
        if(gradYears != null && gradYears.size() > 0) {
            Iterator<String> itr = gradYears.keySet().iterator();
            while(itr.hasNext()) {
                String ins = itr.next();
                buffer.append("institution : " + ins + "-year : " + gradYears.get(ins));
                buffer.append(" , ");
            }
        }
        buffer.append("\n");
        buffer.append("disserations : ");
        if(dissertations != null && dissertations.size() > 0) {
            for(String dissertation : dissertations) {
                buffer.append(dissertation);
                buffer.append(" , ");
            }
        }
        buffer.append("\n");
        buffer.append("advisors : ");
        if(advisorsIDs != null && advisorsIDs.size() > 0) {
            for(int advisorId : advisorsIDs) {
                buffer.append(advisorId);
                buffer.append(" , ");
            }
        }
        buffer.append("\n");
        buffer.append("students : ");
        if(studentIDs != null && studentIDs.size() > 0) {
            for(int student : studentIDs) {
                buffer.append(student);
                buffer.append(" , ");
            }
        }
        buffer.append("\n");
        buffer.append("Total students : " + studentsSum);
        return buffer.toString();
    }
    
    public JSONObject toJSON() throws Exception {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("id", ID);
        if(institutions != null && institutions.size() > 0) {
            JSONArray arr = new JSONArray();
            for(String institution : institutions) {
                arr.put(institution);
            }
            object.put("institutions", arr);
        }
        if(gradYears != null && gradYears.size() > 0) {
            JSONArray arr = new JSONArray();
            Iterator<String> itr = gradYears.keySet().iterator();
            while(itr.hasNext()) {
                String ins = itr.next();
                JSONObject sub = new JSONObject();
                sub.put("institution", ins);
                sub.put("year", gradYears.get(ins));
                arr.put(sub);
            }
            object.put("gradYears", arr);
        }
        if(dissertations != null && dissertations.size() > 0) {
            JSONArray arr = new JSONArray();
            for(String dissertation : dissertations) {
                arr.put(dissertation);
            }
            object.put("disserations", arr);
        }
        if(advisorsIDs != null && advisorsIDs.size() > 0) {
            JSONArray arr = new JSONArray();
            for(int advisorId : advisorsIDs) {
                arr.put(advisorId);
            }
            object.put("advisors", arr);
        }
        if(studentIDs != null && studentIDs.size() > 0) {
            JSONArray arr = new JSONArray();
            for(int student : studentIDs) {
                arr.put(student);
            }
            object.put("students", arr);
        }
        object.put("studentsNum", studentsSum);
        return object;
    }
}
