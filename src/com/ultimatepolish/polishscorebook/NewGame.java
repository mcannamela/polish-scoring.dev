package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Venue;

public class NewGame extends MenuContainerActivity {
	Spinner spinner_p1 ;
	Spinner spinner_p2 ;
	Spinner spinner_session ;
	Spinner spinner_venue ;
	
	int p1_pos = 0;
	int p2_pos = 1;
	int session_pos = 0;
	int venue_pos = 1;
	
	ArrayList<Player> players = new ArrayList<Player>() ;
	ArrayList<Session> sessions= new ArrayList<Session>();
	ArrayList<Venue> venues= new ArrayList<Venue>();
	
	ArrayList<String> playerNames = new ArrayList<String>();
	ArrayList<String> sessionNames = new ArrayList<String>();
	ArrayList<String> venueNames = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_new_game);
		spinner_p1 = (Spinner) findViewById(R.id.spinner_player1);
		spinner_p2 = (Spinner) findViewById(R.id.spinner_player2);
		spinner_session = (Spinner) findViewById(R.id.spinner_session);
		spinner_venue = (Spinner) findViewById(R.id.spinner_venue);
		
		refreshSpinners(spinner_p1);
		
		spinner_p1.setOnItemSelectedListener(mPlayerOneSelectedHandler);
		spinner_p2.setOnItemSelectedListener(mPlayerTwoSelectedHandler);
		spinner_session.setOnItemSelectedListener(mSessionSelectedHandler);
		spinner_venue.setOnItemSelectedListener(mVenueSelectedHandler);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		return true;
	}
	private OnItemSelectedListener mPlayerOneSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			p1_pos = position;
//			Toast.makeText(getApplicationContext(), 
//					"p1 is "+playerNames.get(p1_pos), 
//					Toast.LENGTH_SHORT).show();
		}
		public void onNothingSelected(AdapterView<?> parent) {}
	    
	};
	private OnItemSelectedListener mPlayerTwoSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			p2_pos = position;
//			Toast.makeText(getApplicationContext(), 
//					"p2 is "+playerNames.get(p2_pos), 
//					Toast.LENGTH_SHORT).show();
		}
		public void onNothingSelected(AdapterView<?> parent) {}
		
	};
	private OnItemSelectedListener mSessionSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			session_pos = position;
//			Toast.makeText(getApplicationContext(), 
//					"session is "+sessionNames.get(session_pos), 
//					Toast.LENGTH_SHORT).show();
		}
		public void onNothingSelected(AdapterView<?> parent) {}
	};
	private OnItemSelectedListener mVenueSelectedHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        	venue_pos = position;
//        	Toast.makeText(getApplicationContext(), 
//					"venue is "+venueNames.get(venue_pos), 
//					Toast.LENGTH_SHORT).show();
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    };
	public void refreshSpinners(View view){
		Context context = getApplicationContext();
		try{
			players = Player.getAll(context);
			sessions = Session.getAll(context);
			venues = Venue.getAll(context);
//			Toast.makeText(context, players.toString(), Toast.LENGTH_LONG).show();
		}
		catch (SQLException e){
			Log.e(PolishScorebook.class.getName(), "Could not get objects", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		playerNames.clear();
		sessionNames.clear();
		venueNames.clear();
		for(Player p: players){
			playerNames.add(String.valueOf(p.getId())+" "+p.getFirstName());
		}
		for(Session s: sessions){
			sessionNames.add(String.valueOf(s.getId())+" "+s.getSessionName());
		}
		for(Venue v: venues){
			venueNames.add(String.valueOf(v.getId())+" "+v.getName());
		}

		ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				playerNames);
		pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				sessionNames);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> vAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				venueNames);
		vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		
		spinner_p1.setAdapter(pAdapter);
		spinner_p2.setAdapter(pAdapter);
		
		spinner_session.setAdapter(sAdapter);
		spinner_venue.setAdapter(vAdapter);
	}
	
	public void createGame(View view){
		Long p1id = getIdFromDisplayString(playerNames.get(p1_pos));
		Long p2id = getIdFromDisplayString(playerNames.get(p2_pos));
		Long sid = getIdFromDisplayString(sessionNames.get(session_pos));
		Long vid = getIdFromDisplayString(venueNames.get(venue_pos));
		Game g = new Game(p1id, p2id, sid, vid);
		long gid; 
		g.setDatePlayed(new Date());
		
		try{
			Dao<Game, Long> d = Game.getDao(getApplicationContext());
			d.createIfNotExists(g);
//			Toast.makeText(getApplicationContext(), 
//					"create game with id = " + String.valueOf(g.getId()), 
//					Toast.LENGTH_LONG).show() ;
			gid = g.getId();
			Intent intent = new Intent(this, GameInProgress.class);
			intent.putExtra("GID", gid);
	    	startActivity(intent);
			
		}
		catch(SQLException e){
			Toast.makeText(getApplicationContext(), 
					e.getMessage(), 
					Toast.LENGTH_LONG).show();
		}
		
		
	}
	
	public Long getIdFromDisplayString(String s){
		 return Long.valueOf(s.split("\\s+")[0]);
	}	

}
