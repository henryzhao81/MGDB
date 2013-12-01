package com.mgdb;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.JSONObject;

import com.mgdb.database.DatabaseConnection;
import com.mgdb.datatypes.Person;

public class BatchUpdate {
	
	static String file = "/home/taojiang/work/git/MGDB/output/mgdb_info";
	static String suffix = "update_test_3";
	
	public static void main(String[] args) throws Exception {
		testUpdate();
	}

	public static void testUpdate() throws Exception {
		long currentTime = System.currentTimeMillis();
		BloomTest bt = new BloomTest();
		ArrayList<Integer> missingIds = bt.testBloomFilter(178320);
		ArrayList<Integer> newIds = getNewIds(missingIds);
		ArrayList<Integer> parents = findAllAdvisors(newIds);
		updateAllAdvisor(parents);
		
		Importer impo = new Importer();
        impo.constructTables();
        impo.readPersonFromFile(file + "_" + suffix + ".txt", 1000);
        impo.readDissertationFromFile(file + "_" + suffix + ".txt", 1000);
        
		System.out.println("Total cost : "
				+ (System.currentTimeMillis() - currentTime));
	}

	private static ArrayList<Integer> getNewIds(ArrayList<Integer> missingIds) {
		ArrayList<Integer> newIds = new ArrayList<Integer>();
		for (int id : missingIds) {
			ArrayList<String> pageInfo = new ArrayList<String>();
			GrabData.doGet(GrabData.getUrl, id, pageInfo);
			Person person = GrabData.extractData(id, pageInfo, suffix);
			if (person !=null && person.getAdvisorsIDs() != null) {
				for (String str : person.getAdvisorsIDs()) {
					if (!str.equals(""))
						newIds.add(Integer.valueOf(str.split(",")[0]));
				}
			}
		}	
		return newIds;
	}

	private static ArrayList<Integer> findAllAdvisors(ArrayList<Integer> newIds) {
		String sql = " WITH RECURSIVE ancestors AS ("
				+ "( select advisor from advised, dissertation "
				+ " where student=did and author = ? ) "
				+ " UNION "
				+ "( select a.advisor from ancestors an, advised a, dissertation d"
				+ "  where student=did and d.author=an.advisor ))"
				+ " SELECT array_agg(advisor) as parent from ancestors;";

		ArrayList<Integer> advisorList = new ArrayList<Integer>();
		DatabaseConnection db = new DatabaseConnection();
		Connection conn = db.getConnection();
		PreparedStatement prep = null;
		ResultSet rs = null;
		try {
			for (int id : newIds) {
				prep = conn.prepareStatement(sql);
				prep.setInt(1, id);
				rs = prep.executeQuery();

				while (rs.next()) {
					String parent = rs.getString("parent");
					System.out.println(parent);
					if (parent != null) {
						String[] advisors = parent.substring(1,
								parent.length() - 1).split(",");
						for (String str : advisors) {
							int i = Integer.valueOf(str);
							if (!advisorList.contains(i))
								advisorList.add(i);
						}
					}
				}
				prep.close();
			}
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");

		return advisorList;
	}

	private static void updateAllAdvisor(ArrayList<Integer> advisorList) {
		for (int j : advisorList) {
			ArrayList<String> pageInfo = new ArrayList<String>();
			GrabData.doGet(GrabData.getUrl, j, pageInfo);
			GrabData.extractData(j, pageInfo, suffix);
		}
	}

}
