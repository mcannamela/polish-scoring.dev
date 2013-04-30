package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ultimatepolish.polishscorebook.R;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	/************************************************
	 * Suggested Copy/Paste code. Everything from here to the done block.
	 ************************************************/

	private static final String DATABASE_NAME = "polish.db";
	private static final int DATABASE_VERSION = 9;

	private Dao<Player, Long> playerDao;
	private Dao<Game, Long> gameDao;
	private Dao<Session, Long> sessionDao;
	private Dao<Throw, Long> throwDao;
	private Dao<Venue, Long> venueDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/************************************************
	 * Suggested Copy/Paste Done
	 ************************************************/

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Player.class);
			TableUtils.createTable(connectionSource, Game.class);
			TableUtils.createTable(connectionSource, Session.class);
			TableUtils.createTable(connectionSource, Throw.class);
			TableUtils.createTable(connectionSource, Venue.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create datbase", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Player.class, true);
			TableUtils.dropTable(connectionSource, Game.class, true);
			TableUtils.dropTable(connectionSource, Session.class, true);
			TableUtils.dropTable(connectionSource, Throw.class, true);
			TableUtils.dropTable(connectionSource, Venue.class, true);
			onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
					+ newVer, e);
		}
	}

	public Dao<Player, Long> getPlayerDao() throws SQLException {
		if (playerDao == null) {
			playerDao = getDao(Player.class);
		}
		return playerDao;
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
