package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.SessionMember;
import com.ultimatepolish.scorebookdb.SessionType;
import com.ultimatepolish.scorebookdb.Team;

public class Detail_Session extends MenuContainerActivity {
	Long sId;
	Session s;
	Dao<Session, Long> sDao;
	Dao<SessionMember, Long> smDao;
	Dao<Player, Long> pDao;
	Dao<Team, Long> tDao;
	List<SessionMember> sMembers = new ArrayList<SessionMember>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_detail_session);
		
		Intent intent = getIntent();
		sId = intent.getLongExtra("SID", -1);
		
		if (sId != -1){
			try{
				sDao = Session.getDao(getApplicationContext());
				smDao = SessionMember.getDao(getApplicationContext());
				pDao = Player.getDao(getApplicationContext());
				
				s = sDao.queryForId(sId);
				
				// get all the session members
				QueryBuilder<Session, Long> sQue = sDao.queryBuilder();
				sQue.where().eq("id", sId);
				QueryBuilder<SessionMember, Long> smQue = smDao.queryBuilder();
		        sMembers = smQue.join(sQue).orderBy(SessionMember.PLAYER_SEED, true).query();
		        
	        	for(SessionMember member: sMembers) {
	        		pDao.refresh(member.getPlayer());
	        	}
	        	
		    }
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
		
		if (s.sessionType == SessionType.SNGL_ELIM) {
			setContentView(R.layout.activity_detail_session_singleelim);
			refreshSingleElimBracket();
		} else {
			setContentView(R.layout.activity_detail_session);
		}
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
    }
	
    @Override
    protected void onResume(){
    	super.onResume();
    	refreshDetails();
    }
	
	public void refreshDetails(){
		TextView sName = (TextView) findViewById(R.id.sDet_name);
		sName.setText(s.getSessionName());
		
		TextView sId = (TextView) findViewById(R.id.sDet_id);
		sId.setText(String.valueOf(s.getId()));
		
		TextView sType = (TextView) findViewById(R.id.sDet_type);
		sType.setText(SessionType.typeString[s.getSessionType()]);
		
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
	
	public void refreshSingleElimBracket(){
		View sv = findViewById(R.id.scrollView1);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.sDet_bracket);

		TextView tv;
		
		sMembers = foldRoster(sMembers);
		
		for (SessionMember member: sMembers) {
			if (member != null) {
				tv = new TextView(sv.getContext());
				tv.setText("(" + String.valueOf(member.getPlayerSeed()+1) + ") " + member.getPlayer().getNickName() );
				tv.setId(member.getPlayerSeed()+1);
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(sv.getContext(), android.R.style.TextAppearance_Medium);
				if (member.getPlayerSeed() % 2 == 0) {
					tv.setBackgroundDrawable(sv.getContext().getResources().getDrawable(R.drawable.bracket_top_player));
				} else {
					tv.setBackgroundDrawable(sv.getContext().getResources().getDrawable(R.drawable.bracket_bottom_player));
				}
				
				tv.getBackground().setColorFilter(Color.RED, Mode.MULTIPLY);
				if (member.getPlayerSeed() != 0) {
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.BELOW, member.getPlayerSeed());
					lp.addRule(RelativeLayout.ALIGN_RIGHT, 1);
					rl.addView(tv, lp);
				} else {
					tv.setWidth(350);
					rl.addView(tv);
				}
			}
		}
		
	}
	
	public List<SessionMember> foldRoster(List<SessionMember> sMembers) {
		// expand the list size to the next power of two
		Integer n = factorTwos(sMembers.size());
		while (sMembers.size() < Math.pow(2, n)) {
			sMembers.add(null);
		}
		List<SessionMember> tempRoster = new ArrayList<SessionMember>();
		for (Integer i=0; i < n; i++) {
			tempRoster.clear();
			while (sMembers.size() > 0) {
				tempRoster.add(sMembers.get(0));
				tempRoster.add(sMembers.get(sMembers.size()-1));
				sMembers.remove(0);
				sMembers.remove(sMembers.size()-1);
				Log.i("SessionDetails", "sMembers: " + sMembers.size() + " members, tempRoster: " + tempRoster.size() + " members.");
			}
			sMembers = tempRoster;
			Log.i("SessionDetails", "n=" + i +", sMembers: " + sMembers.size() + " members, tempRoster: " + tempRoster.size() + " members.");
		}
		return sMembers;
	}
	
	public Integer factorTwos(Integer rosterSize) {
		Integer n = 1;
		while (Math.pow(2,n) < rosterSize) {
			n++;
		}
		return n;
	}
}
