

import java.awt.List;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.xml.ws.http.HTTPException;

import org.omg.CORBA.portable.InputStream;

public class GrabData {
	
	static String getUrl = "http://genealogy.math.ndsu.nodak.edu/id.php?id=";
	static String values = "85309";
	
	public static void main(String[] args) throws IOException {		
		ArrayList<String> pageInfo = new ArrayList<String>();
		doGet(getUrl, values, pageInfo);
		ArrayList<String> info = extractInfo(pageInfo);
		for (String str : info) 
		   System.out.println(str);
		System.out.println("\n");
		
		ArrayList<String> advisorList = new ArrayList<String>();
		recursiveAncestors(info.get(4), advisorList);
		/*
		String postUrl = "http://genealogy.math.ndsu.nodak.edu/query-prep.php";
		String postParam = "chrono=0&given_name=&other_names=&family_name=Story&school=&year=&thesis=&country=&msc=&submit=Submit";
		//doPost(postUrl, postParam);*/
	}

	public static void doGet(String urlString, String values, ArrayList<String> pageInfo) throws IOException{
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
	
	public static void doPost(String urlString, String values) throws IOException{       
        URL postUrl = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        connection.connect();        
        
        DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());
        out.writeBytes(values); 
        out.flush();
        out.close(); // flush and close
        
        int responseCode = connection.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + urlString);
		System.out.println("Post parameters : " + values);
		System.out.println("Response Code : " + responseCode);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        String line="";
        while ((line = reader.readLine()) != null){
            System.out.println(line);
        }
        reader.close();
        connection.disconnect();
    }
	
	public static ArrayList<String> extractInfo(ArrayList<String> pageInfo) throws UnsupportedEncodingException {
		String name = "", institution="", year="", dissertation="", advisors="", students="", studNo="0";
		ArrayList<String> info = new ArrayList<String>();
		
		int i=0;
		int size = pageInfo.size();
		
		while((i<size) && (!pageInfo.get(i).contains("h2 style=")) ){
			i++;
	    }
		
		while(i<size) {
			String line = pageInfo.get(i);
		    
			if (line.contains("h2 style=")) {
				i +=1;
				line = pageInfo.get(i);
				name = line.split("</h2>")[0];
			}
			
			//Get year and university
			if (line.contains("#006633; margin-left: 0.5em\">")){
				String inst_year = line.split("#006633; margin-left: 0.5em\">")[1];
				institution += inst_year.split("</span>")[0]+ ";";
				year += inst_year.split("</span>")[1].trim() + ";";

/*				if len(self.institution[len(self.institution)-1]) == 0:
					self.institution[len(self.institution)-1] = None
				if len(self.year[len(self.year)-1]) == 0:
					self.year[len(self.year)-1] = None*/
			}
			
			//Get dissertation title
			if (line.contains("thesisTitle")) {
				i += 2;
				line = pageInfo.get(i);
				if (line.split("</span></div>").length != 0)
				   dissertation += line.split("</span></div>")[0] + ";";
/*				if len(self.dissertation[len(self.dissertation)-1]) == 0:
					self.dissertation[len(self.dissertation)-1] = None*/
			}
			
			// Get all advisors
			if (line.contains("<p style=\"text-align: center; line-height: 2.75ex\">")) {
				int len = line.split("a href=\"id.php\\?id=").length;
				String[] advisor_id = line.split("a href=\"id.php\\?id=");
				
				int j=1; 
				while(j<len) {
					advisors += advisor_id[j].split("\">")[0] + ";";
					j++;
				}
			}
			
			//Get students
			if (line.contains("<td><a href=\"id.php?id=")) {
				students +=line.split("a href=\"id.php\\?id=")[1].split("\">")[0]+";";				
				//students += line.split("a href=\"id.php?")[1].split("id=")[1].split("\">")[0]+";";
			}
			
			// Get number of descendants
			// Uses only '</a> and ' as search string and not '>students</a> and '
		    // because 'students' can change to 'student' !
			if (line.contains("According to our current on-line database")) 
				studNo = line.split("</a> and ")[1].split("<a href=")[0];

			if (line.contains("No students known."))
				studNo = "0";

			if (line.contains("If you have additional information or"))
				break;
			
			i++;
		}	
		info.add(name);
		info.add(institution);
		info.add(year);
		info.add(dissertation);
		info.add(advisors);
		info.add(students);
		info.add(studNo);
		
		return info;
	}
	
	public static void recursiveAncestors(String ancestor, ArrayList<String> advisorList) throws IOException {
		if (ancestor != "") {
		    String[] advisors = ancestor.split(";");
		    for(int i=0; i<advisors.length; i++) {
			   advisorList.add(advisors[i]);
		    }	
		}
		
		while(!advisorList.isEmpty()) {
			ArrayList<String> pageArr = new ArrayList<String>();
			doGet(getUrl, advisorList.get(0), pageArr);
			ArrayList<String> info = extractInfo(pageArr);
			for (String str : info) 
			   System.out.println(str);
			System.out.println("\n");
			
			if (ancestor != "") {
			    String[] tmp = info.get(4).split(";");
			    for(int j=0; j<tmp.length; j++) {
				    if (!advisorList.contains(tmp[j]))
				       advisorList.add(tmp[j]);	
			    }
			}
			advisorList.remove(0);
		}
	}

}