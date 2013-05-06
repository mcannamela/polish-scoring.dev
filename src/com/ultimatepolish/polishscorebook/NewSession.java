package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Session;

public class NewSession extends MenuContainerActivity {
	Long sId;
	Session s;
	Dao<Session, Long> sDao;
	
	TextView name;
//	DatePicker dp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_session);
		
		name = (TextView) findViewById(R.id.editText_sessionName);
//		dp = (DatePicker) findViewById(R.id.datePicker_sessionStartDate);
		Button createButton = (Button) findViewById(R.id.button_createSession);
		
		Intent intent = getIntent();
		sId = intent.getLongExtra("SID", -1);
		if (sId != -1){
			try{
				sDao = Session.getDao(getApplicationContext());
				s = sDao.queryForId(sId);
				createButton.setText("Modify");
				name.setText(s.getSessionName());
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
    	Date startDate = null;
    	
    	String st;
    	st = name.getText().toString().trim().toLowerCase(Locale.US);
    	if (!st.isEmpty()){
    		sessionName = st;
    	}
    	
//    	GregorianCalendar gc = new GregorianCalendar(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
//    	startDate = gc.getTime();
    	startDate = new Date();
    	
    	if (sId != -1) {
    		s.setSessionName(sessionName);
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
    		session = new Session(sessionName, startDate, 1, false);
        	
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
