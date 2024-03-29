package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Team;

public class Detail_Team extends MenuContainerActivity {
	Long tId;
	Team t;
	Dao<Team, Long> tDao;
	Dao<Player, Long> pDao;
	
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
		menu.findItem(R.id.modifyButton).setVisible(true);
		return true;
	}
	@Override
	public void openModifyActivity() {
		Intent intent = new Intent(getApplicationContext(), NewTeam.class);
        intent.putExtra("TID", tId);
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
		Player[] p = new Player[2];
		Context context = getApplicationContext();
		
		if (tId != -1){
			try{
				// TODO: uncomment this once teams are set up in the db again
//				tDao = Team.getDao(context);
				t = tDao.queryForId(tId);
				
				pDao = Player.getDao(context);
				pDao.refresh(t.getFirstPlayer());
				pDao.refresh(t.getSecondPlayer());
				
				p[0] = t.getFirstPlayer();
				p[1] = t.getSecondPlayer();
				
			}
			catch (SQLException e){
				Toast.makeText(context, 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		TextView tName = (TextView) findViewById(R.id.tDet_name);
		tName.setText(t.getTeamName());
		
		TextView teamId = (TextView) findViewById(R.id.tDet_id);
		teamId.setText(String.valueOf(t.getId()));
		
		TextView tP1 = (TextView) findViewById(R.id.tDet_p1name);
		tP1.setText(p[0].getFirstName() + " " + p[0].getLastName());
		
		TextView tP2 = (TextView) findViewById(R.id.tDet_p2name);
		tP2.setText(p[1].getFirstName() + " " + p[1].getLastName());
		
		TextView tWinRatio = (TextView) findViewById(R.id.tDet_winRatio);
//		tWinRatio.setText(String.valueOf(t.getnWins()) + "/" + String.valueOf(t.getnLosses()));
		
		TextView tIsActive = (TextView) findViewById(R.id.teamDet_isActive);
		if (t.getIsActive()) {
			tIsActive.setText("This team is active");
		} else {
			tIsActive.setText("This team is retired");
		}
	}
}
