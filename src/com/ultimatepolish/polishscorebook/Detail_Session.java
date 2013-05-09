package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.SessionType;

public class Detail_Session extends MenuContainerActivity {
	Long sId;
	Session s;
	Dao<Session, Long> sDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_session);
		
		Intent intent = getIntent();
		sId = intent.getLongExtra("SID", -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.modifyButton).setVisible(true);
		return true;
	}
	
	@Override
	public void openModifyActivity() {
		Intent intent = new Intent(getApplicationContext(), NewSession.class);
        intent.putExtra("SID", sId);
        startActivity(intent);
    }
	
	@Override
    protected void onRestart(){
    	super.onRestart();
    	refreshDetails();
    }
	
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshDetails();
    }
	
	public void refreshDetails(){
		if (sId != -1){
			try{
				sDao = Session.getDao(getApplicationContext());
				s = sDao.queryForId(sId);
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		TextView sName = (TextView) findViewById(R.id.sDet_name);
		sName.setText(s.getSessionName());
		
		TextView sId = (TextView) findViewById(R.id.sDet_id);
		sId.setText(String.valueOf(s.getId()));
		
		TextView sType = (TextView) findViewById(R.id.sDet_type);
		switch (s.getSessionType()){
		case SessionType.OPEN:
			sType.setText("Open session");
			break;
		case SessionType.LEAGUE:
			sType.setText("League");
			break;
		case SessionType.LADDER:
			sType.setText("Ladder");
			break;
		case SessionType.SNGL_ELIM:
			sType.setText("Single-elimination tournament");
			break;
		case SessionType.DBL_ELIM:
			sType.setText("Double-elimination tournament");
			break;
		}
		
		
		TextView sStartDate = (TextView) findViewById(R.id.sDet_startDate);
		sStartDate.setText("Start date: " + String.valueOf(s.getStartDate()));
		
		TextView sEndDate = (TextView) findViewById(R.id.sDet_endDate);
		sEndDate.setText("End date: " + String.valueOf(s.getEndDate()));
		
		TextView sIsTeam = (TextView) findViewById(R.id.sDet_isTeam);
		if (s.getIsTeam()) {
			sIsTeam.setText("Doubles session");
		} else {
			sIsTeam.setText("Singles session");
		}
		
		TextView sIsActive = (TextView) findViewById(R.id.sDet_isActive);
		if (s.getIsActive()) {
			sIsActive.setText("This session is active");
		} else {
			sIsActive.setText("This session is no longer active");
		}
		
	}
}
