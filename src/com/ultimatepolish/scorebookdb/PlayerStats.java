package com.ultimatepolish.scorebookdb;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerStats{
	@DatabaseField(unique=true)
	private long playerId;
	
	@DatabaseField
	public int nWins;
	
	@DatabaseField
	private int nLosses;
	
	PlayerStats(){}

	public PlayerStats(long playerId) {
		super();
	}
	
	public static Dao<PlayerStats, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<PlayerStats, Long> d = helper.getPlayerStatsDao();
		return d;
	}

	public static List<PlayerStats> getAll(Context context) throws SQLException{
		Dao<PlayerStats, Long> d = PlayerStats.getDao(context);
		List<PlayerStats> playersStats = new ArrayList<PlayerStats>();
		for(PlayerStats p:d){
			playersStats.add(p);
		}
		return playersStats;
	}

	public long getPlayerId() {
		return playerId;
	}

	public int getnGames() {
		return nWins + nLosses;
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
	
}
