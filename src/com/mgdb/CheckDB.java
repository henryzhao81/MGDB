package com.mgdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mgdb.database.DatabaseConnection;
import com.mgdb.datatypes.Person;

public class CheckDB {

	public static void main(String[] args) throws IOException {
		Map<Integer, Integer> onlineSet = new HashMap<Integer, Integer>();
		onlineSet = getOnlineDescendants();
		checkDescendants(onlineSet);
		System.out.println("DB Checking is completed");
	}

	private static Map<Integer, Integer> getOnlineDescendants() {
		Map<Integer, Integer> onlineSet = new HashMap<Integer, Integer>();
		DatabaseConnection db = new DatabaseConnection();
		Connection conn = db.getConnection();
		ResultSet rs = null;
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from person;");
			while (rs.next()) {
				int spid = Integer.parseInt(rs.getString("pid"));
				int onlineDescend = Integer.parseInt(rs
						.getString("onlinedescendants"));
				onlineSet.put(spid, onlineDescend);
			}
			rs.close();
			stmt.close();
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Get online descendants successfully");
		return onlineSet;
	}

	public static void checkDescendants(Map<Integer, Integer> onlineSet) {
		DatabaseConnection db = new DatabaseConnection();
		Connection conn = db.getConnection();
		
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			Iterator<Integer> itr = onlineSet.keySet().iterator();
			while (itr.hasNext()) {
				int spid = itr.next();
				int onlineDescend = onlineSet.get(spid);
				st = conn.prepareStatement("WITH RECURSIVE descendents AS ( "
								+ "( select author from dissertation, advised where student=did and advisor = ? )"
								+ " UNION "
								+ "(select di.author from dissertation di, advised a, descendents d where student=did and a.advisor=d.author)"
								+ ")"
								+ "SELECT count(author) as dbDescend from descendents");
				st.setInt(1, spid);
				rs = st.executeQuery();
				if (rs.next()) {
					int dbDescend = Integer.parseInt(rs.getString("dbDescend"));
					if (dbDescend != onlineDescend) {
/*						ArrayList<String> pageInfo = new ArrayList<String>();
						GrabData.doGet(GrabData.getUrl, spid, pageInfo);
						Person person = GrabData.extractData(spid, pageInfo, "dbCheck");
						if(person.getStudentsSum() == dbDescend) {
							PreparedStatement updateSql = conn.prepareStatement("Update person set onlinedescendants=? where pid=?");
							updateSql.setInt(1, dbDescend);
							updateSql.setInt(2, spid);
							updateSql.execute();
							updateSql.close();
						}*/
						System.out.println("ID:" + spid + "; OnlineDescendant:"
										+ onlineDescend + "; DBDescendant:"
										+ dbDescend);
					}
				}
			}
			rs.close();
			rs.close();
			st.clearBatch();
			st.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

}
