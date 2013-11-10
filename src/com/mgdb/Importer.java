/**
********************************
Copyright 2013 Proteus Digital Health, Inc.
********************************

CONFIDENTIAL INFORMATION OF PROTEUS DIGITAL HEALTH, INC.

Author : hzhao@proteusdh.com
Nov 9, 2013
*/

package com.mgdb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.JSONArray;
import org.json.JSONObject;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class Importer {
    //static String file = "/Users/hzhao/work/git/math/MGDB/output/mgdb_info";
    static String file = "/home/taojiang/work/git/MGDB/output/mgdb_info";
    public Importer() {}

    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 2) {
            int start = Integer.parseInt(args[0]);
            int end = Integer.parseInt(args[1]);
            int batch = Integer.parseInt(args[2]);
            String suffix = start + "_" + end;
            Importer impo = new Importer();
            impo.readPersonFromFile(file + "_" + suffix + ".txt", batch);
            impo.readDissertationFromFile(file + "_" + suffix + ".txt", batch);
        }else {
            System.out.println("3 or more parameters is required");
        }
    }

    public void readPersonFromFile(String file, int batch) throws Exception {
        InputStreamReader instream = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader in = new BufferedReader(instream);
        String strLine;
        List<JSONObject> objList = new ArrayList<JSONObject>(batch);
        int index = 0;
        while ((strLine = in.readLine()) != null) {
            if(index == batch) {
                this.importPerson(objList);
                index = 0;
                objList.clear();
            }
            if(strLine != null && strLine.length() > 0) {
                JSONObject object = null;
                try {
                    object = new JSONObject(strLine);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                objList.add(object);
            } else {
                System.out.println("Failed to pasre line");
            }
            index++;
        }
        if(objList.size() > 0) {
            this.importPerson(objList);
        }
        in.close();
        instream.close();
    }
    
    public void readDissertationFromFile(String file, int batch) throws Exception {
        InputStreamReader instream = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader in = new BufferedReader(instream);
        String strLine;
        List<JSONObject> objList = new ArrayList<JSONObject>(batch);
        int index = 0;
        while ((strLine = in.readLine()) != null) {
            if(index == batch) {
                this.importDissertation(objList);
                index = 0;
                objList.clear();
            }
            if(strLine != null && strLine.length() > 0) {
                JSONObject object = null;
                try {
                    object = new JSONObject(strLine);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                objList.add(object);
            } else {
                System.out.println("Failed to pasre line");
            }
            index++;
        }
        if(objList.size() > 0) {
            this.importDissertation(objList);
        }
        in.close();
        instream.close();
    }

    private void importPerson(List<JSONObject> list) {
        ODatabaseDocumentTx database = new ODatabaseDocumentTx("remote:localhost/mgdb").open("admin","admin");
        for(JSONObject obj : list) {
            System.out.println(obj.toString());
            constructPerson(obj);
        }
        System.out.println("===== person batch finish ====");
        database.close();
    }
    
    private void importDissertation(List<JSONObject> list) {
        ODatabaseDocumentTx database = new ODatabaseDocumentTx("remote:localhost/mgdb").open("admin","admin");
        for(JSONObject obj : list) {
            System.out.println(obj.toString());
            constructDissertation(obj);
        }
        System.out.println("===== dissertation batch finish ====");
        database.close();
    }
    
    private void constructPerson(JSONObject object) {
        ODocument doc = new ODocument("person");
        int pid = -1;
        String name = "NULL";
        int students = 0;
        try {
            pid = object.getInt("id");
            name = object.getString("name");
            JSONArray array = object.getJSONArray("students");
            if(array != null)
                students = array.length();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        doc.field("pID", pid);
        doc.field("name", name);   
        doc.field("onlineDescendants", students);
        doc.save();
    }
    
    private void constructDissertation(JSONObject object) {
        JSONArray dissertArr = null;
        try {
            dissertArr = object.getJSONArray("disserations");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        if(dissertArr != null && dissertArr.length() > 0) {
            try {
                for(int i = 0; i < dissertArr.length(); i++) {
                    String str = dissertArr.getString(i);
                    ODocument doc = new ODocument("dissertation");
                    doc.field("title", str);
                    int pid = object.getInt("id");                   
                    String strQuery = "select from person where pID = '" + pid + "'";
                    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(strQuery);
                    List<ODocument> results = ODatabaseRecordThreadLocal.INSTANCE.get().command(query).execute();
                    if(results != null && results.size() > 0) {
                        doc.field("author", results.get(0));
                    }
                    JSONArray uniArr = object.getJSONArray("institutions");
                    if(uniArr != null && uniArr.length() > 0) {
                        //doc.field("university", uniArr.toString());
                        StringBuffer uniBuffer = new StringBuffer();
                        for(int j = 0; j < uniArr.length(); j++) {
                            uniBuffer.append(uniArr.getString(j));
                        }
                        doc.field("university", uniBuffer.toString());
                    }
                    JSONArray years = object.getJSONArray("gradYears");
                    if(years != null && years.length() > 0) {
                        StringBuffer ybuf = new StringBuffer();
                        for(int j = 0; j < years.length(); j++) {
                            JSONObject each = years.getJSONObject(j);
                            int yearStr = each.getInt("year");
                            ybuf.append(yearStr);
                        }
                        doc.field("year", ybuf.toString());
                    }
                    doc.save();
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
