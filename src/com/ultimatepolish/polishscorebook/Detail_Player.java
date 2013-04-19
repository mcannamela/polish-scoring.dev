package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;

public class Detail_Player extends Activity {
	Long pId;
	Player p;
	Dao<Player, Long> pDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_detail_player);
		
		Intent intent = getIntent();
		pId = intent.getLongExtra("PID", -1);
		if (pId != -1){
			try{
				pDao = Player.getDao(getApplicationContext());
				p = pDao.queryForId(pId);
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		
//		spinner_p1 = (Spinner) findViewById(R.id.spinner_player1);
//		spinner_p2 = (Spinner) findViewById(R.id.spinner_player2);
//		spinner_session = (Spinner) findViewById(R.id.spinner_session);
//		spinner_venue = (Spinner) findViewById(R.id.spinner_venue);

		refreshDetails();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		
		return true;
	}
	
	public void refreshDetails(){
		
		TextView pName = (TextView) findViewById(R.id.pDet_name);
		pName.setText(p.getFirstName() + ' ' + p.getLastName() + " (" + p.getNickName() + ")");
		
		TextView pHeight = (TextView) findViewById(R.id.pDet_height);
		pHeight.setText("Height: " + String.valueOf(p.getHeight_cm()) + " cm");
		
		TextView pWeight = (TextView) findViewById(R.id.pDet_weight);
		pWeight.setText("Weight: " + String.valueOf(p.getWeight_kg()) + " kg");
		
//		TextView pWeight = (TextView) findViewById(R.id.pDet_weight);
//		pWeight.setText();

	}

}
