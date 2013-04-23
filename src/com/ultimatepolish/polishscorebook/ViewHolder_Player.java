package com.ultimatepolish.polishscorebook;

public class ViewHolder_Player {
	public String name;
    public String nickName;
    public String id;
    
    public String getId(){
    	return id;
    }
    public String getName(){
    	return name;
    }
    public String getNickName(){
    	return nickName;
    }
    
    public void setId(String playerId){
    	this.id = playerId;
    }
    public void setName(String playerName){
    	this.name = playerName;
    }
    public void setNickName(String playerNickName){
    	this.nickName = playerNickName;
    }
}
