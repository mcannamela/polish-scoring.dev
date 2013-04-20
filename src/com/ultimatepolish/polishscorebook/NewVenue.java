package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Venue;

public class NewVenue extends MenuContainerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_new_venue);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		return true;
	}
	public void createNewVenue(View view) {
		Venue venue= null;
    	String venueName = null;
    	boolean  scoreKeptFromTop;
    	TextView tv;
    	String s;
    	tv = (TextView) findViewById(R.id.editText_venueName);
    	s = tv.getText().toString().trim().toLowerCase(Locale.US);
    	if (!s.isEmpty()){
    		venueName = new String(s);
    	}
    	CheckBox cb = (CheckBox) findViewById(R.id.checkBox_scoreKeptFromTop);
    	scoreKeptFromTop = cb.isChecked();
    	
    	venue = new Venue(venueName, scoreKeptFromTop);
    	
    	Context context = getApplicationContext();
    	try{
    		Dao<Venue, Long> dao = getHelper().getVenueDao();
	   		dao.create(venue);
	   		Toast.makeText(context, "venue created!", Toast.LENGTH_SHORT).show();
		   	}
		 catch (SQLException e){
			 Log.e(PolishScorebook.class.getName(), "Could not create venue", e);
			 Toast.makeText(context, "could not create venue", Toast.LENGTH_SHORT).show();
		   	}
    }

}
