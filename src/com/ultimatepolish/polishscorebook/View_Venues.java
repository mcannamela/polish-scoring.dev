package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Venue;

public class View_Venues extends MenuContainerActivity {
	private static final String LOGTAG = "View_Venues";
	
	private LinkedHashMap<String, ViewHolderHeader_Venue> sHash = new LinkedHashMap<String, ViewHolderHeader_Venue>();
	private List<ViewHolderHeader_Venue> statusList = new ArrayList<ViewHolderHeader_Venue>();
	private ListAdapter_Venue venueAdapter;
	private ExpandableListView elv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_listing);
		
		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        elv = (ExpandableListView) findViewById(R.id.dbListing);
        venueAdapter = new ListAdapter_Venue(View_Venues.this, statusList);
        elv.setAdapter(venueAdapter);
        expandAll();
        elv.setOnChildClickListener(elvItemClicked);
        elv.setOnGroupClickListener(elvGroupClicked);
        
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.venues).setEnabled(false);
		menu.findItem(R.id.addButton).setVisible(true);
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
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshVenueListing();
    }
    @Override
    protected void onStop() {
    	super.onStop();
    }
    private void expandAll() {
    	//method to expand all groups
    	int count = venueAdapter.getGroupCount();
    	for (int i = 0; i < count; i++){
		elv.expandGroup(i);
    	}
    }
    private void collapseAll() {
    	//method to collapse all groups
    	int count = venueAdapter.getGroupCount();
    	for (int i = 0; i < count; i++){
    	elv.collapseGroup(i);
    	}
    }
    protected void refreshVenueListing(){
    	sHash.clear();
    	statusList.clear();
    	
    	// add all the statii to the headers
        addStatus("Active");
        addStatus("Inactive");
        
        // add all the venues
    	Dao<Venue, Long> venueDao = null;
        try{
        	venueDao = getHelper().getVenueDao();
        	for (Venue v: venueDao) {
        		addVenue(v.getIsActive(), 
        				String.valueOf(v.getId()), 
        				v.getName()
        				);
        	}
    	}
        catch (SQLException e){
    		Context context = getApplicationContext();
    		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    		Log.e(View_Games.class.getName(), "Retrieval of venues failed", e);
        }
        
    	expandAll();
    	venueAdapter.notifyDataSetChanged(); // required in case the list has changed
    }
    private OnChildClickListener elvItemClicked =  new OnChildClickListener() {
    	public boolean onChildClick(ExpandableListView parent, View v,
    	int groupPosition, int childPosition, long id) {

	    //get the group header
	    ViewHolderHeader_Venue statusInfo = statusList.get(groupPosition);
	    //get the child info
   		ViewHolder_Venue venueInfo =  statusInfo.getVenueList().get(childPosition);
   		//display it or do something with it
   		Toast.makeText(getBaseContext(), "Selected " + venueInfo.getName(), Toast.LENGTH_SHORT).show();
    	
   		// load the game in progress screen
   		Long vId  = Long.valueOf(venueInfo.getId());
		Intent intent = new Intent(getApplicationContext(), Detail_Venue.class);
        intent.putExtra("VID", vId);
        startActivity(intent);
    	return false;
    	}
    };
    private OnGroupClickListener elvGroupClicked =  new OnGroupClickListener() {
    	public boolean onGroupClick(ExpandableListView parent, View v,
    	int groupPosition, long id) {
    	    
    	//get the group header
    	ViewHolderHeader_Venue statusInfo = statusList.get(groupPosition);
    	//display it or do something with it
    	Toast.makeText(getBaseContext(), "Tapped " + statusInfo.getName(), Toast.LENGTH_SHORT).show();
    	return false;
    	}
    };
    private void addStatus(String statusName){
    	ViewHolderHeader_Venue vhh_Venue = new ViewHolderHeader_Venue();
    	vhh_Venue.setName(statusName);
    	statusList.add(vhh_Venue);
    	sHash.put(statusName, vhh_Venue);
    }
    private void addVenue(Boolean isActive, String venueId, String venueName){
    	//find the index of the session header
    	String sortBy;
    	if (isActive) {
    		sortBy = "Active";
    	} else {
    		sortBy = "Inactive";
    	}
    	ViewHolderHeader_Venue statusInfo = sHash.get(sortBy);
    	try {
    		List<ViewHolder_Venue> venueList = statusInfo.getVenueList();
	    
		    //create a new child and add that to the group
		    ViewHolder_Venue venueInfo = new ViewHolder_Venue();
		    venueInfo.setId(venueId);
		    venueInfo.setName(venueName);
		    venueList.add(venueInfo);
			statusInfo.setVenueList(venueList);
    	} catch(NullPointerException e) {
    		loge("The header " + sortBy + " does not exist", e);
    	}
	}
    
    public void log(String msg){
  		Log.i(LOGTAG, msg);
  	}
  	public void logd(String msg){
  		Log.d(LOGTAG, msg);
  	}
  	public void loge(String msg, Exception e){
  		Log.e(LOGTAG, msg + ": " + e.getMessage());
  	}
}
