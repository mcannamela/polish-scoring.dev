package com.ultimatepolish.polishscorebook;

import java.util.ArrayList;

public class ViewHolderHeader_Venue {
	private String name;
	private ArrayList<ViewHolder_Venue> venueList = new ArrayList<ViewHolder_Venue>();
	  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ViewHolder_Venue> getVenueList() {
		return venueList;
	}
	public void setVenueList(ArrayList<ViewHolder_Venue> venueList) {
		this.venueList = venueList;
	}
}
