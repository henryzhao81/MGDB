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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.mgdb.datatypes.Person;
import com.mgdb.datatypes.TreeNode;

public class TreeBuilder {

    private Map<Integer, Person> pMap = new HashMap<Integer, Person>();
    private List<TreeNode> firstLevel = new ArrayList<TreeNode>();
    private Map<Integer, TreeNode> tMap = new HashMap<Integer, TreeNode>();
    private TreeNode root = new TreeNode();

    public TreeBuilder() {}
    
    public static void main(String[] args) throws Exception {
        TreeBuilder builder = new TreeBuilder();
        builder.readPersonFromFile("/Users/hzhao/work/git/math/MGDB/mgdb_info_1_180000.txt");
    }
    
    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }
    
    public void preOrder(TreeNode node) {
        if(node == null)
            return;
        System.out.println(node.getPid());
        TreeNode[] children = node.getChildren();
        for(TreeNode each : children) {
            preOrder(each);
        }
    }
    
    public void postOrder(TreeNode node) {
        if(node == null)
            return;
        TreeNode[] children = node.getChildren();
        for(TreeNode each : children) {
            
        }
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
                    Person person = new Person();
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
        System.out.println("firstLevel  : " + firstLevel.size());
        this.buildRoot();
        
        TreeNode check = tMap.get(3);
        int[] v = new int[1];
        v[0] = 0;
        viewNode(check, v);
        System.out.println("level : " + v[0]);
        
        in.close();
        instream.close();
    }
    
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
            List<Integer> aids = eachPer.getAdvisorsIDs();
            if(aids != null && aids.size() > 0) {
                TreeNode adNode = this.tMap.get(aids.get(0));
                eachNode.setParent(adNode);
            } else {
                firstLevel.add(eachNode);
            }
            List<Integer> cids = eachPer.getStudentIDs();
            if(cids != null && cids.size() > 0) {
                int size = cids.size();
                TreeNode[] children = new TreeNode[size];
                for(int i = 0; i < size; i++) {
                    TreeNode cNode = this.tMap.get(cids.get(i));
                    children[i] = cNode;
                }
                eachNode.setChildren(children);
            }
        }
    }
}
