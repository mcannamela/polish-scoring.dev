package com.ultimatepolish.polishscorebook;

import java.util.ArrayList;

public class ViewHolderHeader_Team {
	private String name;
	private ArrayList<ViewHolder_Team> teamList = new ArrayList<ViewHolder_Team>();
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ViewHolder_Team> getTeamList() {
		return teamList;
	}
	public void setTeamList(ArrayList<ViewHolder_Team> teamList) {
		this.teamList = teamList;
	}
}
