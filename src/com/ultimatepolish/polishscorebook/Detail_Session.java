package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ultimatepolish.scorebookdb.Game;
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
			createSingleElimBracket();
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
    	if (s.sessionType == SessionType.SNGL_ELIM) {
			refreshSingleElimBracket();
		}
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
	
	public void createSingleElimBracket(){
		View sv = findViewById(R.id.scrollView1);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.sDet_bracket);
		RelativeLayout.LayoutParams lp;
		TextView tv;
		Integer matchIdx;
		
		foldRoster();
		makeInvisibleHeaders(rl);
		
		// create the lowest tier
		for (Integer i=0; i < sMembers.size()-1; i+=2) {
			Log.i("SessionDetails", "Match idx " + String.valueOf(i/2) + ", " +
					sMembers.get(i).getPlayerSeed() + " vs " +
					sMembers.get(i+1).getPlayerSeed());
			matchIdx = i/2;
			// upper half of match bracket
			tv = makeHalfBracket(sv.getContext(), sMembers.get(i), true, true);
			tv.setId(matchIdx + 1000);
			
			if (i != 0) {
				lp = new RelativeLayout.LayoutParams(
				        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.BELOW, matchIdx - 1 + 2000);
				lp.addRule(RelativeLayout.ALIGN_RIGHT, 1);
				lp.setMargins(0, 8, 0, 0);
				rl.addView(tv, lp);
			} else {
				rl.addView(tv);
			}
			
			// lower half of match bracket
			tv = makeHalfBracket(sv.getContext(), sMembers.get(i+1), false, true);
			tv.setId(matchIdx + 2000); // (bottom id same as top but offset by 1000)
			
			lp = new RelativeLayout.LayoutParams(
			        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.BELOW, matchIdx + 1000);
			lp.addRule(RelativeLayout.ALIGN_RIGHT, 1);
			lp.setMargins(0, 0, 0, 8);
			rl.addView(tv, lp);
			
		}
		
		// create higher tiers
		SessionMember dummySessionMember = new SessionMember();
		dummySessionMember.setPlayerSeed(-2);
		for (Integer i=sMembers.size()/2; i < sMembers.size()-1; i++) {
			matchIdx = i;
			Integer topParentMatch = getTopParentMatch(matchIdx);
			Integer bottomParentMatch = getTopParentMatch(matchIdx)+1;
			
			// upper half of match bracket
			tv = makeHalfBracket(sv.getContext(), dummySessionMember, true, false);
			tv.setId(matchIdx + 1000);

			lp = new RelativeLayout.LayoutParams(
			        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_LEFT, getTier(matchIdx)+1);
			lp.addRule(RelativeLayout.ALIGN_RIGHT, getTier(matchIdx)+1);
			lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParentMatch + 2000);
			lp.addRule(RelativeLayout.BELOW, topParentMatch + 1000);
			lp.setMargins(0, -2, 0, 0);
			rl.addView(tv, lp);
			
			// lower half of match bracket
			tv = makeHalfBracket(sv.getContext(), dummySessionMember, false, false);
			tv.setId(i + 2000);

			lp = new RelativeLayout.LayoutParams(
			        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_LEFT, getTier(matchIdx)+1);
			lp.addRule(RelativeLayout.ALIGN_RIGHT, getTier(matchIdx)+1);
			lp.addRule(RelativeLayout.ABOVE, bottomParentMatch + 2000);
			lp.addRule(RelativeLayout.BELOW, matchIdx + 1000);
			lp.setMargins(0, 0, 0, -2);
			rl.addView(tv, lp);
			
			Log.i("bracket", "Game " + i + ", topParent = " + topParentMatch + ", bottomParent = " + bottomParentMatch);
		}
		
		// create winner view
		Integer topParent = getTopParentMatch(sMembers.size()-1);
		tv = new TextView(sv.getContext());
		tv.setBackgroundDrawable(sv.getContext().getResources().getDrawable(R.drawable.bracket_endpoint));
		tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
		lp = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_LEFT, getTier(sMembers.size()-1)+1);
		lp.addRule(RelativeLayout.ALIGN_RIGHT, getTier(sMembers.size()-1)+1);
		lp.addRule(RelativeLayout.ALIGN_BOTTOM, topParent + 1000);
		lp.setMargins(0, 0, 0, -19);
		rl.addView(tv, lp);
	}
	
	public void refreshSingleElimBracket(){
		TextView tv;
		Integer matchIdx;
		View sv = findViewById(R.id.scrollView1);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.sDet_bracket);
		RelativeLayout.LayoutParams lp;
		
		// for players with byes, move their labeled view up to the next tier
		for (Integer i=0; i < sMembers.size()-1; i+=2) {
			matchIdx = i/2;
			if (sMembers.get(i+1).getPlayerSeed() == -1) {
				tv = (TextView) findViewById(matchIdx + 1000);
				tv.setText(null);
				tv.setBackgroundDrawable(null);

				tv = (TextView) findViewById(matchIdx + 2000);
				tv.setText(null);
				tv.setBackgroundDrawable(null);
				
				Integer childId = getChildBracketId(matchIdx);
				tv = (TextView) findViewById(childId);
				lp = (LayoutParams) tv.getLayoutParams();
				rl.removeView(tv);
				
				Boolean onTop = true;
				if (childId >= 2000) {onTop = false;}
				Log.i("bracket", "matchIdx: " + matchIdx + ", childId: " + childId);
				
				tv = makeHalfBracket(sv.getContext(), sMembers.get(i), onTop, true);
				tv.setId(childId);

				lp = new RelativeLayout.LayoutParams(
				        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.ALIGN_RIGHT, getTier(matchIdx)+2);
								
				if (onTop) {
					lp.addRule(RelativeLayout.ALIGN_TOP, matchIdx + 1000);
					if (matchIdx != 0) {
						lp.setMargins(0, -2, 0, 0);
					}
				} else {
					lp.addRule(RelativeLayout.BELOW, childId - 1000);
					lp.setMargins(0, 0, 0, -2);
				}

				rl.addView(tv, lp);
			}
		}
		
		// remove and re-add views in order so that they are drawn above lower tiers
		for (Integer i = 0; i < sMembers.size()-1; i++) {
			tv = (TextView) findViewById(i+1000);
			rl.removeView(tv);
			rl.addView(tv);
			
			tv = (TextView) findViewById(i+2000);
			rl.removeView(tv);
			rl.addView(tv);
		}
		
		// TODO: pull all games from the session and figure out how to manage those
		ForeignCollection<Game> sGames = s.getGames();
		Log.i("bracket", "session has " + sGames.size() + " games.");
