package com.ultimatepolish.polishscorebook;

import android.widget.TextView;

public class ViewHolder_Game {
	public TextView gameId;
    public TextView playerOne;
    public TextView playerTwo;
    
    public String getGameId(){
    	return gameId.getText().toString();
    }
    public String getPlayerOne(){
    	return playerOne.getText().toString();
    }
    public String getPlayerTwo(){
    	return playerTwo.getText().toString();
    }
    
}
