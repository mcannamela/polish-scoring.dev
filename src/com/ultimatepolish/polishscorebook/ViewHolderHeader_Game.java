package com.ultimatepolish.polishscorebook;

import java.util.ArrayList;

import com.ultimatepolish.scorebookdb.Game;

public class ViewHolderHeader_Game {
	private String name;
	private ArrayList<ViewHolder_Game> gameList = new ArrayList<ViewHolder_Game>();;
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ViewHolder_Game> getGameList() {
		return gameList;
	}
	public void setGameList(ArrayList<ViewHolder_Game> gameList) {
		this.gameList = gameList;
	}
}
