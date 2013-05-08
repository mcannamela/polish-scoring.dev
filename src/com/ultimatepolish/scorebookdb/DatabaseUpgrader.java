package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseUpgrader extends OrmLiteBaseActivity<DatabaseHelper> {
	
	public void increment_09(SQLiteDatabase sqliteDatabase, int oldVer, int newVer, ConnectionSource connectionSource){
//		try {
			// game table
//			gDao.executeRaw("ALTER TABLE `game` ADD COLUMN isTeam BOOLEAN DEFAULT 0;");
//			gDao.executeRaw("ALTER TABLE `game` ADD COLUMN isComplete BOOLEAN DEFAULT 0;");
//			gDao.executeRaw("ALTER TABLE `game` ADD COLUMN isTracked BOOLEAN DEFAULT 1;");
//		} catch (SQLException e) {
//			Log.e(DatabaseUpgrader.class.getName(), "Unable to upgrade database from version " + 9 + " to "
//					+ 10, e);
//		}
	}	
}
