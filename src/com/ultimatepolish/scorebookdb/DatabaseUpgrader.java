package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseUpgrader {
	public static void increment_09( 
			ConnectionSource connectionSource,
			Dao<Game, Long> gDao,
			Dao<Player, Long> pDao,
			Dao<Session, Long> sDao,
			Dao<Venue, Long> vDao) throws SQLException{
	
		String gameUpdateQuery = "BEGIN TRANSACTION;";
		String playerUpdateQuery = "BEGIN TRANSACTION;";
		String sessionUpdateQuery = "BEGIN TRANSACTION;";
		String venueUpdateQuery = "BEGIN TRANSACTION;";
		
		// game table
		String addGameColumn = "ALTER TABLE game ADD COLUMN ";
		
		gameUpdateQuery+= addGameColumn+"isTeam BOOLEAN DEFAULT 0;";
		gameUpdateQuery+= addGameColumn+"isComplete BOOLEAN DEFAULT 1;";
		gameUpdateQuery+= addGameColumn+"isTracked BOOLEAN DEFAULT 1;";
		gameUpdateQuery+= "END TRANSACTION";
		
		gDao.executeRaw(gameUpdateQuery);
		
		// player table
		pDao.executeRaw("ALTER TABLE player RENAME TO ptemp;");
		TableUtils.createTable(connectionSource, Player.class);
		
//		pDao.executeRaw("ALTER TABLE player ADD COLUMN isActive BOOLEAN DEFAULT 1;");
		
		playerUpdateQuery+="INSERT INTO player (id, firstName, lastName, nickName, "+
		                	"throwsRightHanded, throwsLeftHanded, height_cm, weight_kg) " +
						"SELECT id, firstName, lastName, nickName, throwsRightHanded, "+
		                	"throwsLeftHanded, height_cm, weight_kg, FROM ptemp;";
		playerUpdateQuery+="DROP TABLE ptemp;";
    	playerUpdateQuery+="END TRANSACTION;";

		// session table
//		String addSessionColumn = "ALTER TABLE session ADD COLUMN ";
		
		sDao.executeRaw("ALTER TABLE session RENAME TO stemp;");
		TableUtils.createTable(connectionSource, Session.class);
		
		/*sDao.executeRaw(addSessionColumn+"sessionType INT DEFAULT 1;");
		sDao.executeRaw(addSessionColumn+"isTeam BOOLEAN DEFAULT 0;");
		sDao.executeRaw(addSessionColumn+"isActive BOOLEAN DEFAULT 1;");*/
		
		
		sessionUpdateQuery+="INSERT INTO session(id, sessionName,  startDate, "+
							"endDate) " +
							"SELECT id, sessionName, startDate, endDate, " +
							" FROM stemp;";
		sessionUpdateQuery+= "UPDATE session SET sessionType = 0 WHERE sessionName = 'side_books';";
		sessionUpdateQuery+= "DROP TABLE stemp;";
		sessionUpdateQuery+= "END TRANSACTION;";
		sDao.executeRaw(sessionUpdateQuery);
		
		// venue table
		String addVenueColumn ="ALTER TABLE venue ADD COLUMN "; 
		
		vDao.executeRaw("ALTER TABLE venue RENAME TO vtemp;");
		TableUtils.createTable(connectionSource, Venue.class);
		
		/*vDao.executeRaw(addVenueColumn+"longitude LONG;");
		vDao.executeRaw(addVenueColumn+"latitude LONG;");
		vDao.executeRaw(addVenueColumn+"zipCode LONG;");
		vDao.executeRaw(addVenueColumn+"isActive BOOLEAN DEFAULT 1;");*/
		
		venueUpdateQuery+="INSERT INTO venue(id, venueName, scoreKeptFromTop) " +
				"SELECT id, name, scoreKeptFromTop FROM vtemp;";
		venueUpdateQuery+="DROP TABLE vtemp;";
		venueUpdateQuery+="END TRANSACTION;";
		vDao.executeRaw(venueUpdateQuery);
	}
	
	public static void increment_10( 
			ConnectionSource connectionSource,
			Dao<Game, Long> gDao,
			Dao<Throw, Long> tDao 
			) throws SQLException{
		// add all the new column types. after populating the new columns, the table gets rebuilt
		
		//reuse common strings where possible
		String addThrowColumn = "ALTER TABLE throw ADD COLUMN ";
		String updateThrowSet = "UPDATE throw SET ";
		String booleanDefault0 = "BOOLEAN DEFAULT 0;";
		
		String throwUpdateQuery = "BEGIN TRANSACTION;";
		
		// these are easy. tipped and linefault were not tracked at all previously.
		throwUpdateQuery+= addThrowColumn+" isTipped "+booleanDefault0;
		throwUpdateQuery+= addThrowColumn+" isLineFault "+booleanDefault0;
		
		// populating defensivePlayerId column requires looping through all the games
		throwUpdateQuery+= addThrowColumn+" defensivePlayerId INTEGER;";
		for(Game g:gDao){
			throwUpdateQuery+= updateThrowSet+" defensivePlayerId=" + g.getFirstPlayerId() +
					" WHERE gameId=" + g.getId() + " AND playerId= " + g.getSecondPlayerId() + ";";
			throwUpdateQuery+= updateThrowSet+" defensivePlayerId=" + g.getSecondPlayerId() +
					" WHERE gameId=" + g.getId() + " AND playerId= " + g.getFirstPlayerId() + ";";
		}
		
		// fireCounts will be recalculated if the game is loaded again. should find a better way to handle this 
		throwUpdateQuery+= addThrowColumn+" offenseFireCount INTEGER DEFAULT 0;";
		throwUpdateQuery+= addThrowColumn+" defenseFireCount INTEGER DEFAULT 0;";
		
		// other columns
		throwUpdateQuery+= addThrowColumn+" deadType INTEGER DEFAULT 0;";
		throwUpdateQuery+= updateThrowSet+" throwResult=3 WHERE isBroken=1;";
		
		// migrate offensive errors. drink drop and break errors werent tracked before so stay false.
		throwUpdateQuery+= addThrowColumn+" isOffensiveDrinkDropped "+booleanDefault0;
		throwUpdateQuery+= addThrowColumn+" isOffensivePoleKnocked "+booleanDefault0;
		throwUpdateQuery+= addThrowColumn+" isOffensiveBottleKnocked "+booleanDefault0;
		throwUpdateQuery+= addThrowColumn+" isOffensiveBreakError "+booleanDefault0;
		
		throwUpdateQuery+= updateThrowSet+" isOffensivePoleKnocked=1 WHERE ownGoalScore=2;";
		throwUpdateQuery+= updateThrowSet+" isOffensiveBottleKnocked=1 WHERE ownGoalScore=3;";
		
		// migrate defensive errors.
		  // isGoaltend (db10) == > isGoaltend (db11) 
		throwUpdateQuery+= addThrowColumn+" isGrabbed "+booleanDefault0;
		  // isDrinkDropped (db10) ==> isDefensiveDrinkDropped (db11)
		throwUpdateQuery+= addThrowColumn+" isDefensivePoleKnocked "+booleanDefault0;
		throwUpdateQuery+= addThrowColumn+" isDefensiveBottleKnocked "+booleanDefault0;
		throwUpdateQuery+= addThrowColumn+" isDefensiveBreakError "+booleanDefault0;
		
		  // errorScore=0 (PCB knocked but bottle caught) no longer tracked.

		throwUpdateQuery+= updateThrowSet+" isGrabbed=1 WHERE errorScore=1;";
		
		  // for errorScore=2/3, assume defense knock pole/bottle if throw was a strike. assume dead pole/bottle hit that wasnt caught if throw was HRLL
		throwUpdateQuery+= updateThrowSet+" isDefensivePoleKnocked=1 WHERE errorScore=2 and throwType=3;"; // deadType to high
		throwUpdateQuery+= updateThrowSet+" isDefensiveBottleKnocked=1 WHERE errorScore=3 and throwType=3;"; // deadType to high
		
		throwUpdateQuery+= updateThrowSet+" deadType=1 WHERE errorScore>1 and throwType=4;"; // deadType to high
		throwUpdateQuery+= updateThrowSet+" deadType=2 WHERE errorScore>1 and throwType=5;"; // deadType to right
		throwUpdateQuery+= updateThrowSet+" deadType=3 WHERE errorScore>1 and throwType=6;"; // deadType to low
		throwUpdateQuery+= updateThrowSet+" deadType=4 WHERE errorScore>1 and throwType=7;"; // deadType to left
		throwUpdateQuery+= updateThrowSet+" throwType=2 WHERE errorScore=2 AND throwType!=3;"; // throwType to pole
		throwUpdateQuery+= updateThrowSet+" throwType=0 WHERE errorScore=3 AND throwType!=3;"; // throwType to bottle
		throwUpdateQuery+= updateThrowSet+" throwResult=1 WHERE errorScore>1 AND throwType!=3;"; // throwResult to dropped
		throwUpdateQuery+= "END TRANSACTION";
		
		tDao.executeRaw(throwUpdateQuery);
		
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
	}
}
