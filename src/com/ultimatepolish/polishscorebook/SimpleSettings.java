package com.ultimatepolish.polishscorebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.Date;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.Venue;

public class SimpleSettings extends MenuContainerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_simple_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		menu.findItem(R.id.action_settings).setVisible(false);
		return true;
	}
	public void clearTables(View view){
    	Dao<Player, Long> pd;
    	Dao<Game, Long> gd;
    	Dao<Session, Long> sd;
    	Dao<Throw, Long> td;
    	Dao<Venue, Long> vd;
    	try{
    		pd = getHelper().getPlayerDao();
    		gd = getHelper().getGameDao();
    		sd = getHelper().getSessionDao();
    		td = getHelper().getThrowDao();
   		 	vd = getHelper().getVenueDao();
   		 	for(Player x: pd){
   		 		pd.delete(x);
   		 	}
   		 	for(Game x: gd){
   		 		gd.delete(x);
   		 	}
   		 	for(Session x: sd){
   		 		sd.delete(x);
   		 	}
   		 	for(Throw x: td){
   		 		td.delete(x);
   		 	}
   		 	for(Venue x: vd){
   				vd.delete(x);
   			}
   		 
	   	}
	   	catch (SQLException e){
	   		Context context = getApplicationContext();
	   		int duration = Toast.LENGTH_LONG;
	   		Toast.makeText(context, e.getMessage(), duration).show();
	   		Log.e(PolishScorebook.class.getName(), "Clearing tables failed", e);
	   	}
    }

	public void doPopulateTest(View view){
    	Dao<Player, Long> playerDao=null;
    	Player[] players = {
	    	new Player("michael", "cannamela", "mike c", true, false,170, 70 ),
	    	new Player("erin", "arai", "samu", true, false,160, 50 ),
	    	new Player("matt", "tuttle", "king tut", true, false, 182, 63),
	    	new Player("andrew", "o'brien", "dru", true, false, 182, 63),
	    	new Player("matt", "miguez", "murder", true, false, 182, 63),
	    	new Player("julian", "spring", "juice", false, true, 182, 63),
	    	new Player("mike", "freeman", "freeeedom", true, false, 182, 63),
	    	new Player("phillip", "anderson", "pillip", false, true, 182, 63),
	    	new Player("jon", "sukovich", "sukes appeal", true, false, 182, 63)
    	};
    	Dao<Session, Long> sessionDao=null;
    	Session s1 = new Session("league", new Date());
    	Session s2 = new Session("side_books", new Date());
    	Dao<Venue, Long> venueDao=null;
    	Venue v1 = new Venue("cogswell", true);
		Venue v2 = new Venue("verndale", true);
		Venue v3 = new Venue("oxford", true);
    	try{
    		playerDao = getHelper().getPlayerDao();
    		for (int i=0;i<players.length;i++){
    			playerDao.create(players[i]);
    		}
    		
    		sessionDao = getHelper().getSessionDao();
    		sessionDao.create(s1);
    		sessionDao.create(s2);
			venueDao = getHelper().getVenueDao();
			venueDao.create(v1);
			venueDao.create(v2);
			venueDao.create(v3);
    	}
    	catch (SQLException e){
    		Context context = getApplicationContext();
    		int duration = Toast.LENGTH_LONG;
    		Toast.makeText(context, e.getMessage(), duration).show();
    		Log.e(PolishScorebook.class.getName(), "Creation of players failed", e);
    	}
    }
	public void saveDB(View view){
    	if (isExternalStorageWritable()){
    		File internalDB = getInternalPath();
    		File externalDB = getExternalPath();
    		try{
	    		copyFile(internalDB, externalDB);
    		}
	        catch (Exception e){
	        	Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
	        }
    	}
    	else{
    		Toast.makeText(getApplicationContext(), 
					"db not saved; storage not writable", 
					Toast.LENGTH_SHORT).show();
    	}
    }
	public void loadDB(View view){
//    	if (isExternalStorageWritable()){
//    		File internalDB = getInternalPath();
//    		File externalDB = getExternalPath();
//    		try{
//	    		copyFile(externalDB, internalDB);
//    		}
//	        catch (Exception e){
//	        	Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
//	        }
//    	}
//    	else{
//    		Toast.makeText(getApplicationContext(), 
//					"db not loaded; storage not writable", 
//					Toast.LENGTH_SHORT).show();
//    	}
    	Toast.makeText(getApplicationContext(), 
				"feature not yet implemented!", 
				Toast.LENGTH_SHORT).show();
    }
	
	File getExternalPath(){
		File externalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File externalDB = new File(externalPath, "polish.bak.db");
		return externalDB;
	}
	File getInternalPath(){
		String dbPath = getHelper().getReadableDatabase().getPath();
		File internalDB = new File(dbPath);
		return internalDB;
	}
	boolean isExternalStorageWritable(){
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}
	
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
    
   
    
	
}
