/**
********************************
Copyright 2013 Proteus Digital Health, Inc.
********************************

CONFIDENTIAL INFORMATION OF PROTEUS DIGITAL HEALTH, INC.

Author : hzhao@proteusdh.com
Nov 17, 2013
*/

package com.mgdb.datatypes;

public class TreeNode {   
    String code;
    int pid;
    TreeNode parent;
    TreeNode[] children;
    int level;
	int preindex;
	int postindex;
    
    public int getPreindex() {
		return preindex;
	}

	public int getPostindex() {
		return postindex;
	}
	
    public void setPreindex(int preindex) {
		this.preindex = preindex;
	}

	public void setPostindex(int postindex) {
		this.postindex = postindex;
	}
    
    public TreeNode() {}
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode[] getChildren() {
        return children;
    }

    public void setChildren(TreeNode[] children) {
        this.children = children;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("pid : " + this.getPid() + " ");
        buffer.append("preIndex : " + this.getPreindex() + " ");
        buffer.append("postIndex : " + this.getPostindex() + " ");
        return buffer.toString();
    }
}
