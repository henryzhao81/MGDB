package com.mgdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class FindDescendant {
	static int totalDescendant = 0;
	static Set<Integer> descendantList = new HashSet<Integer>();
	
	public static void main(String[] args) throws IOException {
		long currentTime = System.currentTimeMillis();
		
		int id = Integer.valueOf(args[0]);
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String db = "/home/taojiang/MGDB/trunk/MGDB_late";
			conn = DriverManager.getConnection("jdbc:sqlite:" + db);
		
		    findAllDescendant(id, conn);	
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}   
		    
		System.out.println("Total Descendants: " + totalDescendant);
		Integer[] tmp = descendantList.toArray(new Integer[0]);
	    Arrays.sort(tmp);
		for(int i: tmp) {
			System.out.print(i + ",");
		}
		System.out.println("\nTotal cost : "+ (System.currentTimeMillis() - currentTime));
	}
	
	public static void findAllDescendant(int id, Connection conn) {
		//System.out.println(String.format("running in %s mode",
		//		SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java"));
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{		
			stmt = conn
					.prepareStatement("SELECT author FROM advised, dissertation WHERE student=dID AND advisor = ? ;");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			while (rs.next()) {				
				int pid = Integer.parseInt(rs.getString("author"));
				if(!descendantList.contains(pid)) {
					totalDescendant +=1;
					descendantList.add(pid);
					findAllDescendant(pid, conn);
				}				
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		//System.out.println("Opened database successfully");
	}
}
