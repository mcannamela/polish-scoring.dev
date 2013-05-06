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
public class Team{
	public static final String TEAM_NAME = "teamName";
	
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(columnName=TEAM_NAME, canBeNull=false)
	private String teamName;
	
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private long firstPlayerId;
	
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private long secondPlayerId;
	
	@DatabaseField
	private int  nWins = 0;
	
	@DatabaseField
	private int  nLosses = 0;
	
	@DatabaseField
	private long drawableId;
	
	@DatabaseField
	private boolean isActive = true;
	
	Team(){}

	public Team(  String teamName, 
					long firstPlayerId,
					long secondPlayerId) {
		super();
		this.teamName = teamName;
		this.firstPlayerId = firstPlayerId;
		this.secondPlayerId = secondPlayerId;
	}
	
	public static Dao<Team, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Team, Long> d = helper.getTeamDao();
		return d;
	}
	
	public static long getIdByPlayers(Player p1, Player p2, Context context) throws SQLException{
		Team p = getByPlayers(p1, p2, context);
		if (p==null){
			return -1;
		}
		return p.getId();
	}
	
	public static Team getByPlayers(Player p1, Player p2, Context context) throws SQLException{
		List<Team> teamList = null;
//		HashMap<String, Object> m = buildNameMap(p1, p2);
//		
//		teamList = getDao(context).queryForFieldValuesArgs(m);
//		if (teamList.isEmpty()){
			return null;
//		}
//		else{
//			return teamList.get(0);
//		}
	}
	
	public Player[] getPlayers(Context context) throws SQLException{
		Player[] players = new Player[2]; 
		Dao<Player, Long> d = Player.getDao(context);
		players[0] = d.queryForId(firstPlayerId);
		players[1] = d.queryForId(secondPlayerId);
		
		return players;
	}
	
	public static boolean exists(String teamName, Context context) throws SQLException{
		if (teamName == null){
			return false;
		}
		List<Team> teamList = null;
//		HashMap<String,Object> m = buildNameMap(teamName);
//		
//		teamList = getDao(context).queryForFieldValuesArgs(m);
//		if (teamList.isEmpty()){
			return false;
//		}
//		else{
//			return true;
//		}
	}
	
	public boolean exists(Context context) throws SQLException{
		return exists(teamName, context);
	}
		
	public static List<Team> getAll(Context context) throws SQLException{
		Dao<Team, Long> d = Team.getDao(context);
		List<Team> teams = new ArrayList<Team>();
		for(Team t:d){
			teams.add(t);
		}
		return teams;
	}
	
//	public static HashMap<String,Object> buildPlayerMap(Player p1, Player p2){
//		HashMap<long, 1, Object> m = new HashMap<long, Object>();
//		m.put(FIRST_NAME, first.toLowerCase(Locale.US));
//		m.put(LAST_NAME, last.toLowerCase(Locale.US));
//		m.put(NICK_NAME, nick.toLowerCase(Locale.US));
//		return m;
//	}
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public int getnWins() {
		return nWins;
	}

	public void setnWins(int nWins) {
		this.nWins = nWins;
	}

	public int getnLosses() {
		return nLosses;
	}

	public void setnLosses(int nLosses) {
		this.nLosses = nLosses;
	}
	
	public long getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(long drawableId) {
		this.drawableId = drawableId;
	}
	
	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	
}
