package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
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
	
	private List<Class> tableClasses = new ArrayList<Class>();

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
		Log.i("DatabaseHelper.onCreate()", "Attempting to create db");
		try {
			createAll(connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create database", e);
		}
	}

	@Override
	public void onUpgrade(final SQLiteDatabase sqliteDatabase, final ConnectionSource connectionSource, int oldVer, final int newVer) {
		Log.i("DatabaseHelper.onUpgrade()", "Attempting to upgrade from version " + oldVer + " to version " + newVer);

//		DatabaseUpgrader dbUp = new DatabaseUpgrader();
		switch (oldVer){
			case 9:
//				dbUp.increment_09(sqliteDatabase, oldVer, newVer, connectionSource);
				increment_09(sqliteDatabase, connectionSource);
				
			case 10:
				increment_10(sqliteDatabase, connectionSource);
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
	
	private void increment_09(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		try {
			// game table
			Dao<Game, Long> gDao = getGameDao();
			gDao.executeRaw("ALTER TABLE game ADD COLUMN isTeam BOOLEAN DEFAULT 0;");
			gDao.executeRaw("ALTER TABLE game ADD COLUMN isComplete BOOLEAN DEFAULT 1;");
			gDao.executeRaw("ALTER TABLE game ADD COLUMN isTracked BOOLEAN DEFAULT 1;");
			
			// player table
			Dao<Player, Long> pDao = getPlayerDao();
			pDao.executeRaw("ALTER TABLE player ADD COLUMN isActive BOOLEAN DEFAULT 1;");
			pDao.executeRaw("ALTER TABLE player RENAME TO temp;");
			TableUtils.createTable(connectionSource, Player.class);
			pDao.executeRaw("INSERT INTO player(id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, height_cm, weight_kg, isActive) " +
					"SELECT id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, height_cm, weight_kg, isActive FROM temp;");
			pDao.executeRaw("DROP TABLE temp;");

			// session table
			Dao<Session, Long> sDao = getSessionDao();
			sDao.executeRaw("ALTER TABLE session ADD COLUMN sessionType INT DEFAULT 1;");
			sDao.executeRaw("UPDATE session SET sessionType = 0 WHERE sessionName = 'side_books';");
			sDao.executeRaw("ALTER TABLE session ADD COLUMN isTeam BOOLEAN DEFAULT 0;");
			sDao.executeRaw("ALTER TABLE session ADD COLUMN isActive BOOLEAN DEFAULT 1;");
			sDao.executeRaw("ALTER TABLE session RENAME TO temp;");
			TableUtils.createTable(connectionSource, Session.class);
			sDao.executeRaw("INSERT INTO session(id, sessionName, sessionType, startDate, endDate, isTeam, isActive) " +
					"SELECT id, sessionName, sessionType, startDate, endDate, isTeam, isActive FROM temp;");
			sDao.executeRaw("DROP TABLE temp;");
			
			// venue table
			Dao<Venue, Long> vDao = getVenueDao();
			vDao.executeRaw("ALTER TABLE venue ADD COLUMN longitude LONG;");
			vDao.executeRaw("ALTER TABLE venue ADD COLUMN latitude LONG;");
			vDao.executeRaw("ALTER TABLE venue ADD COLUMN zipCode LONG;");
			vDao.executeRaw("ALTER TABLE venue ADD COLUMN isActive BOOLEAN DEFAULT 1;");
			vDao.executeRaw("ALTER TABLE venue RENAME TO temp;");
			TableUtils.createTable(connectionSource, Venue.class);
			vDao.executeRaw("INSERT INTO venue(id, venueName, scoreKeptFromTop, longitude, latitude, zipCode, isActive) " +
					"SELECT id, name, scoreKeptFromTop, longitude, latitude, zipCode, isActive FROM temp;");
			vDao.executeRaw("DROP TABLE temp;");
			
			// new tables
			// if createAll() is modified to only create when the table doesnt exist, 
			// this could be replaced with a simple call to createAll and wouldnt remove
			// items from tableClasses (although i dont think that causes any problems).
			tableClasses.clear();
			tableClasses.add( PlayerStats.class);
			tableClasses.add( Team.class);
			tableClasses.add( TeamStats.class);
			tableClasses.add( Badge.class);
			tableClasses.add( SessionMember.class);
			
			for(Class c:tableClasses){
				TableUtils.createTable(connectionSource, c);
			}
			
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + 9 + " to "
					+ 10, e);
		}
	}
	
	private void increment_10(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		try {
			// throw table
			Dao<Throw, Long> tDao = getThrowDao();
//			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isTeam BOOLEAN DEFAULT 0;");
//			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isComplete BOOLEAN DEFAULT 1;");
//			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isTracked BOOLEAN DEFAULT 1;");
			tDao.executeRaw("ALTER TABLE throw RENAME TO temp;");
			TableUtils.createTable(connectionSource, Throw.class);
			tDao.executeRaw("INSERT INTO player(id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, height_cm, weight_kg, isActive) " +
					"SELECT id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, height_cm, weight_kg, isActive FROM temp;");
			tDao.executeRaw("DROP TABLE temp;");
			

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
			// change to CreateTableIfNotExists?
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
