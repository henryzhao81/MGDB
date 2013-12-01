/**
********************************
Copyright 2013 Proteus Digital Health, Inc.
********************************

CONFIDENTIAL INFORMATION OF PROTEUS DIGITAL HEALTH, INC.

Author : hzhao@proteusdh.com
Nov 16, 2013
*/

package com.mgdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.JSONObject;

import com.mgdb.database.DatabaseConnection;
import com.mgdb.datatypes.Person;
import com.mgdb.datatypes.TreeNode;

public class TreeBuilder {

    private Map<Integer, Person> pMap = new HashMap<Integer, Person>();
    private List<TreeNode> firstLevel = new ArrayList<TreeNode>();
    private Map<Integer, TreeNode> tMap = new HashMap<Integer, TreeNode>();
    private static TreeNode root = new TreeNode();

    public TreeBuilder() {}
    
    public static void main(String[] args) throws Exception {
        TreeBuilder builder = new TreeBuilder();
       // builder.readPersonFromFile("/Users/hzhao/work/git/math/MGDB/mgdb_info_1_180000.txt");
        builder.readPersonFromFile("/home/taojiang/work/git/MGDB/output/20131113/mgdb_info_1_180000.txt");
        //builder.readPersonFromDB();
     //   builder.preOrder(builder.root, new int[]{0});
    //    builder.postOrder(root, new int[]{0});        
    }
    
    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }
    
	public void preOrder(TreeNode node, int[] index) {
		if (node == null)
			return;
		node.setPreindex(index[0]++);
		System.out.println(node.getPid() + " : " + node.getPreindex());
		TreeNode[] children = node.getChildren();
		if (children != null && children.length > 0) {
			for (TreeNode each : children) {
				preOrder(each, index);
			}
		}
	}

	public void postOrder(TreeNode node, int[] index) {
		if (node == null)
			return;
		TreeNode[] children = node.getChildren();
		if (children != null && children.length > 0) {
			postOrder(children[0], index);
			if (children.length > 1) {
				for (int i = 1; i < children.length; i++) {
					postOrder(children[i], index);
				}
			}
		}
		node.setPostindex(index[0]++);
		System.out.println(node.getPid() + " : " + node.getPostindex());
    }

    public void readPersonFromFile(String file) throws Exception {
        InputStreamReader instream = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader in = new BufferedReader(instream);
        String strLine;
        while ((strLine = in.readLine()) != null) {
            if(strLine != null && strLine.length() > 0) {
                JSONObject object = null;
                try {
                    object = new JSONObject(strLine);
                    Person person = new Person(object.getInt("id"));
                    person.fromJson(object);
                    this.pMap.put(person.getID(), person);
                    TreeNode node = new TreeNode();
                    node.setPid(person.getID());
                    this.tMap.put(person.getID(), node);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Failed to pasre line");
            }
        }
        this.buildTree();
        this.buildStudents();
        System.out.println("firstLevel  : " + firstLevel.size());
        this.buildRoot();
        
        TreeNode check = tMap.get(56216);
        int[] v = new int[1];
        v[0] = 0;
        viewNode(check, v);
        System.out.println("level : " + v[0]);
        
        in.close();
        instream.close();
    }
    
/*    
    public void readPersonFromDB() {
    	String sql = " select distinct on(pid) * from person p left join "+
                     " (select * from dissertation, advised where did = student and advisororder=1) as diss " +
                     " on diss.author = p.pid ;";
    	DatabaseConnection db = new DatabaseConnection();
    	Connection conn = db.getConnection();
    	ResultSet rs = null;
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("pid");
				Person person = new Person(id);
                person.fromDB(rs);
                this.pMap.put(id, person);
                TreeNode node = new TreeNode();
                node.setPid(id);
                this.tMap.put(id, node);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		
		this.buildTree();
        System.out.println("firstLevel  : " + firstLevel.size());
        this.buildRoot();
        
        TreeNode check = tMap.get(258);
        int[] v = new int[1];
        v[0] = 0;
        viewNode(check, v);
        System.out.println("level : " + v[0]);
    }*/
    
    public void viewNode(TreeNode node, int[] level) {
        if(node == null) 
            return;
        System.out.println("node : " + node.getPid());
        level[0]++;
        viewNode(node.getParent(), level);
    }
    
    public void buildRoot() {
        TreeNode[] nodes = firstLevel.toArray(new TreeNode[0]);
        for(TreeNode node : nodes) {
            node.setParent(root);
        }
        root.setPid(0);
        root.setChildren(nodes);
    }
    
    public void buildTree() {
        Iterator<Integer> keys = this.pMap.keySet().iterator();
        while(keys.hasNext()) {
            int key = keys.next();
            Person eachPer = this.pMap.get(key);
            TreeNode eachNode = this.tMap.get(key);
            List<String> aids = eachPer.getAdvisorsIDs();
            if(aids != null && aids.size() > 0) {
                TreeNode adNode = this.tMap.get(Integer.valueOf(aids.get(0)));
                eachNode.setParent(adNode);
            } else {
                firstLevel.add(eachNode);
            }
        }
    }
    
    public void buildStudents() {
        Iterator<Integer> keys = this.pMap.keySet().iterator();
        while(keys.hasNext()) {
            int key = keys.next();
            Person eachPer = this.pMap.get(key);
            TreeNode eachNode = this.tMap.get(key);
            List<Integer> cids = eachPer.getStudentIDs();
            if(cids != null && cids.size() > 0) {
                int size = cids.size();
                TreeNode[] children = new TreeNode[size];
                for(int i = 0; i < size; i++) {
                    TreeNode cNode = this.tMap.get(cids.get(i));
                    TreeNode pNode = cNode.getParent();
                    if (pNode != null && pNode.getPid() == key) {
                    	children[i] = cNode;
                    }                    
                }
                eachNode.setChildren(children);
            }
        }
    }

}
