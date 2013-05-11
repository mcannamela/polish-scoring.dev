package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Game {
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private long firstPlayerId;
	
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private long secondPlayerId;
	
	@DatabaseField(canBeNull=false)
	public boolean firstPlayerOnTop;
	
	@DatabaseField
	private long sessionId;
	
	@DatabaseField
	private long venueId;
	
	@DatabaseField(canBeNull=false)
	private Date datePlayed;
	
	@DatabaseField
	private int firstPlayerScore;
	
	@DatabaseField
	private int secondPlayerScore;

	@DatabaseField
	private boolean isTeam;
	
	@DatabaseField
	private boolean isComplete = false;
	
	@DatabaseField
	private boolean isTracked = true;
	
	public Game() {
		super();
	}

	public Game(long firstPlayerId, long secondPlayerId, long sessionId,
			long venueId, boolean isTeam, boolean isTracked, Date datePlayed) {
		super();
		this.firstPlayerId = firstPlayerId;
		this.secondPlayerId = secondPlayerId;
		this.sessionId = sessionId;
		this.venueId = venueId;
		this.isTeam = isTeam;
		this.isTracked = isTracked;
		this.datePlayed = datePlayed;
		
	}
	
	public Game(long firstPlayerId, long secondPlayerId, long sessionId,
			long venueId, boolean isTeam, boolean isTracked) {
		super();
		this.firstPlayerId = firstPlayerId;
		this.secondPlayerId = secondPlayerId;
		this.sessionId = sessionId;
		this.venueId = venueId;
		this.isTeam = isTeam;
		this.isTracked = isTracked;
		this.datePlayed = new Date();
	}

	public static Dao<Game, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Game, Long> d = null;
		try {
			d = helper.getGameDao();
		}
		catch (SQLException e){
			throw new RuntimeException("couldn't get dao: ", e);
		}
		return d;
	}
	public boolean isValidThrow(Throw t){
		boolean isValid = true;
		int idx = t.getThrowIdx();
		switch (idx%2){
		    //first player is on offense
			case 0:
				isValid= isValid && (t.getOffensivePlayerId()==firstPlayerId);
				break;
		    //second player is on defense
			case 1:
				isValid= isValid && (t.getOffensivePlayerId()==secondPlayerId);
				break;
			default:
				throw new RuntimeException("invalid index "+idx);
		}
		return isValid;
	}
	public Player[] getPlayers(Context context) throws SQLException{
		Player[] players = new Player[2]; 
		Dao<Player, Long> d = Player.getDao(context);
		players[0] = d.queryForId(firstPlayerId);
		players[1] = d.queryForId(secondPlayerId);
		
		return players;
	}
	
	public ArrayList<Throw> getThrowList(Context context) throws SQLException{
		int tidx, maxThrowIndex;
		ArrayList<Throw> throwArray = new ArrayList<Throw>();
		
		HashMap<Integer, Throw> throwMap = new HashMap<Integer, Throw>();
		HashMap<String,Object> m = new HashMap<String,Object>();
		m.put("gameId", getId());
		
		Dao<Throw, Long> d = Throw.getDao(context);		
		List<Throw> dbThrows = d.queryForFieldValuesArgs(m);
		
		maxThrowIndex = 0;
		if (!dbThrows.isEmpty()) {
			Collections.sort(dbThrows);
			
			for (Throw t:dbThrows){
				tidx = t.getThrowIdx();
				
				//purge any throws with negative index
				if (tidx<0){
					d.delete(t);
				}
				
				//populate the map
				throwMap.put(tidx, t);
				
				//keep track of the maximum index
				if (tidx>maxThrowIndex){
					maxThrowIndex=tidx;
				}
			}
			
			//ensure throws in correct order and complete
			Throw t = null;
			for (int i=0;i<=maxThrowIndex;i++){
				t = throwMap.get(i);
				//infill with a caught strike if necessary
				if (t==null){
					t = makeNewThrow(i);
					t.setThrowType(ThrowType.STRIKE);
					t.setThrowResult(ThrowResult.CATCH);
				}
				throwArray.add(t);
			}
		}
		
		return throwArray;
	}
	public Throw makeNewThrow(int throwNumber){
		long playerId;
		if (throwNumber%2 == 0){
			playerId=getFirstPlayerId();
		}
		else{
			playerId=getSecondPlayerId();
		}
		Date timestamp = new Date(System.currentTimeMillis());
		Throw t = new Throw (throwNumber, getId(), playerId, timestamp);
		
		return t;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFirstPlayerId() {
		return firstPlayerId;
	}

	public void setFirstPlayerId(long firstPlayerId) {
		this.firstPlayerId = firstPlayerId;
	}

	public long getSecondPlayerId() {
		return secondPlayerId;
	}

	public void setSecondPlayerId(long secondPlayerId) {
		this.secondPlayerId = secondPlayerId;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public long getVenueId() {
		return venueId;
	}

	public void setVenueId(long venueId) {
		this.venueId = venueId;
	}

	public Date getDatePlayed() {
		return datePlayed;
	}

	public void setDatePlayed(Date datePlayed) {
		this.datePlayed = datePlayed;
	}

	public int getFirstPlayerScore() {
		return firstPlayerScore;
	}

	public void setFirstPlayerScore(int firstPlayerScore) {
		this.firstPlayerScore = firstPlayerScore;
	}

	public int getSecondPlayerScore() {
		return secondPlayerScore;
	}

	public void setSecondPlayerScore(int secondPlayerScore) {
		this.secondPlayerScore = secondPlayerScore;
	}

	public Session getSession(Context context) {
		try{
			Dao<Session, Long> d = Session.getDao(context);
			return d.queryForId(getSessionId());
		}
		catch (SQLException e){
			return null;
		}
		
	}

	public Venue getVenue(Context context) {
		try{
			Dao<Venue, Long> d = Venue.getDao(context);
			return d.queryForId(getVenueId());
		}
		catch (SQLException e){
			return null;
		}
	}
	
	public boolean getIsTeam() {
		return isTeam;
	}
	
	public boolean getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public boolean getIsTracked() {
		return isTracked;
	}
}
