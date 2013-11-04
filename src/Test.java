import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Test {
	static String getUrl = "http://genealogy.math.ndsu.nodak.edu/quickSearch.php";
	static String values = "searchTerms=Story";
	
	public static void main(String[] args) throws IOException {		
		//ArrayList<String> pageInfo = new ArrayList<String>();
		//doPost(getUrl, values);
		for (int i=158700; i<=160000; i++)
			System.out.print(i+" ");
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
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//设置编码,否则中文乱码
        String line="";
        while ((line = reader.readLine()) != null){
            System.out.println(line);
        }
        reader.close();
        connection.disconnect();
    }

}
