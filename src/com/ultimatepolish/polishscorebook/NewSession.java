package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Session;

public class NewSession extends MenuContainerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_new_session);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		return true;
	}
	public void createNewSession(View view) {
		Session session= null;
    	String sessionName = null;
    	Date startDate = null;
    	
    	TextView tv;
    	String s;
    	tv = (TextView) findViewById(R.id.editText_sessionName);
    	s = tv.getText().toString().trim().toLowerCase(Locale.US);
    	if (!s.isEmpty()){
    		sessionName = new String(s);
    	}
    	
//    	DatePicker dp = (DatePicker) findViewById(R.id.datePicker_sessionStartDate);
//    	GregorianCalendar gc = new GregorianCalendar(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
//    	startDate = gc.getTime();
    	startDate = new Date();
    	session = new Session(sessionName, startDate);
    	
    	
    	Context context = getApplicationContext();
    	try{
    		Dao<Session, Long> dao = getHelper().getSessionDao();
	   		dao.create(session);
	   		Toast.makeText(context, "Session created!", Toast.LENGTH_SHORT).show();
	   		finish();
		   	}
		 catch (SQLException e){
			 Log.e(PolishScorebook.class.getName(), "Could not create Session", e);
			 Toast.makeText(context, "could not create Session", Toast.LENGTH_SHORT).show();
//			 Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		   	}
    }

}
