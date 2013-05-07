package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ultimatepolish.polishscorebook.R;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "polish.db";
	private static final int DATABASE_VERSION = 10;

	private Dao<Player, Long> playerDao;
	private Dao<PlayerStats, Long> playerStatsDao;
	private Dao<Team, Long> teamDao;
	private Dao<TeamStats, Long> teamStatsDao;
	private Dao<Badge, Long> badgeDao;
	private Dao<Game, Long> gameDao;
	private Dao<Session, Long> sessionDao;
	private Dao<SessionMember, Long> sessionMemberDao;
	private Dao<Throw, Long> throwDao;
	private Dao<Venue, Long> venueDao;
	
	private ArrayList<Class> tableClasses = new ArrayList<Class>();

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
		tableClasses.add( Player.class);
		tableClasses.add( PlayerStats.class);
		tableClasses.add( Team.class);
		tableClasses.add( TeamStats.class);
		tableClasses.add( Badge.class);
		tableClasses.add( Game.class);
		tableClasses.add( Session.class);
		tableClasses.add( SessionMember.class);
		tableClasses.add( Throw.class);
		tableClasses.add( Venue.class);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			createAll(connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create database", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		switch (oldVer){
			case 9:
				upgrade_09_10(sqliteDatabase, connectionSource);
			case 10:
				break;
			default:
				try {
					dropAll(connectionSource);
					createAll(connectionSource);
				} catch (SQLException e) {
					Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to "
							+ newVer, e);
				}
		}
		
	}
	
	private void upgrade_09_10(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		//TODO: actual db migration goes here
		try {
			dropAll(connectionSource);
			createAll(connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + 9 + " to "
					+ 10, e);
		}
	}
	
	
	
	
	public void createAll(){
		try{
			createAll(getConnectionSource());
		}
		catch (SQLException e){
			Log.e(DatabaseHelper.class.toString(), e.getMessage());
			throw new RuntimeException("could not create tables",e);
		}
	}
	public void dropAll(){
		try{
			dropAll(getConnectionSource());
		}
		catch (SQLException e){
			Log.e(DatabaseHelper.class.toString(), e.getMessage());
			throw new RuntimeException("could not drop tables",e);
		}
	}
	protected void createAll(ConnectionSource connectionSource) throws SQLException{
		for(Class c:tableClasses){
			TableUtils.createTable(connectionSource, c);
		}
	}
	
	protected void dropAll(ConnectionSource connectionSource) throws SQLException{
		for(Class c:tableClasses){
			TableUtils.dropTable(connectionSource, c, true);
		}
	}

	public Dao<Player, Long> getPlayerDao() throws SQLException {
		if (playerDao == null) {
			playerDao = getDao(Player.class);
		}
		return playerDao;
	}
	
	public Dao<PlayerStats, Long> getPlayerStatsDao() throws SQLException {
		if (playerStatsDao == null) {
			playerStatsDao = getDao(PlayerStats.class);
		}
		return playerStatsDao;
	}
	
	public Dao<Team, Long> getTeamDao() throws SQLException {
		if (teamDao == null) {
			teamDao = getDao(Team.class);
		}
		return teamDao;
	}
	
	public Dao<TeamStats, Long> getTeamStatsDao() throws SQLException {
		if (teamStatsDao == null) {
			teamStatsDao = getDao(TeamStats.class);
		}
		return teamStatsDao;
	}
	
	public Dao<Badge, Long> getBadgeDao() throws SQLException {
		if (badgeDao == null) {
			badgeDao = getDao(Badge.class);
		}
		return badgeDao;
	}

	public Dao<Game, Long> getGameDao() throws SQLException {
		if (gameDao == null) {
			gameDao = getDao(Game.class);
		}
		return gameDao;
	}
	public Dao<Session, Long> getSessionDao() throws SQLException {
		if (sessionDao == null) {
			sessionDao = getDao(Session.class);
		}
		return sessionDao;
	}
	public Dao<SessionMember, Long> getSessionMemberDao() throws SQLException {
		if (sessionMemberDao == null) {
			sessionMemberDao = getDao(SessionMember.class);
		}
		return sessionMemberDao;
	}
	public Dao<Throw, Long> getThrowDao() throws SQLException {
		if (throwDao == null) {
			throwDao = getDao(Throw.class);
		}
		return throwDao;
	}
	public Dao<Venue, Long> getVenueDao() throws SQLException {
		if (venueDao == null) {
			venueDao = getDao(Venue.class);
		}
		return venueDao;
	}
}
