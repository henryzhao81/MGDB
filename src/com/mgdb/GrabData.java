package com.mgdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.JSONObject;

import com.mgdb.datatypes.Person;

public class GrabData {
	static String getUrl = "http://genealogy.math.ndsu.nodak.edu/id.php?id=";
	static String file = "/home/taojiang/work/git/MGDB/output/mgdb_info";
	static String notExistStr = "You have specified an ID that does not exist in the database. Please back up and try again.";

	public static void main(String[] args) throws IOException {
		if (args != null && args.length > 1) {
			int start = Integer.parseInt(args[0]);
			int end = Integer.parseInt(args[1]);
			String suffix = args[2];

			long currentTime = System.currentTimeMillis();

			for (int i = start; i <= end; i++) {
				ArrayList<String> pageInfo = new ArrayList<String>();
				doGet(getUrl, i, pageInfo);
				extractData(i, pageInfo, suffix);
			}

			System.out.println("Total cost : "
					+ (System.currentTimeMillis() - currentTime));
		} else {
			System.out.println("2 or more parameters is required");
		}
		// recursiveAncestors(person);
		// recursiveDecendents(person);
	}
	
	public static Person extractData(int id, ArrayList<String> pageInfo, String suffix) {
		if (pageInfo.size() != 0) {
			if (pageInfo.get(0).contains(notExistStr)) {
				System.out.println("The ID " + id
						+ " doesn't exist on the website.");
			} else {
				Person person = new Person(id);
				person.parseInfo(pageInfo);
				System.out.println(person.toString());
				if (person.getName() != null && person.getName().length() > 0) {
					try {
						writeFile(file + "_" + suffix + ".txt",
								person.toJSON());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					System.out.println("Empty username for ID " + id);
				}
				return person;
			}
		} else {
			System.out.println("Failed to parse pageInfo for ID : " + id);
		}
		return null;
	}

	public static void writeFile(String file, JSONObject object)
			throws Exception {
		FileWriterWithEncoding fstream = new FileWriterWithEncoding(file,
				"UTF-8", true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(object.toString());
		out.newLine();
		out.close();
	}

	public static void doGet(String urlString, int values, List<String> pageInfo) {
		HttpURLConnection connection = null;
		try {
			String getURL = urlString + values;
			URL getUrl = new URL(getURL);
			connection = (HttpURLConnection) getUrl.openConnection();
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
			String lines;
			while ((lines = reader.readLine()) != null) {
				pageInfo.add(lines);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
/*
	public static void recursiveAncestors(Person person) throws IOException {
		List<String> advisorIds = person.getAdvisorsIDs();
		Queue<Integer> advisorsQueue = new LinkedBlockingQueue<Integer>();
		Map<Integer, Person> advisorMap = new HashMap<Integer, Person>();
		advisorMap.put(person.getID(), person);
		for (String advisor : advisorIds) {
			Person each = new Person(Integer.valueOf(advisor));
			List<String> lines = new ArrayList<String>();
			doGet(getUrl, advisor, lines);
			each.parseInfo(lines);
			System.out.println(each.toString());
			advisorMap.put(each.getID(), each);
			List<String> ids = each.getAdvisorsIDs();
			if (ids != null && ids.size() > 0) {
				for (String i : ids) {
					advisorsQueue.offer(i);
				}
			}
		}
		recursiveAncestorsHelper(advisorMap, advisorsQueue);
	}

	private static void recursiveAncestorsHelper(Map<Integer, Person> persons,
			Queue<Integer> queue) throws IOException {
		while (!queue.isEmpty()) {
			int id = queue.poll();
			if (!persons.containsKey(id)) {
				Person each = new Person(id);
				List<String> lines = new ArrayList<String>();
				doGet(getUrl, id, lines);
				each.parseInfo(lines);
				persons.put(each.getID(), each);
				System.out.println(each.toString());
				List<Integer> ids = each.getAdvisorsIDs();
				if (ids != null && ids.size() > 0) {
					for (int i : ids) {
						queue.offer(i);
					}
				}
			}
		}
	}*/

	public static void recursiveDecendents(Person person) throws IOException {
		List<Integer> studentsIds = person.getStudentIDs();
		Queue<Integer> studentsQueue = new LinkedBlockingQueue<Integer>();
		Map<Integer, Person> studentsMap = new HashMap<Integer, Person>();
		studentsMap.put(person.getID(), person);
		for (int studentId : studentsIds) {
			Person each = new Person(studentId);
			List<String> lines = new ArrayList<String>();
			doGet(getUrl, studentId, lines);
			each.parseInfo(lines);
			System.out.println(each.toString());
			studentsMap.put(each.getID(), each);
			List<Integer> ids = each.getStudentIDs();
			if (ids != null && ids.size() > 0) {
				for (int i : ids) {
					studentsQueue.offer(i);
				}
			}
		}
		recursiveDecendentsHelper(studentsMap, studentsQueue);
	}

	private static void recursiveDecendentsHelper(Map<Integer, Person> persons,
			Queue<Integer> queue) throws IOException {
		while (!queue.isEmpty()) {
			int id = queue.poll();
			if (!persons.containsKey(id)) {
				Person each = new Person(id);
				List<String> lines = new ArrayList<String>();
				doGet(getUrl, id, lines);
				each.parseInfo(lines);
				persons.put(each.getID(), each);
				System.out.println(each.toString());
				List<Integer> ids = each.getStudentIDs();
				if (ids != null && ids.size() > 0) {
					for (int i : ids) {
						queue.offer(i);
					}
				}
			}
		}
	}
}
