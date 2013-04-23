package com.ultimatepolish.polishscorebook;

public class ViewHolder_Session {
	public String id;
    public String name;
    
    public String getId(){
    	return id;
    }
    public String getName(){
    	return name;
    }
    
    public void setId(String sessionId){
    	this.id = sessionId;
    }
    public void setName(String sessionName){
    	this.name = sessionName;
    }
}
