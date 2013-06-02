package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseUpgrader {
	public static void increment_09(int oldVer, int newVer, 
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
}
