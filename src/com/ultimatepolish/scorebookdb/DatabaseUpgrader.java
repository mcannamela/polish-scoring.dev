package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseUpgrader {
	public static void increment_09( 
			ConnectionSource connectionSource,
			Dao<Game, Long> gDao,
			Dao<Player, Long> pDao,
			Dao<Session, Long> sDao,
			Dao<Venue, Long> vDao,
			Dao<Throw, Long> tDao) throws SQLException{
	
		String addGameColumn = "ALTER TABLE game ADD COLUMN ";
		gDao.executeRaw(addGameColumn+"isTeam BOOLEAN DEFAULT 0;");
		gDao.executeRaw(addGameColumn+"isComplete BOOLEAN DEFAULT 1;");
		gDao.executeRaw(addGameColumn+"isTracked BOOLEAN DEFAULT 1;");
		
		// clean out games with non-unique players
		GenericRawResults<String[]> rawResults = gDao.queryRaw(
				    "SELECT * FROM game WHERE firstPlayerId == secondPlayerId");
		List<String[]> results = rawResults.getResults();
		String[] resultArray = results.get(0);
		for (String[] gRes: results) {
			String gId = gRes[0];
			tDao.executeRaw("DELETE FROM throw WHERE gameId == " + gId + ";");
		}
		gDao.executeRaw("DELETE FROM game WHERE firstPlayerId == secondPlayerId;");
		
		// continue migrating game table
		pDao.executeRaw("ALTER TABLE game RENAME TO temp;");
		TableUtils.createTable(connectionSource, Game.class);
		pDao.executeRaw("INSERT INTO game(id, firstPlayer_id, secondPlayer_id, session_id, venue_id, firstPlayerOnTop, datePlayed, firstPlayerScore, secondPlayerScore, isTeam, isComplete, isTracked) " +
				"SELECT id, firstPlayerId, secondPlayerId, sessionId, venueId, firstPlayerOnTop, datePlayed, firstPlayerScore, secondPlayerScore, isTeam, isComplete, isTracked FROM temp;");
		pDao.executeRaw("DROP TABLE temp;");
		
		// mark completed games as appropriate
		for (Game g: gDao) {
			g.checkGameComplete();
			gDao.update(g);
		}
		
		// player table
		pDao.executeRaw("ALTER TABLE player ADD COLUMN isActive BOOLEAN DEFAULT 1;");
		pDao.executeRaw("ALTER TABLE player ADD COLUMN prefersRightSide BOOLEAN DEFAULT 0;");
		pDao.executeRaw("ALTER TABLE player ADD COLUMN prefersLeftSide BOOLEAN DEFAULT 0;");
		pDao.executeRaw("ALTER TABLE player RENAME TO temp;");
		TableUtils.createTable(connectionSource, Player.class);
		pDao.executeRaw("INSERT INTO player(id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, prefersRightSide, prefersLeftSide, height_cm, weight_kg, isActive) " +
				"SELECT id, firstName, lastName, nickName, throwsRightHanded, throwsLeftHanded, prefersRightSide, prefersLeftSide, height_cm, weight_kg, isActive FROM temp;");
		pDao.executeRaw("DROP TABLE temp;");

		// session table
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
	}
	
	public static void increment_10( 
			ConnectionSource connectionSource,
			Dao<Game, Long> gDao,
			Dao<Throw, Long> tDao 
			) throws SQLException{
			
			//throw table
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
			tDao.executeRaw("UPDATE throw SET throwResult="+String.valueOf(ThrowResult.BROKEN)+" WHERE isBroken=1;");
			
			// migrate offensive errors. drink drop and break errors werent tracked before so stay false.
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensiveDrinkDropped BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensivePoleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensiveBottleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isOffensiveBreakError BOOLEAN DEFAULT 0;");
			
			tDao.executeRaw("UPDATE throw SET isOffensivePoleKnocked=1 WHERE ownGoalScore=2 AND isOwnGoal=1;");
			tDao.executeRaw("UPDATE throw SET isOffensiveBottleKnocked=1 WHERE ownGoalScore=3 AND isOwnGoal=1;");
			
			////////////////////////////////////////////////////////////////////
			//////////////// MIGRATE DEFENSIVE ERRORS //////////////////////////
			////////////////////////////////////////////////////////////////////
			
			
			//////////// ADD NEW COLUMNS ///////////////////
			// isGoaltend (db10) == > isGoaltend (db11) 
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isGrabbed BOOLEAN DEFAULT 0;");
			 // isDrinkDropped (db10) ==> isDefensiveDrinkDropped (db11)
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isDefensivePoleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isDefensiveBottleKnocked BOOLEAN DEFAULT 0;");
			tDao.executeRaw("ALTER TABLE throw ADD COLUMN isDefensiveBreakError BOOLEAN DEFAULT 0;");
			
			
			/////////// FIX FALSE POSITIVES ////////////////
			tDao.executeRaw("UPDATE throw SET errorScore=0 WHERE isError=0;");
			
			////////// FIX E1's ///////////////////
			// errorScore=0 (PCB knocked but bottle caught) no longer tracked.
			tDao.executeRaw("UPDATE throw SET isGrabbed=1 WHERE errorScore=1 AND isError=1;");
			
		    ////////// FIX E2's and E3s///////////////////
			//for errorScore=2/3, assume defense knock pole/bottle if throw was a strike. 
			//assume dead pole/bottle hit that wasnt caught if throw was HRLL
			tDao.executeRaw("UPDATE throw SET isDefensivePoleKnocked=1 "+
								"WHERE errorScore=2 and throwType="+String.valueOf(ThrowType.STRIKE)+" AND isError=1;"); 
			tDao.executeRaw("UPDATE throw SET isDefensiveBottleKnocked=1 "+
								"WHERE errorScore=3 and throwType="+String.valueOf(ThrowType.STRIKE)+" AND isError=1;"); 
			
			//HRLL with error score greater than 1 maps to dead HRLL
			tDao.executeRaw("UPDATE throw SET deadType="+String.valueOf(DeadType.HIGH)+
								" WHERE isError=1 AND errorScore>1 and throwType="+String.valueOf(ThrowType.BALL_HIGH)+";"); // deadType to high
			tDao.executeRaw("UPDATE throw SET deadType="+String.valueOf(DeadType.RIGHT)+
								" WHERE isError=1 AND errorScore>1 and throwType="+String.valueOf(ThrowType.BALL_RIGHT)+";"); // deadType to right
			tDao.executeRaw("UPDATE throw SET deadType="+String.valueOf(DeadType.LOW)+
								" WHERE isError=1 AND errorScore>1 and throwType="+String.valueOf(ThrowType.BALL_LOW)+";"); // deadType to low
			tDao.executeRaw("UPDATE throw SET deadType="+String.valueOf(DeadType.LEFT)+
								" WHERE isError=1 AND errorScore>1 and throwType="+String.valueOf(ThrowType.BALL_LEFT)+";"); // deadType to left
			
			// throwType to pole
			tDao.executeRaw("UPDATE throw SET throwType="+String.valueOf(ThrowType.POLE)+
								" WHERE isError=1 AND errorScore=2 AND throwType!="+String.valueOf(ThrowType.STRIKE)+";"); 
			// throwType to bottle
			tDao.executeRaw("UPDATE throw SET throwType="+String.valueOf(ThrowType.BOTTLE)+
								" WHERE isError=1 AND errorScore=3 AND throwType!="+String.valueOf(ThrowType.STRIKE)+";"); 
			// throwResult to dropped
			//mc:doesn't this overwrite the previous two statements? maybe it should come first...
			tDao.executeRaw("UPDATE throw SET throwResult="+String.valueOf(ThrowResult.DROP)+
								" WHERE isError=1 AND errorScore>1 AND throwType!="+String.valueOf(ThrowType.STRIKE)+";"); 
			
			
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
