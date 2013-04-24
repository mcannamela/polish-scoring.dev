package com.ultimatepolish.polishscorebook;

public class ViewHolder_Team {
	public String id;
	public String name;
	public String p1name;
	public String p2name;
    
    public String getId(){
    	return id;
    }
    public String getNickName(){
    	return name;
    }
    public String getP1Name(){
    	return p1name;
    }
    public String getP2Name(){
    	return p2name;
    }
    
    public void setId(String teamId){
    	this.id = teamId;
    }
    public void setName(String teamName){
    	this.name = teamName;
    }
    public void setP1Name(String p1Name){
    	this.p1name = p1Name;
    }
    public void setP2Name(String p2Name){
    	this.p2name = p2Name;
    }

}
