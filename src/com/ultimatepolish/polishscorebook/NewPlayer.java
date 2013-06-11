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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;

public class NewPlayer extends MenuContainerActivity {
	Long pId;
	Player p;
	Dao<Player, Long> pDao;
	
	TextView name;
	TextView nick;
	TextView weight;
	TextView height;
	CheckBox rh;
	CheckBox lh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_player);
		
		name = (TextView) findViewById(R.id.editText_playerName);
		nick = (TextView) findViewById(R.id.editText_nickname);
		weight = (TextView) findViewById(R.id.editText_weight);
		height = (TextView) findViewById(R.id.editText_height);
		rh = (CheckBox) findViewById(R.id.checkBox_throwsRightHanded);
		lh = (CheckBox) findViewById(R.id.checkBox_throwsLeftHanded);
		Button createButton = (Button) findViewById(R.id.button_createPlayer);
		
		Intent intent = getIntent();
		pId = intent.getLongExtra("PID", -1);
		if (pId != -1){
			try{
				pDao = Player.getDao(getApplicationContext());
				p = pDao.queryForId(pId);
				createButton.setText("Modify");
				name.setText(p.getFirstName() + " " + p.getLastName());
				nick.setText(p.getNickName());
				weight.setText(String.valueOf(p.getWeight_kg()));
				height.setText(String.valueOf(p.getHeight_cm()));
				if (p.throwsLeftHanded == true) {
					lh.setChecked(true);
				}
				if (p.throwsRightHanded == true) {
					rh.setChecked(true);
				}
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void createNewPlayer(View view) {
		Context context = getApplicationContext();
		Player newPlayer = null;
		String firstName = null;
		String lastName = null;
    	String nickname = null;
    	
    	int height_cm = -1;
    	int weight_kg = -1;
    	
    	Boolean throwsRightHanded = null;
    	Boolean throwsLeftHanded = null;
    	    	
    	throwsRightHanded = rh.isChecked();
    	throwsLeftHanded = lh.isChecked();
    	
    	String s = name.getText().toString();
    	String[] toks;
    	
    	toks = s.split("\\s+");
    	if (toks.length>=2){
    		firstName = toks[0].toLowerCase(Locale.US);
    		lastName = toks[1].toLowerCase(Locale.US);
    	}
    	
    	s = nick.getText().toString().trim().toLowerCase(Locale.US);
    	if (!s.isEmpty()){
    		nickname = new String(s);
    	}
    	
    	s = weight.getText().toString().trim();
    	if (!s.isEmpty()){
    		weight_kg =  Integer.parseInt(s);
    	}
    	
    	s = height.getText().toString().trim();
    	if (!s.isEmpty()){
    		height_cm =  Integer.parseInt(s);
    	}
    	
    	if (pId != -1) {
    		p.setFirstName(firstName);
    		p.setLastName(lastName);
    		p.setNickName(nickname);
    		p.setWeight_kg(weight_kg);
    		p.setHeight_cm(height_cm);
    		p.setLeftHanded(throwsLeftHanded);
    		p.setRightHanded(throwsRightHanded);
    		try {
				pDao.update(p);
				Toast.makeText(context, "Player modified.", Toast.LENGTH_SHORT).show();
				finish();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(context, "Could not modify player.", Toast.LENGTH_SHORT).show();
			}
    		
    	} else {
	    	newPlayer = new Player(firstName, lastName, nickname, 
	    							throwsRightHanded, throwsLeftHanded, 
	    							height_cm, weight_kg);
	    	
	    	try{
	    		Dao<Player, Long> dao = getHelper().getPlayerDao();
		   		dao.create(newPlayer);
		   		Toast.makeText(context, "Player created!", Toast.LENGTH_SHORT).show();
		   		finish();
			   	}
			 catch (SQLException e){
				 Log.e(PolishScorebook.class.getName(), "Could not create player.", e);
				 boolean player_exists = false;
				 try{
					 player_exists = newPlayer.exists(context);
					 if (player_exists){
					 		Toast.makeText(context, "Player already exists.", Toast.LENGTH_SHORT).show();
					 	}
					 else{
						 Toast.makeText(context, "Could not create player.", Toast.LENGTH_SHORT).show();
					 }
				 }
				 catch (SQLException ee){
					 Toast.makeText(context, ee.getMessage(), Toast.LENGTH_LONG).show();
				   		Log.e(PolishScorebook.class.getName(), "Could not test for existence of player", ee);
				 }
			 }
    	}
    }
}
