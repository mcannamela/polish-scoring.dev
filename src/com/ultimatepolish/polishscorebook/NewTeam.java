package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ultimatepolish.scorebookdb.Player;

public class NewTeam extends MenuContainerActivity {
	Long tId;
//	Team t;
//	Dao<Team, Long> tDao;
	
	TextView name;
	Spinner p1;
	Spinner p2;
	int p1_pos = 0;
	int p2_pos = 1;
	
	List<Player> players = new ArrayList<Player>();
	List<String> playerNames = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_team);
		
		name = (TextView) findViewById(R.id.editText_teamName);
		p1 = (Spinner) findViewById(R.id.spinner_player1);
		p2 = (Spinner) findViewById(R.id.spinner_player2);
		Button createButton = (Button) findViewById(R.id.button_createTeam);
		
		refreshSpinners();
		
		p1.setOnItemSelectedListener(mPlayerOneSelectedHandler);
		p2.setOnItemSelectedListener(mPlayerTwoSelectedHandler);
		
		Intent intent = getIntent();
		tId = intent.getLongExtra("TID", -1);
		if (tId != -1){
//			try{
//				tDao = Team.getDao(getApplicationContext());
//				t = tDao.queryForId(tId);
				createButton.setText("Modify");
//				name.setText(t.getName());
//				p1 set to player one
//				p2 set to player two
//			}
//			catch (SQLException e){
//				Toast.makeText(getApplicationContext(), 
//						e.getMessage(), 
//						Toast.LENGTH_LONG).show();
//			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void createNewTeam(View view) {
		Context context = getApplicationContext();
//		Team newTeam = null;
		String teamName = null;
		Player[] p = null;
    	
    	
    	String s = name.getText().toString().trim().toLowerCase(Locale.US);
    	if (!s.isEmpty()){
    		teamName = new String(s);
    	}
    	
//    	p[0] = pId from spinner p1
//    	p[1] = pId from spinner p2
//    	check that they are different players.
    	
//    	if (tId != -1) {
//    		t.setName(teamName);
//    		t.setPlayers(p);
//    		try {
//				tDao.update(t);
//				Toast.makeText(context, "Team modified.", Toast.LENGTH_SHORT).show();
//				finish();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Toast.makeText(context, "Could not modify team.", Toast.LENGTH_SHORT).show();
//			}
//    		
//    	} else {
//	    	newTeam = new Team(teamName, p);
	    	
//	    	try{
//	    		Dao<Team, Long> dao = getHelper().getTeamDao();
//		   		dao.create(newTeam);
//		   		Toast.makeText(context, "Team created!", Toast.LENGTH_SHORT).show();
		   		Toast.makeText(context, "Teams not available yet!", Toast.LENGTH_SHORT).show();
		   		finish();
//			   	}
//			 catch (SQLException e){
//				 Log.e(PolishScorebook.class.getName(), "Could not create player.", e);
//				 boolean player_exists = false;
//				 try{
//					 player_exists = newPlayer.exists(context);
//					 if (player_exists){
//					 		Toast.makeText(context, "Player already exists.", Toast.LENGTH_SHORT).show();
//					 	}
//					 else{
//						 Toast.makeText(context, "Could not create player.", Toast.LENGTH_SHORT).show();
//					 }
//				 }
//				 catch (SQLException ee){
//					 Toast.makeText(context, ee.getMessage(), Toast.LENGTH_LONG).show();
//				   		Log.e(PolishScorebook.class.getName(), "Could not test for existence of player", ee);
//				 }
//			 }
//    	}
    }
	private OnItemSelectedListener mPlayerOneSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			p1_pos = position;
		}
		public void onNothingSelected(AdapterView<?> parent) {}  
	};
	private OnItemSelectedListener mPlayerTwoSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			p2_pos = position;
		}
		public void onNothingSelected(AdapterView<?> parent) {}
	};
	public void refreshSpinners(){
		Context context = getApplicationContext();
		try{
			players = Player.getAll(context);
		}
		catch (SQLException e){
			Log.e(PolishScorebook.class.getName(), "Could not get objects", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		playerNames.clear();
		
		for(Player p: players){
			playerNames.add(p.getFirstName() + " " + p.getLastName());
		}

		ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				playerNames);
		pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		p1.setAdapter(pAdapter);
		p2.setAdapter(pAdapter);
	}
}
