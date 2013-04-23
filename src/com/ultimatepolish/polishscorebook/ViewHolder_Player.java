package com.ultimatepolish.polishscorebook;

public class ViewHolder_Player {
	public String name;
    public String nickName;
    public String playerId;
    
    public String getName(){
    	return name;
    }
    public String getNickName(){
    	return nickName;
    }
    public String getId(){
    	return playerId;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    public void setNickName(String nickName){
    	this.nickName = nickName;
    }
    public void setId(String playerId){
    	this.playerId = playerId;
    }
}
