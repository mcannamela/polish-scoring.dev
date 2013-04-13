package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Venue;

public class View_Venues extends MenuContainerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_view_list);
		
		refreshVenuesListing();
		
		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.venues).setEnabled(false);
		return true;
	}
	
	@Override
	public void openAddActivity() {
    	Intent intent = new Intent(this, NewVenue.class);
    	startActivity(intent);
    }
	@Override
    protected void onRestart(){
    	super.onRestart();
    	refreshVenuesListing();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshVenuesListing();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	finish();
    }
    private OnItemClickListener mGameClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
        	String msg;
        	
        	ViewHolder_Venue h = (ViewHolder_Venue) v.getTag();
        	Long gid  = Long.valueOf(h.getVenueId());
        	msg = h.getVenueId() +" was clicked";
        	Context context = getApplicationContext();
    		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    		
//    		Intent intent = new Intent(getApplicationContext(), GameInProgress.class);
//			intent.putExtra("GID", gid);
//	    	startActivity(intent);
        }
    };
    protected void refreshVenuesListing(){
    	ArrayList<Venue> venuesArray = new ArrayList<Venue>();
        Dao<Venue, Long> venueDao=null;
    	
    	try{
    		venueDao = getHelper().getVenueDao();
    		 for(Venue s: venueDao){
    			 venuesArray.add(s);
    			}
    	}
    	catch (SQLException e){
    		Context context = getApplicationContext();
    		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    		Log.e(View_Venues.class.getName(), "Retrieval of venues failed", e);
    	}
        
        ViewAdapter_Venue adapter = new ViewAdapter_Venue(this, 
											                R.id.layout_session_list_item, 
											                venuesArray);
        ListView listView = (ListView) findViewById(R.id.db_listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mGameClickedHandler); 
    }
}
