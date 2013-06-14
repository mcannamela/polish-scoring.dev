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
	
	@DatabaseField(canBeNull=false)
	private long firstPlayer_id;
	
	@DatabaseField(canBeNull=false)
	private long secondPlayer_id;
	
	@DatabaseField(canBeNull=false)
	public boolean firstPlayerOnTop;
	
	@DatabaseField(foreign = true)
	private Session session;
	
	@DatabaseField(foreign = true)
	private Venue venue;
	
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

	public Game(long firstPlayerId, long secondPlayerId, Session session,
			Venue venue, boolean isTeam, boolean isTracked, Date datePlayed) {
		super();
		this.firstPlayer_id = firstPlayerId;
		this.secondPlayer_id = secondPlayerId;
		this.session = session;
		this.venue = venue;
		this.isTeam = isTeam;
		this.isTracked = isTracked;
		this.datePlayed = datePlayed;
		
	}
	
	public Game(long firstPlayerId, long secondPlayerId, Session session,
			Venue venue, boolean isTeam, boolean isTracked) {
		super();
		this.firstPlayer_id = firstPlayerId;
		this.secondPlayer_id = secondPlayerId;
		this.session = session;
		this.venue = venue;
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
	public static List<Game> getAll(Context context) throws SQLException{
		Dao<Game, Long> d = Game.getDao(context);
		List<Game> games = new ArrayList<Game>();
		for(Game g:d){
			games.add(g);
		}
		return games;
	}
	public boolean isValidThrow(Throw t){
		boolean isValid = true;
		int idx = t.getThrowIdx();
		switch (idx%2){
		    //first player is on offense
			case 0:
				isValid= isValid && (t.getOffensivePlayerId()==firstPlayer_id);
				break;
		    //second player is on defense
			case 1:
				isValid= isValid && (t.getOffensivePlayerId()==secondPlayer_id);
				break;
			default:
				throw new RuntimeException("invalid index "+idx);
		}
		return isValid;
	}
	public Player[] getPlayers(Context context) throws SQLException{
		Player[] players = new Player[2]; 
		Dao<Player, Long> d = Player.getDao(context);
		players[0] = d.queryForId(firstPlayer_id);
		players[1] = d.queryForId(secondPlayer_id);
		
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
		long offensivePlayerId, defensivePlayerId;
		if (throwNumber%2 == 0){
			offensivePlayerId = getFirstPlayerId();
			defensivePlayerId = getSecondPlayerId();
		}
		else{
			offensivePlayerId = getSecondPlayerId();
			defensivePlayerId = getFirstPlayerId();
		}
		Date timestamp = new Date(System.currentTimeMillis());
		Throw t = new Throw (throwNumber, getId(), offensivePlayerId, defensivePlayerId, timestamp);
		
		return t;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFirstPlayerId() {
		return firstPlayer_id;
	}

	public void setFirstPlayerId(long firstPlayerId) {
		this.firstPlayer_id = firstPlayerId;
	}

	public long getSecondPlayerId() {
		return secondPlayer_id;
	}

	public void setSecondPlayerId(long secondPlayerId) {
		this.secondPlayer_id = secondPlayerId;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
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
