package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Venue {
	public static final String VENUE_NAME = "name";
	
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(canBeNull=false, unique=true, columnName=VENUE_NAME)
	private String name;
	
	@DatabaseField(canBeNull=true)
	public boolean scoreKeptFromTop = true;
	
	Venue(){}

	public Venue(String name, boolean scoreKeptFromTop) {
		super();
		this.name = name;
		this.scoreKeptFromTop = scoreKeptFromTop;
	}
	public static Dao<Venue, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Venue, Long> d = helper.getVenueDao();
		return d;
	}
	public static ArrayList<Venue> getAll(Context context) throws SQLException{
		Dao<Venue, Long> d = Venue.getDao(context);
		ArrayList<Venue> venues = new ArrayList<Venue>();
		for(Venue v:d){
			venues.add(v);
		}
		return venues;
	}

	public Venue(String name) {
		super();
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean exists(Context context) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	
}
