package com.ultimatepolish.scorebookdb;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Badge{
	
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField
	private long playerId; // could be a team
	
	@DatabaseField
	private boolean isTeam;
	
	@DatabaseField
	private long sessionId;
	
	@DatabaseField(canBeNull=false)
	private int badgeType;
	
	@DatabaseField
	private long drawableId;
	
	Badge(){}

	public Badge(long playerId, boolean isTeam, long sessionId, int badgeType) {
		super();
		this.playerId = playerId;
		this.isTeam = isTeam;
		this.sessionId = sessionId;
		this.badgeType = badgeType;
	}
	
	public Badge(long playerId, boolean isTeam, int badgeType) {
		super();
		this.playerId = id;
		this.isTeam = isTeam;
		this.badgeType = badgeType;
	}
	
	public static Dao<Badge, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Badge, Long> d = helper.getBadgeDao();
		return d;
	}
		
	public static List<Badge> getAll(Context context) throws SQLException{
		Dao<Badge, Long> d = Badge.getDao(context);
		List<Badge> badges = new ArrayList<Badge>();
		for(Badge b:d){
			badges.add(b);
		}
		return badges;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	
	public boolean getIsTeam() {
		return isTeam;
	}

//	public void setIsTeam(boolean isTeam) {
//		this.isTeam = isTeam;
//	}
	
	public long getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
}
