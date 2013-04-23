package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Venue;

public class Detail_Venue extends MenuContainerActivity {
	Long vId;
	Venue v;
	Dao<Venue, Long> vDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_venue);
		
		Intent intent = getIntent();
		vId = intent.getLongExtra("VID", -1);
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
		if (vId != -1){
			try{
				vDao = Venue.getDao(getApplicationContext());
				v = vDao.queryForId(vId);
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		TextView vName = (TextView) findViewById(R.id.vDet_name);
		vName.setText(v.getName());
		
		TextView vId = (TextView) findViewById(R.id.vDet_id);
		vId.setText(String.valueOf(v.getId()));
		
		TextView pFromTop = (TextView) findViewById(R.id.vDet_fromTop);
		if (v.scoreKeptFromTop) {
			pFromTop.setText("Score kept from top");
		} else {
			pFromTop.setText("Score kept from bottom");
		}
	}
	public void modifyVenue(View view){
		Intent intent = new Intent(getApplicationContext(), NewVenue.class);
        intent.putExtra("VID", vId);
        startActivity(intent);
	}
}
