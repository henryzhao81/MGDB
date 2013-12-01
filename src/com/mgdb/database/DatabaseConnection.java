package com.mgdb.database;

import java.sql.Connection;
import java.sql.DriverManager;


public class DatabaseConnection {
	
	public Connection conn;
    private String url = "jdbc:postgresql://localhost:5432/MGDB_TMP";
    private String username = "postgres";
    private String password = "113816";
	
	public Connection getConnection() {
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Open database successfully");
		return conn;
	}
	
	public void closeConnection(Connection conn) {
	    try {
	        conn.close();

	    } catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Close database successfully");
	}

}
