package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.SessionType;

public class NewSession extends MenuContainerActivity {
	Long sId;
	Session s;
	Dao<Session, Long> sDao;
	
	TextView name;
	Spinner sessionTypeSpinner;
	CheckBox isTeamCB;
	CheckBox isActiveCB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_session);
		
		name = (TextView) findViewById(R.id.editText_sessionName);
		Button createButton = (Button) findViewById(R.id.button_createSession);		
		sessionTypeSpinner = (Spinner) findViewById(R.id.newSession_sessionType);
		isTeamCB = (CheckBox) findViewById(R.id.newSession_isTeam);
		isActiveCB = (CheckBox) findViewById(R.id.newSession_isActive);
		
		
		List<String> sessionTypes = new ArrayList<String>();
		sessionTypes.add("League");
		sessionTypes.add("Ladder");
		sessionTypes.add("Single elimination tournament");
		sessionTypes.add("Double elimination tournament");
		ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item,
				sessionTypes);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sessionTypeSpinner.setAdapter(sAdapter);
		
		Intent intent = getIntent();
		sId = intent.getLongExtra("SID", -1);
		if (sId != -1){
			try{
				sDao = Session.getDao(getApplicationContext());
				s = sDao.queryForId(sId);
				createButton.setText("Modify");
				name.setText(s.getSessionName());
				sessionTypeSpinner.setVisibility(View.GONE);
				isTeamCB.setVisibility(View.GONE);
				isActiveCB.setVisibility(View.VISIBLE);
				isActiveCB.setChecked(s.getIsActive());
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
	
	public void createNewSession(View view) {
		Context context = getApplicationContext();
		Session session= null;
    	String sessionName = null;
    	int sessionType = 0;
    	Date startDate;
    	Boolean isTeam;
    	Boolean isActive = true;
    	
    	// get the session name
    	String st;
    	st = name.getText().toString().trim().toLowerCase(Locale.US);
    	if (!st.isEmpty()){
    		sessionName = st;
    	}
    	
    	// get the session type
    	switch (sessionTypeSpinner.getSelectedItemPosition()) {
    	case 0:
    		// is league
    		sessionType = SessionType.LEAGUE;
    		break;
    	case 1:
    		// is ladder
    		sessionType = SessionType.LADDER;
    		break;
    	case 2:
    		// is single elimination tourny
    		sessionType = SessionType.SNGL_ELIM;
    		break;
    	case 3:
    		// is double elimination tourny
    		sessionType = SessionType.DBL_ELIM;
    		break;
    	}
    	
    	// get the start date    	
    	startDate = new Date();
    	
    	// get isTeam
    	isTeam = isTeamCB.isChecked();
    	
    	// get isActive
    	isActive = isActiveCB.isChecked();
    	
    	// make the new session or modify an existing one
    	if (sId != -1) {
    		s.setSessionName(sessionName);
    		s.setIsActive(isActive);
    		
    		try {
				sDao.update(s);
				Toast.makeText(context, "Session modified.", Toast.LENGTH_SHORT).show();
				finish();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(PolishScorebook.class.getName(), "Could not modify session.", e);
				Toast.makeText(context, "Could not modify session.", Toast.LENGTH_SHORT).show();
			}
    	} else {
    		session = new Session(sessionName, sessionType, startDate, isTeam);
        	
        	try{
        		Dao<Session, Long> dao = getHelper().getSessionDao();
    	   		dao.create(session);
    	   		Toast.makeText(context, "Session created!", Toast.LENGTH_SHORT).show();
    	   		finish();
    		   	}
    		 catch (SQLException e){
    			 Log.e(PolishScorebook.class.getName(), "Could not create session.", e);
    			 Toast.makeText(context, "Could not create session.", Toast.LENGTH_SHORT).show();
    		   	}
    	}
    	
    }

}
