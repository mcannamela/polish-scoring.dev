package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;

public class Detail_Team extends MenuContainerActivity {
	Long tId;
//	Team t;
//	Dao<Team, Long> tDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_team);
		
		Intent intent = getIntent();
		tId = intent.getLongExtra("TID", -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		
		return true;
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
		if (tId != -1){
//			try{
//				tDao = Team.getDao(getApplicationContext());
//				t = tDao.queryForId(tId);
//			}
//			catch (SQLException e){
//				Toast.makeText(getApplicationContext(), 
//						e.getMessage(), 
//						Toast.LENGTH_LONG).show();
//			}
		}
		
		TextView tName = (TextView) findViewById(R.id.tDet_name);
//		tName.setText(t.getName());
		
		TextView teamId = (TextView) findViewById(R.id.tDet_id);
//		teamId.setText(String.valueOf(t.getId()));
		
		TextView tP1 = (TextView) findViewById(R.id.tDet_p1name);
//		tP1.setText(t.getPlayers[0].getFirstName() + " " + t.getPlayers[0].getLastName());
		
		TextView tP2 = (TextView) findViewById(R.id.tDet_p2name);
//		tP2.setText(t.getPlayers[1].getFirstName() + " " + t.getPlayers[1].getLastName());
		
		TextView tWinRatio = (TextView) findViewById(R.id.tDet_winRatio);
//		tWinRatio.setText(String.valueOf(t.getnWins()) + "/" + String.valueOf(t.getnLosses()));
	}
	public void modifyTeam(View view){
		Intent intent = new Intent(getApplicationContext(), NewTeam.class);
        intent.putExtra("TID", tId);
        startActivity(intent);
	}

}
