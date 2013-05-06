package com.ultimatepolish.scorebookdb;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	private static final Class<?>[] classes = new Class[] {
	    Player.class, PlayerStats.class, Team.class, TeamStats.class, Badge.class, Game.class, Session.class, SessionMember.class, Throw.class, Venue.class
	};
	public static void main(String[] args) throws Exception {
		writeConfigFile("ormlite_config.txt", classes);
	}
}