//		((LinearLayout) findViewById(13)).getChildAt(0).getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
	}
	
	public void foldRoster() {
		// expand the list size to the next power of two
		Integer n = factorTwos(sMembers.size());
		while (sMembers.size() < Math.pow(2, n)) {
			SessionMember s = new SessionMember();
			s.setPlayerSeed(-1);
			sMembers.add(s);
		}
		List<SessionMember> tempRoster = new ArrayList<SessionMember>();
		for (Integer i=0; i < n-1; i++) {
			tempRoster.clear();
			for (Integer j=0; j < sMembers.size()/Math.pow(2, i+1); j++) {
				tempRoster.addAll(sMembers.subList(j*(int) Math.pow(2,i), (j+1)*(int) Math.pow(2,i)));
				tempRoster.addAll(sMembers.subList(sMembers.size()-(j+1)*(int) Math.pow(2, i), sMembers.size()-(j)*(int) Math.pow(2, i)));
			}
			sMembers.clear();
			sMembers.addAll(tempRoster);
		}
	}
	
	public Integer factorTwos(Integer rosterSize) {
		Integer n = 1;
		while (Math.pow(2,n) < rosterSize) {
			n++;
		}
		return n;
	}

	public void makeInvisibleHeaders(RelativeLayout rl) {
		// invisible headers are for spacing the bracket.
		Context context = rl.getContext();
		TextView tv;
		RelativeLayout.LayoutParams lp;
		
		// header for the labeled brackets on tier 0
		tv = new TextView(context);
		tv.setWidth(350);
		tv.setHeight(0);
		tv.setId(1);
		tv.setBackgroundColor(Color.BLACK);
		lp = new RelativeLayout.LayoutParams(
		        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
		rl.addView(tv, lp);
		
		// headers for the remaining tiers
		Integer nTiers = factorTwos(sMembers.size());
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// tier width = (screen width - label width - arbitrary side spacing) / number tiers
		Integer tierWidth = (metrics.widthPixels - 350 - 100) / nTiers;
		
		for (Integer i = 0; i < nTiers; i++) {
			tv = new TextView(context);
			tv.setWidth(tierWidth);
			tv.setHeight(0);
			tv.setId(i+2);
			tv.setBackgroundColor(Color.RED);
			lp = new RelativeLayout.LayoutParams(
			        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
			lp.addRule(RelativeLayout.RIGHT_OF, i+1);
			lp.setMargins(-14, 0, 0, 0);
			rl.addView(tv, lp);
		}
	}
			
	public TextView makeHalfBracket(Context context, SessionMember member, Boolean onTop, Boolean addLabels) {
		TextView tv = new TextView(context);
		
		Boolean isBye = member.getPlayerSeed() == -1;
		Boolean isUnset = member.getPlayerSeed() == -2;
		
		if (addLabels) {
			tv.setWidth(350);
			if (isBye) {
				tv.setHeight(10);
			} else {
				tv.setText("(" + String.valueOf(member.getPlayerSeed()+1) + ") " + member.getPlayer().getNickName() );
				if (onTop) {
					tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bracket_top_labeled));
				} else {
					tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bracket_bottom_labeled));
				}
			}
		} else if (!isBye) {
			if (onTop) {
				tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bracket_top));
			} else {
				tv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bracket_bottom));
			}
		}
		
		tv.setGravity(Gravity.RIGHT);
		tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
		if (isUnset) {
			// in this case, its actually an unset game
			tv.getBackground().setColorFilter(Color.LTGRAY, Mode.MULTIPLY);
		} else if (!isBye) {
			tv.getBackground().setColorFilter(member.getPlayer().getColor(), Mode.MULTIPLY);
		}
		
		return tv;
	}
	
	public Integer getTier(Integer matchIdx) {
		return ((Double) Math.floor(-Math.log(1-((double)matchIdx)/sMembers.size())/Math.log(2))).intValue();
	}
	
	public Integer getTopGameOfTier(Integer tier) {
		// note that tier numbering starts from 0 but games start from 1 
		// because games are identified by view id and must be positive integers
		return (int) (sMembers.size()*(1-Math.pow(2, -tier+1)));
	}
	
	public Integer getTopParentMatch(Integer matchIdx) {
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopGameOfTier(tier);
		Integer topOfPrevTier = getTopGameOfTier(tier-1);
		
		Integer topParentMatch = topOfPrevTier + 2*(matchIdx - topOfTier);
		return topParentMatch;
	}
	
	public Integer getChildBracketId(Integer matchIdx) {
		Integer tier = getTier(matchIdx);
		Integer topOfTier = getTopGameOfTier(tier);
		Integer topOfNextTier = getTopGameOfTier(tier+1);
		
		Integer childBracket = topOfNextTier + (matchIdx - topOfTier)/2 + 1000;
		if (matchIdx % 2 != 0) {
			childBracket += 1000;
		}
		return childBracket;
	}
}
