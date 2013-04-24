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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Session;

public class View_Sessions extends MenuContainerActivity {
	private LinearLayout ll;
	private ListView lv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_list);
		
		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        ll = (LinearLayout) findViewById (R.id.db_viewListings);
    	lv = new ListView(this);
    	ll.addView(lv);
        lv.setOnItemClickListener(mSessionClickedHandler);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.sessions).setEnabled(false);
		return true;
	}	
	@Override
	public void openAddActivity() {
    	Intent intent = new Intent(this, NewSession.class);
    	startActivity(intent);
    }
	@Override
    protected void onRestart(){
    	super.onRestart();
    	refreshSessionsListing();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshSessionsListing();
    }
    @Override
    protected void onStop() {
    	super.onStop();
    }
    private OnItemClickListener mSessionClickedHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
        	String msg;
        	
        	ViewHolder_Session h = (ViewHolder_Session) v.getTag();
        	Long gid  = Long.valueOf(h.getSessionId());
        	msg = h.getSessionId() +" was clicked";
        	Context context = getApplicationContext();
    		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    		
//    		Intent intent = new Intent(getApplicationContext(), GameInProgress.class);
//			intent.putExtra("GID", gid);
//	    	startActivity(intent);
        }
    };
    protected void refreshSessionsListing(){
    	ArrayList<Session> sessionsArray = new ArrayList<Session>();
        Dao<Session, Long> sessionDao=null;
    	
    	try{
    		 sessionDao = getHelper().getSessionDao();
    		 for(Session s: sessionDao){
    			 sessionsArray.add(s);
    			}
    	}
    	catch (SQLException e){
    		Context context = getApplicationContext();
    		Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    		Log.e(View_Sessions.class.getName(), "Retrieval of sessions failed", e);
    	}
        
    	
    	ViewAdapter_Session adapter = new ViewAdapter_Session(this, 
                R.id.layout_session_list_item, 
                sessionsArray);
        lv.setAdapter(adapter);
    }
}
