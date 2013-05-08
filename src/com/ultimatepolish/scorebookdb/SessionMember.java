package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class SessionMember {
	@DatabaseField(canBeNull=false,uniqueCombo=true)
	private long sessionId;
	
	@DatabaseField(canBeNull=false,uniqueCombo=true)
	private long playerId; // could be a team
	
	@DatabaseField(canBeNull=false)
	private int playerSeed;
	
	@DatabaseField(canBeNull=false)
	private int playerRank;
	
	// would be nice to force both seed and rank to be unique for a given session
	// but i dont think it is possible to have multiple independent uniqueCombos
	// will just have to handle carefully elsewhere?
	
	public SessionMember(){}

	public SessionMember(long sessionId, long playerId, int playerSeed) {
		super();
		this.sessionId = sessionId;
		this.playerId = playerId;
		this.playerSeed = playerSeed;
		this.playerRank = playerSeed;
	}
	
	public static Dao<SessionMember, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<SessionMember, Long> d = helper.getSessionMemberDao();
		return d;
	}
	
	public static List<SessionMember> getAll(Context context) throws SQLException{
		Dao<SessionMember, Long> d = SessionMember.getDao(context);
		List<SessionMember> sessionMembers = new ArrayList<SessionMember>();
		for(SessionMember s:d){
			sessionMembers.add(s);
		}
		return sessionMembers;
	}

	public long getSessionId() {
		return sessionId;
	}

//	public void setSessionId(long sessionId) {
//		this.sessionId = sessionId;
//	}

	public long getPlayerId() {
		return playerId;
	}

//	public void setPlayerId(long playerId) {
//		this.playerId = playerId;
//	}
	
	public int getPlayerSeed() {
		return playerSeed;
	}

//	public void setPlayerSeed(int playerSeed) {
//		this.playerSeed = playerSeed;
//	}
	
	public int getPlayerRank() {
		return playerRank;
	}

	public void setPlayerRank(int playerRank) {
		this.playerRank = playerRank;
	}
}
