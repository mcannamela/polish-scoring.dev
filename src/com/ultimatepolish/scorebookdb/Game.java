package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.Date;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Game {
	@DatabaseField(generatedId=true)
	private long id;
	@DatabaseField(canBeNull=false)
	private long firstPlayerId;
	@DatabaseField(canBeNull=false)
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

	public Game() {
		super();
	}

	public Game(long firstPlayerId, long secondPlayerId, long sessionId,
			long venueId, Date datePlayed) {
		super();
		this.firstPlayerId = firstPlayerId;
		this.secondPlayerId = secondPlayerId;
		this.sessionId = sessionId;
		this.venueId = venueId;
		this.datePlayed = datePlayed;
	}
	
	public Game(long firstPlayerId, long secondPlayerId, long sessionId, long venueId) {
		super();
		this.firstPlayerId = firstPlayerId;
		this.secondPlayerId = secondPlayerId;
		this.sessionId = sessionId;
		this.venueId = venueId;
		this.datePlayed = new Date();
	}

	public static Dao<Game, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Game, Long> d = helper.getGameDao();
		return d;
	}
	
	public Player[] getPlayers(Context context) throws SQLException{
		Player[] players = new Player[2]; 
		Dao<Player, Long> d = Player.getDao(context);
		players[0] = d.queryForId(firstPlayerId);
		players[1] = d.queryForId(secondPlayerId);
		
		return players;
	}
	public Throw makeNewThrow(int throwNumber){
		long playerId;
		if ((throwNumber-1) % 2 == 0){
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
}
