package com.ultimatepolish.polishscorebook;

import android.widget.TextView;

public class ViewHolder_Game {
	public String gameId;
    public String playerOne;
    public String playerTwo;
    public String score;
    
    public String getGameId(){
    	return gameId;
    }
    public String getPlayerOne(){
    	return playerOne;
    }
    public String getPlayerTwo(){
    	return playerTwo;
    }
    public String getScore(){
    	return score;
    }
    
    public void setGameId(String gameId){
    	this.gameId = gameId;
    }
    public void setPlayerOne(String playerOne){
    	this.playerOne = playerOne;
    }
    public void setPlayerTwo(String playerTwo){
    	this.playerTwo = playerTwo;
    }
    public void setScore(String score){
    	this.score = score;
    }
}
