package com.mgdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.common.hash.BloomFilter;
import com.mgdb.database.DatabaseConnection;
import com.mgdb.datatypes.Person;
import com.mgdb.datatypes.PersonFunnel;

public class BloomTest {
	//static int maxId = 178191;
	
/*	public static void main(String[] args) throws IOException {
		testBloomFilter(178191);
	}*/

	public ArrayList<Integer> testBloomFilter(int maxId) {	
		BloomFilter<Person> friends = BloomFilter.create(new PersonFunnel(),
				200000, 0.000001);	
		
		buildBloomFilter(friends);
		
		ArrayList<Integer> rs = new ArrayList<Integer>();
		for (int i = 177315; i <= maxId; i++) {
			Person p = new Person(i);
			if (!friends.mightContain(p)) {
				rs.add(i);
				System.out.print(i+",");
			}
		}
		System.out.println("\nComparion is completed");
		return rs;
	}
	
	private void buildBloomFilter(BloomFilter<Person> friends) {
		String sql = " select * from person;";	
		DatabaseConnection db = new DatabaseConnection();
		Connection conn = db.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery(sql);
			while (rs.next()) {
				int spid = Integer.parseInt(rs.getString("pid"));
				Person p = new Person(spid);
				friends.put(p);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}
}
