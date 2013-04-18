package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;

public class NewPlayer extends MenuContainerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_new_player);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		return true;
	}
	public void createNewPlayer(View view) {
		Player newPlayer = null;
		String firstName = null;
		String lastName = null;
    	String nickname = null;
    	
    	int height_cm = -1;
    	int weight_kg = -1;
    	
    	Boolean throwsRightHanded = null;
    	Boolean throwsLeftHanded = null;
    	
    	CheckBox cb = (CheckBox) findViewById(R.id.checkBox_throwsRightHanded);
    	throwsRightHanded = cb.isChecked();
    	cb = (CheckBox) findViewById(R.id.checkBox_throwsLeftHanded);
    	throwsLeftHanded = cb.isChecked();
    	
    	TextView tv = (TextView) findViewById(R.id.editText_playerName);
    	String s = tv.getText().toString();
    	String[] toks;
    	
    	toks = s.split("\\s+");
    	if (toks.length>=2){
    		firstName = toks[0].toLowerCase(Locale.US);
    		lastName = toks[1].toLowerCase(Locale.US);
    	}
    	
    	tv = (TextView) findViewById(R.id.editText_nickname);
    	s = tv.getText().toString().trim().toLowerCase(Locale.US);
    	if (!s.isEmpty()){
    		nickname = new String(s);
    	}
    	
    	tv = (TextView) findViewById(R.id.editText_weight);
    	s = tv.getText().toString().trim();
    	if (!s.isEmpty()){
    		weight_kg =  Integer.parseInt(s);
    	}
    	
    	tv = (TextView) findViewById(R.id.editText_height);
    	s = tv.getText().toString().trim();
    	if (!s.isEmpty()){
    		height_cm =  Integer.parseInt(s);
    	}
    	
    	newPlayer = new Player(firstName, lastName, nickname, 
    							throwsRightHanded, throwsLeftHanded, 
    							height_cm, weight_kg);
    	
    	Context context = getApplicationContext();
    	try{
    		
    		Dao<Player, Long> dao = getHelper().getPlayerDao();
	   		dao.create(newPlayer);
	   		Toast.makeText(context, "player created!", Toast.LENGTH_SHORT).show();
		   	}
		 catch (SQLException e){
			 Log.e(PolishScorebook.class.getName(), "Could not create player", e);
			 boolean player_exists = false;
			 try{
				 player_exists = newPlayer.exists(context);
				 if (player_exists){
				 		Toast.makeText(context, "player already exists", Toast.LENGTH_SHORT).show();
				 	}
				 else{
					 Toast.makeText(context, "could not create player", Toast.LENGTH_SHORT).show();
				 }
			 }
			 catch (SQLException ee){
				 Toast.makeText(context, ee.getMessage(), Toast.LENGTH_LONG).show();
			   		Log.e(PolishScorebook.class.getName(), "Could not test for existence of player", ee);
			 }
		   	}
    }
}
