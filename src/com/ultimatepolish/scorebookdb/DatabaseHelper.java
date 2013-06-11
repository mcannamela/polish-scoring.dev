package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	private static final int DATABASE_VERSION = 11;

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
			Log.i("DatabaseHelper.increment_09", "Attempting to upgrade from version 09 to version 10");
			// game table
			Dao<Game, Long> gDao = getGameDao();
			String addGameColumn = "ALTER TABLE game ADD COLUMN ";
			gDao.executeRaw(addGameColumn+"isTeam BOOLEAN DEFAULT 0;");
			gDao.executeRaw(addGameColumn+"isComplete BOOLEAN DEFAULT 1;");
			gDao.executeRaw(addGameColumn+"isTracked BOOLEAN DEFAULT 1;");
			
			// player table
			Dao<Player, Long> pDao = getPlayerDao();
			pDao.executeRaw("ALTER TABLE player ADD COLUMN isActive BOOLEAN DEFAULT 1;");
			pDao.executeRaw("ALTER TABLE player ADD COLUMN prefersRightSide BOOLEAN DEFAULT 0;");
			pDao.executeRaw("ALTER TABLE player ADD COLUMN prefersRightSide BOOLEAN DEFAULT 0;");
			pDao.executeRaw("ALTER TABLE player RENAME TO temp;");
			TableUtils.createTable(connectionSource, Player.class);
			pDao.executeRaw("INSERT INTO player(id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, prefersRightSide, prefersLeftSide, height_cm, weight_kg, isActive) " +
					"SELECT id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, prefersRightSide, prefersLeftSide, height_cm, weight_kg, isActive FROM temp;");
			pDao.executeRaw("DROP TABLE temp;");

			// session table
			Dao<Session, Long> sDao = getSessionDao();
			String addSessionColumn = "ALTER TABLE session ADD COLUMN ";
			sDao.executeRaw(addSessionColumn+"sessionType INT DEFAULT 1;");
			sDao.executeRaw("UPDATE session SET sessionType = 0 WHERE sessionName = 'side_books';");
			sDao.executeRaw(addSessionColumn+"isTeam BOOLEAN DEFAULT 0;");
			sDao.executeRaw(addSessionColumn+"isActive BOOLEAN DEFAULT 1;");
			sDao.executeRaw("ALTER TABLE session RENAME TO temp;");
			TableUtils.createTable(connectionSource, Session.class);
			sDao.executeRaw("INSERT INTO session(id, sessionName, sessionType, startDate, endDate, isTeam, isActive) " +
					"SELECT id, sessionName, sessionType, startDate, endDate, isTeam, isActive FROM temp;");
			sDao.executeRaw("DROP TABLE temp;");
			
			// venue table
			Dao<Venue, Long> vDao = getVenueDao();
			String addVenueColumn ="ALTER TABLE venue ADD COLUMN "; 
			vDao.executeRaw(addVenueColumn+"longitude LONG;");
			vDao.executeRaw(addVenueColumn+"latitude LONG;");
			vDao.executeRaw(addVenueColumn+"zipCode LONG;");
			vDao.executeRaw(addVenueColumn+"isActive BOOLEAN DEFAULT 1;");
			vDao.executeRaw("ALTER TABLE venue RENAME TO temp;");
			TableUtils.createTable(connectionSource, Venue.class);
			vDao.executeRaw("INSERT INTO venue(id, venueName, scoreKeptFromTop, longitude, latitude, zipCode, isActive) " +
					"SELECT id, name, scoreKeptFromTop, longitude, latitude, zipCode, isActive FROM temp;");
			vDao.executeRaw("DROP TABLE temp;");
			
			createAll();
			
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + 9 + " to "
					+ 10, e);
		} 
	}
	
	private void increment_10(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource){
		try {
			Log.i("DatabaseHelper.increment_10", "Attempting to upgrade from version 10 to version 11");
			// throw table
			Dao<Game, Long> gDao = getGameDao();
			Dao<Throw, Long> tDao = getThrowDao();
			
			// add all the new column types. after populating the new columns, the table gets rebuilt
			
			// these are easy. tipped and linefault were not tracked at all previously.
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isTipped BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isLineFault BOOLEAN DEFAULT 0;");
			
			// populating defensivePlayerId column requires looping through all the games
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN defensivePlayerId INTEGER;");
			for(Game g:gDao){
				tDao.executeRaw("UPDATE throw SET defensivePlayerId=" + g.getFirstPlayerId() +
						" WHERE gameId=" + g.getId() + " AND playerId= " + g.getSecondPlayerId() + ";");
				tDao.executeRaw("UPDATE throw SET defensivePlayerId=" + g.getSecondPlayerId() +
						" WHERE gameId=" + g.getId() + " AND playerId= " + g.getFirstPlayerId() + ";");
			}
			
			// fireCounts will be recalculated if the game is loaded again. should find a better way to handle this 
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN offenseFireCount INTEGER DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN defenseFireCount INTEGER DEFAULT 0;");
			
			// other columns
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN deadType INTEGER DEFAULT 0;");
			tDao.executeRaw("UPDATE throw SET throwResult=3 WHERE isBroken=1;");
			
			// migrate offensive errors. drink drop and break errors werent tracked before so stay false.
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensiveDrinkDropped BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensivePoleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensiveBottleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensiveBreakError BOOLEAN DEFAULT 0;");
			
			tDao.executeRaw("UPDATE throw SET isOffensivePoleKnocked=1 WHERE ownGoalScore=2;");
			tDao.executeRaw("UPDATE throw SET isOffensiveBottleKnocked=1 WHERE ownGoalScore=3;");
			
			// migrate defensive errors.
			  // isGoaltend (db10) == > isGoaltend (db11) 
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isGrabbed BOOLEAN DEFAULT 0;");
			  // isDrinkDropped (db10) ==> isDefensiveDrinkDropped (db11)
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isDefensivePoleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isDefensiveBottleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isDefensiveBreakError BOOLEAN DEFAULT 0;");
			
			  // errorScore=0 (PCB knocked but bottle caught) no longer tracked.

			tDao.executeRaw("UPDATE throw SET isGrabbed=1 WHERE errorScore=1;");
			
			  // for errorScore=2/3, assume defense knock pole/bottle if throw was a strike. assume dead pole/bottle hit that wasnt caught if throw was HRLL
			tDao.executeRaw("UPDATE throw SET isDefensivePoleKnocked=1 WHERE errorScore=2 and throwType=3;"); // deadType to high
			tDao.executeRaw("UPDATE throw SET isDefensiveBottleKnocked=1 WHERE errorScore=3 and throwType=3;"); // deadType to high
			
			tDao.executeRaw("UPDATE throw SET deadType=1 WHERE errorScore>1 and throwType=4;"); // deadType to high
			tDao.executeRaw("UPDATE throw SET deadType=2 WHERE errorScore>1 and throwType=5;"); // deadType to right
			tDao.executeRaw("UPDATE throw SET deadType=3 WHERE errorScore>1 and throwType=6;"); // deadType to low
			tDao.executeRaw("UPDATE throw SET deadType=4 WHERE errorScore>1 and throwType=7;"); // deadType to left
			tDao.executeRaw("UPDATE throw SET throwType=2 WHERE errorScore=2 AND throwType!=3;"); // throwType to pole
			tDao.executeRaw("UPDATE throw SET throwType=0 WHERE errorScore=3 AND throwType!=3;"); // throwType to bottle
			tDao.executeRaw("UPDATE throw SET throwResult=1 WHERE errorScore>1 AND throwType!=3;"); // throwResult to dropped
			
			
			// rebuild the table and copy data over
			tDao.executeRaw("ALTER TABLE throw RENAME TO temp;");
			TableUtils.createTable(connectionSource, Throw.class);
			tDao.executeRaw("INSERT INTO throw(id, throwIdx, gameId, offensivePlayerId, defensivePlayerId, timestamp, " +
					"throwType, throwResult, deadType, isTipped, isGoaltend, isGrabbed, isDrinkHit, isLineFault, " +
					"isOffensiveDrinkDropped, isOffensivePoleKnocked, isOffensiveBottleKnocked, isOffensiveBreakError, " +
					"isDefensiveDrinkDropped, isDefensivePoleKnocked, isDefensiveBottleKnocked, isDefensiveBreakError, " +
					"offenseFireCount, defenseFireCount, initialOffensivePlayerScore, initialDefensivePlayerScore) " +
					"SELECT id, throwNumber, gameId, playerId, defensivePlayerId, timestamp, " +
					"throwType, throwResult, deadType, isTipped, isGoaltend, isGrabbed, isDrinkHit, isLineFault, " +
					"isOffensiveDrinkDropped, isOffensivePoleKnocked, isOffensiveBottleKnocked, isOffensiveBreakError, " +
					"isDrinkDropped, isDefensivePoleKnocked, isDefensiveBottleKnocked, isDefensiveBreakError, " +
					"offenseFireCount, defenseFireCount, initialOffensivePlayerScore, initialDefensivePlayerScore FROM temp;");
			tDao.executeRaw("DROP TABLE temp;");
			

		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + 10 + " to "
					+ 11, e);
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
			TableUtils.createTableIfNotExists(connectionSource, c);
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
