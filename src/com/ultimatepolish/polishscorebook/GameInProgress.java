package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.ThrowResult;
import com.ultimatepolish.scorebookdb.ThrowType;
import com.ultimatepolish.scorebookdb.Venue;

public class GameInProgress extends MenuContainerActivity 
								implements ThrowTableFragment.OnTableRowClickedListener{

	private FragmentArrayAdapter vpAdapter;
	private List<ThrowTableFragment> fragmentArray = new ArrayList<ThrowTableFragment>(0);
	private ViewPager vp;
	
	Game g;
	Player[] p = new Player[2]; 
	Session s;
	Venue v;
	
	Dao<Game, Long> gDao;
	Dao<Throw, Long>tDao; 
	List<Throw> throwsList; // = new ArrayList<Throw>(0);

	int throwIdx = 0;
	int currentThrowType = ThrowType.NOT_THROWN;
	
	// LISTENERS ==================================================
	private OnCheckedChangeListener checkboxChangedListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			updateThrow();
		}
	};

    private OnValueChangeListener numberPickerChangeListener = new OnValueChangeListener() {
		public void onValueChange(NumberPicker parent, int oldVal, int newVal) {
			updateThrow();
		}
	};

	public void onThrowClicked(int local_throw_nr){
		int global_throw_nr = ThrowTableFragment.localThrowIdxToGlobal(vp.getCurrentItem(), local_throw_nr);
		if (global_throw_nr > throwsList.size()) {
			global_throw_nr = throwsList.size() - 1;
		}
		gotoThrowIdx(global_throw_nr);
	}
	
    private class FragmentArrayAdapter extends FragmentPagerAdapter{

    	public FragmentArrayAdapter(FragmentManager fm) {
            super(fm);
        }
    	
		@Override
		public int getCount() {
			return fragmentArray.size();
		}
		
	    @Override
		public Fragment getItem(int position) {
	    	return fragmentArray.get(position);
		}

    }
    
    private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener{
		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			super.onPageScrollStateChanged(state);
		}
		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// TODO Auto-generated method stub
			super.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			TextView tv = (TextView) findViewById(R.id.textView_pageIndex);
			tv.setText("Page: " + String.valueOf(position+1));
			renderPage(pageIdx());
		}
    }

    public static class GentlemensDialogFragment extends DialogFragment{
    	@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Time out, Gentlemen!")
                   .setPositiveButton("resume", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // FIRE ZE MISSILES!
                       }
                   	});
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    
    // INITIALIZATION =============================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_in_progress);
		
		Intent intent = getIntent();
		Long gId = intent.getLongExtra("GID", -1);
		
		initGame(gId);
		initMetadata();
		initNumPickers();
		initListeners();
		
		Log.i("GIP", "onCreate() - about to create fragments");
		initTableFragments();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	@Override
	protected void onResume(){
		super.onResume();
		getThrowsFromDB();

		FragmentArrayAdapter ad = (FragmentArrayAdapter) vp.getAdapter(); 
		vp.setCurrentItem(0);
		Log.i("GIP", "onResume() - vp's adapter has " + ad.getCount() + " items");
		
		// change throw to the last throw
		int initThrowIdx = 0;
		if (throwsList.size() > 0){
			initThrowIdx = throwsList.size() - 1;
		}
		Log.i("GIP", "onResume() - About to change current throw idx to " + initThrowIdx);
		gotoThrowIdx(initThrowIdx, false);
	}
	@Override
	protected void onRestart(){
		super.onRestart();
		getThrowsFromDB();
	}	
	@Override
	protected void onPause() {
		super.onPause();
		saveAllThrows();
		saveGame();
		updateScoresFromThrowIdx(0);
	}
	@Override
    protected void onStop() {
    	super.onStop();
    }
	
	private void initGame(long gId){
		Context context = getApplicationContext();
		if (gId!=-1){
			try{
				gDao = Game.getDao(context);
				tDao = Throw.getDao(context);
				
				g = gDao.queryForId(gId);
				p = g.getPlayers(context);
				s = g.getSession(context);
				v = g.getVenue(context);
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
			
			vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
		}
	}
	private void initMetadata(){
		DateFormat df = new SimpleDateFormat("EEE MMM dd, yyyy @HH:mm", Locale.US);
		TextView tv;
		
		// player names
		tv = (TextView) findViewById(R.id.textView_players);
		tv.setText(p[0].getDisplayName() + " " + getString(R.string.gip_vs_text) + " " + p[1].getDisplayName());
		
		// session
		tv = (TextView) findViewById(R.id.textView_session);
		tv.setText(getString(R.string.gip_session_text) + " " + s.getSessionName());
		
		// venue
		tv = (TextView) findViewById(R.id.textView_venue);
		tv.setText(getString(R.string.gip_venue_text) + " " + v.getName());
		
		// date
		tv = (TextView) findViewById(R.id.textView_datePlayed);
		tv.setText(df.format(g.getDatePlayed()));
		
		// game ID
		tv = (TextView) findViewById(R.id.textView_gId);
		tv.setText(getString(R.string.gip_gamenum_text) + String.valueOf(g.getId()));
		
		// table header
		tv = (TextView) findViewById(R.id.header_p1);
		tv.setText(p[0].getNickName() );
		tv.setTextColor(ThrowTableRow.tableTextColor);
		tv.setTextSize(ThrowTableRow.tableTextSize);

		tv = (TextView) findViewById(R.id.header_p2);
		tv.setText(p[1].getNickName() );
		tv.setTextColor(ThrowTableRow.tableTextColor);
		tv.setTextSize(ThrowTableRow.tableTextSize);
	}
	private void initNumPickers(){
		// set ranges and text for the number pickers
		NumberPicker np;
		
		// catch type numberpicker
		np = (NumberPicker) findViewById(R.id.numPicker_catch);
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		String[] catchText = new String[3];
		catchText[0] = getString(R.string.gip_drop);
		catchText[1] = getString(R.string.gip_catch);
		catchText[2] = getString(R.string.gip_stalwart);
		np.setMinValue(0);
		np.setMaxValue(2);
		np.setValue(1);
		np.setDisplayedValues(catchText);
		np.setOnValueChangedListener(numberPickerChangeListener); 
		
		// error numberpicker
		np = (NumberPicker) findViewById(R.id.numPicker_errorScore);
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np.setMinValue(0);
		np.setMaxValue(3);
		np.setOnValueChangedListener(numberPickerChangeListener);
		
		// own goal numberpicker
		np = (NumberPicker) findViewById(R.id.numPicker_ownGoalScore);
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np.setMinValue(2); 
		np.setMaxValue(3);
		np.setOnValueChangedListener(numberPickerChangeListener);
		
		// goaltend numberpicker
		np = (NumberPicker) findViewById(R.id.numPicker_goaltendScore);
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np.setMinValue(2);
		np.setMaxValue(3);
		np.setOnValueChangedListener(numberPickerChangeListener);	
	}
	private void initListeners(){
		CheckBox cb;
		
		cb = (CheckBox) findViewById(R.id.checkBox_broken);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_drinkDrop);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_drinkHit);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_error);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_firedOn);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_goaltend);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_onFire);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_ownGoal);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_short);
		cb.setOnCheckedChangeListener(checkboxChangedListener);
		
		cb = (CheckBox) findViewById(R.id.checkBox_trap);
		cb.setOnCheckedChangeListener(checkboxChangedListener);	
	}
	private void getThrowsFromDB(){
		// get all the throws for this game from db
		HashMap<String,Object> m = new HashMap<String,Object>();
		m.put("gameId", g.getId());
		List<Throw> tList = null;
		try{
			 tList = tDao.queryForFieldValuesArgs(m);
		}
		catch (SQLException e){
			Toast.makeText(getApplicationContext(), 
			"error querying throw dao", 
			Toast.LENGTH_SHORT).show();
		}
		
		// sort the list by throw number if it isnt empty
		int maxThrowIdx = -1;
		Log.i("GIP", "initThrows() - tList has " + tList.size() + " elements.");
		if (!tList.isEmpty()) {
			Collections.sort(tList);
			// TODO: remove the following if-statement once negative throw numbers are deleted from all games in db
//			if (tList.get(0).getThrowIdx() < 0) {
//				tList.remove(0);
//			}
			tList.get(0).setInitialScores(); // make sure scores start at 0-0
			maxThrowIdx = tList.get(tList.size()-1).getThrowIdx();
			
			// verify that tList isnt corrupt by checking:
				// the first throw idx is 0
				if (tList.get(0).getThrowIdx() != 0) {
					Log.e("GIP", "getThrowsFromDB() - tList starts with throw "
							+ tList.get(0).getThrowIdx()+ " instead of 1");
				}
				// all throws have unique throw numbers
				for(int i = 0; i < (maxThrowIdx); i++){
					if (tList.get(i).getThrowIdx() == tList.get(i+1).getThrowIdx()) {
						Log.e("GIP", "getThrowsFromDB() - tList has duplicated throw idx (" 
								+ tList.get(i).getThrowIdx() + ")");
					}
				}
				// max throw number matches the size of tList
				if (maxThrowIdx != tList.size()-1) {
					Log.e("GIP", "getThrowsFromDB() - tList size doesnt match number of throws ");
				}
		}
	
		// push tList to global
		throwsList = tList;
		Log.i("GIP", "initThrows() - tList size doesnt match number of throws ");
		// update the view
		TextView tv = (TextView) findViewById(R.id.textView_throwCount);
		
		tv.setText("nThrows: "+ throwsList.size());
	}
	private void initTableFragments(){
		fragmentArray.clear();
		ThrowTableFragment frag = ThrowTableFragment.newInstance();
//		ThrowTableFragment.N_ROWS = 10;
        fragmentArray.add(frag);
        
        FragmentManager fragMan = getFragmentManager();
        vpAdapter = new FragmentArrayAdapter(fragMan);
        ViewPager vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(new MyPageChangeListener());
        
        vp.setCurrentItem(0);
        
        Log.i("GIP", "initTableFragments() - fragments created, adapter has " + vpAdapter.getCount() + " items");
        
        TextView tv = (TextView) findViewById(R.id.textView_pageIndex);
		tv.setText("Page: 1");
	}
	
    //=================================================================
	
	/////////////////////////////////////////////////////////
    /////////////// apply the state of the ui to a throw/////
    private void applyUIStateToCurrentThrow(Throw t){
    	Log.i("GIP", "applyUIStateToCurrentThrow() - Applying state to throw idx " + t.getThrowIdx());
    	applyCurrentThrowType(t);
    	applyCurrentThrowResult(t);
    	applySpecialMarks(t);
    	applyPreviousScores(t);
    }
	private void applyCurrentThrowType(Throw t){
		t.setThrowType(currentThrowType);
	}
	private void applyCurrentThrowResult(Throw t){
		NumberPicker np = (NumberPicker) findViewById(R.id.numPicker_catch);
		switch (np.getValue()) { 
		case 0:
			t.setThrowResult(ThrowResult.DROP);
			break;
		case 1:
			t.setThrowResult(ThrowResult.CATCH);
			break;
		case 2:
			t.setThrowResult(ThrowResult.STALWART);
			break;
		default:
			// TODO: error handling? 
		}		
	}
	private void applySpecialMarks(Throw t){
		t.isError = isError();
		t.isGoaltend = isGoaltend();
		t.isOwnGoal = isOwnGoal();
		if (t.isError){
			t.setErrorScore(getErrorScore());
		}
		if (t.isOwnGoal){
			t.setOwnGoalScore(getOwnGoalScore());
		}
		if (t.isGoaltend){
			t.setGoaltendScore(getGoaltendScore());
		}
		
		
		t.isShort = isShort();
		t.isTrap=isTrap();
		t.isBroken = isBroken();
		
		t.isDrinkDropped = isDrinkDrop();
		t.isDrinkHit = isDrinkHit();
		
		t.isOnFire=isOnFire();
		t.isFiredOn=isFiredOn();
	}
	private void applyPreviousScores(Throw t) {
		if (t.getThrowIdx() == 0) {
			t.setInitialScores();
		} else {
			t.setInitialScores(getPreviousThrow(t.getThrowIdx()));
		}	
	}
	//////////////////////////////////////////////////////////
	
	//-----------------------------------------------------------------
	//------------- set the state of the ui to the state of a throw----
	private void applyCurrentThrowToUIState(Throw t){
		setThrowType(t);
		setThrowResult(t);
		setSpecialMarks(t);
	}
	private void setSpecialMarks(Throw t){
		setIsError(t.isError);
		setIsOwnGoal(t.isOwnGoal);
		setIsGoaltend(t.isGoaltend);
		setErrorScore(t.getErrorScore());
		setOwnGoalScore(t.getOwnGoalScore());
		setGoaltendScore(t.getGoaltendScore());
		
		setIsShort(t.isShort);
		setIsTrap(t.isTrap);
		setIsBroken(t.isBroken);
		setIsDrinkHit(t.isDrinkHit);
		setIsDrinkDropped(t.isDrinkDropped);
		setIsOnFire(t.isOnFire);
		setIsFiredOn(t.isFiredOn);
	}
	private void setThrowType(Throw t){
		currentThrowType = t.getThrowType();
	}
	private void setThrowResult(Throw t) {
		NumberPicker np = (NumberPicker) findViewById(R.id.numPicker_catch);
		switch (t.getThrowResult()) { 
		case ThrowResult.DROP:
			np.setValue(0);
			break;
		case ThrowResult.CATCH:
			np.setValue(1);
			break;
		case ThrowResult.STALWART:
			np.setValue(2);
			break;
		default:
			// TODO: error handling? 
		}		
		
	}
	//-----------------------------------------------------------------
	
	//{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
	//{{{{{{{{{{{{{{{{{{{{{{{{{Draw the scores{{{{{{{{{{{{{{{{{{{{{{{

	//	private void renderThrows(){
//		for (int i=0;i<N_PAGES;i++){
//			renderPage(i);
//		}
//	}

	private void renderPage(int pidx){
		ThrowTableFragment frag;
		while (pidx >= fragmentArray.size()) {
			frag = ThrowTableFragment.newInstance();
        	fragmentArray.add(frag);
		}
		
		frag = fragmentArray.get(pidx);
		Log.i("GIP", "renderPage(pidx) - made fragment");
		int[] range = ThrowTableFragment.throwIdxRange(pidx);
		Log.i("GIP", "renderPage(pidx) - got throw range");
		frag.renderAsPage(pidx, throwsList);
		Log.i("GIP", "renderPage(pidx) - rendered as page");
		frag.clearHighlighted();
		Log.i("GIP", "renderPage(pidx) - cleared highlighted");
		
		if (throwIdx >= range[0] && throwIdx <= range[1]){
			frag.highlightThrow(throwIdx);
		}
	}
	
	private void updateCurrentScore(){
		Log.i("GIP", "updateCurrentScore(): About to get throwIdx " + String.valueOf(throwsList.size()-1));
		Throw lastThrow = getThrow(throwsList.size()-1);
		int[] scores = lastThrow.getFinalScores();
		if (lastThrow.isP1Throw()){
			g.setFirstPlayerScore(scores[0]);
			g.setSecondPlayerScore(scores[1]);
		}
		else{
			g.setFirstPlayerScore(scores[1]);
			g.setSecondPlayerScore(scores[0]);
		}
		
	}
	
	//{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{

	public void confirmThrow(){
		if ((throwIdx + 7) % 70 == 0) {
			Toast.makeText(getApplicationContext(), 
					"GTO in 3 innings", Toast.LENGTH_LONG).show();
		} else if ((throwIdx+1) % 70 == 0) {
			respectGentlemens();
		}
		gotoThrowIdx(throwIdx+1);
	}
	private void respectGentlemens(){
		GentlemensDialogFragment frag = new GentlemensDialogFragment();
		frag.show(getFragmentManager(), "gentlemens");
	}
		
	
	void gotoThrowIdx(int newThrowIdx){
		gotoThrowIdx(newThrowIdx, true);
	}
	void gotoThrowIdx(int newThrowIdx, boolean saveCurrent){
		Log.i("GIP", "gotoThrow() - Going from throw idx " + throwIdx + " to throw idx " + newThrowIdx + ".");

		Throw t;
		if (saveCurrent == true) {
			// Save the current throw based on UI
			Log.i("GIP", "gotoThrowIdx(): About to get throw idx " + throwIdx);
			t = getThrow(throwIdx);
			Log.i("GIP", "gotoThrow() - Retrieved throw " + t.getThrowIdx() + " from list.");
			
			applyUIStateToCurrentThrow(t);
			
			try{
				saveThrow(t);
			}
			catch (SQLException e){
//				Toast.makeText(getApplicationContext(), 
//				"Could not save throw at idx " + throwIdx + ", " + e.getMessage(), 
//				Toast.LENGTH_LONG).show();
				Log.e("GIP", "gotoThrow() - " + e.getMessage());
			}
		}
		
		
		// Update the scores
		updateScoresFromThrowIdx(0);
		
		// Go to throw newThrowNr
		throwIdx = newThrowIdx;
//		Log.i("GIP", "gotoThrow() - throwIdx is now " + throwIdx);
		
		Log.i("GIP", "gotoThrowIdx(): About to get throw idx " + throwIdx);
		t = getThrow(throwIdx);
//		Log.i("GIP", "gotoThrow() - Retrieved throw " + t.getThrowNumber() + " from list.");
		
		applyCurrentThrowToUIState(t);
		
		ViewPager vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
		FragmentArrayAdapter ad = (FragmentArrayAdapter) vp.getAdapter();
		
		Log.i("GIP", "gotoThrow() - vp's adapter has  " + ad.getCount() + " items");
		try{
			vp.setCurrentItem(pageIdx(throwIdx));			
			renderPage(pageIdx(throwIdx));
			Log.i("GIP", "gotoThrow() - Changed to page " + pageIdx(throwIdx) + ".");
		}
		catch (NullPointerException e){
			Log.e("GIP", "gotoThrow() - Failed to change to page " + pageIdx(throwIdx) + ".");
		}
		
		updateCurrentScore();
		saveGame();
		
	}
	private void updateScoresFromThrow(Throw t){
		updateScoresFromThrowIdx(t.getThrowIdx());
	}
	private void updateScoresFromThrowIdx(int throwIdx){
		Throw t,u;
		Log.i("GIP", "updateScoresFromThrowIdx(throwIdx): Updating scores from throw idx " + throwIdx);
		if (throwIdx <= 0 && throwsList.size() != 0) {
//			Log.i("GIP", "updateScoresFromThrowIdx(): About to get throw idx " + throwIdx);
			t = getThrow(0);
			t.setInitialScores();
//			Log.i("GIP", "Setting initial scores of throw " + t.getThrowNumber() + " to 0-0");
			throwIdx = 1;
		}
		for (int i = throwIdx; i < throwsList.size(); i++){
//			Log.i("GIP", "updateScoresFromThrowIdx(): About to get throw idx " + i);
			t = getThrow(i);
			u = getPreviousThrow(i);
			t.setInitialScores(u);
//			Log.i("GIP", "Setting initial scores of throw " + t.getThrowNumber()
//					+ " to final scores of throw " + u.getThrowNumber());
		}
	}
	void saveAllThrows(){
		for(Throw t: throwsList){
			try{
				saveThrow(t);
			}
			catch(SQLException e){
				Toast.makeText(getApplicationContext(), 
						"could not save throw "+t.getThrowIdx()+", "+e.getMessage(), 
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	void saveThrow(Throw t) throws SQLException{
		HashMap<String,Object> m = new HashMap<String,Object>();
		m.put(Throw.THROW_NUMBER, t.getThrowIdx());
		m.put(Throw.GAME_ID, t.getGameId());
		List<Throw> tList = tDao.queryForFieldValuesArgs(m);
		if (tList.isEmpty()){
			tDao.create(t);
			Log.i("GIP", "saveThrow(Throw) - Throw idx " + t.getThrowIdx() + " not found in db, did not save.");
		}
		else{
			t.setId(tList.get(0).getId());
			tDao.update(t);
			Log.i("GIP", "saveThrow(Throw) - Saved throw idx " + t.getThrowIdx());
		}
	}
	
	void saveGame(){
		try{
			gDao.update(g);
		}
		catch (SQLException e){
			Toast.makeText(getApplicationContext(), 
					"could not save game, "+e.getMessage(), 
					Toast.LENGTH_LONG).show();
		}
	}
		
	
	public void buttonPressed(View view){
		int buttonId = ((Button) view).getId();
		switch (buttonId) {
			case R.id.gip_button_high:
				currentThrowType = ThrowType.BALL_HIGH;
				break;
			case R.id.gip_button_low:
				currentThrowType = ThrowType.BALL_LOW;
				break;
			case R.id.gip_button_left:
				currentThrowType = ThrowType.BALL_LEFT;
				break;
			case R.id.gip_button_right:
				currentThrowType = ThrowType.BALL_RIGHT;
				break;
			case R.id.gip_button_strike:
				currentThrowType = ThrowType.STRIKE;
				break;
			case R.id.gip_button_bottle:
				currentThrowType = ThrowType.BOTTLE;
				break;
			case R.id.gip_button_pole:
				currentThrowType = ThrowType.POLE;
				break;
			case R.id.gip_button_cup:
				currentThrowType = ThrowType.CUP;
				break;
		}
		
//		Log.i("GIP", "buttonPressed(view): " + currentThrowType);
		updateThrow();
		confirmThrow();
	}
	
	private void updateThrow(){
		Log.i("GIP", "updateThrow(): Updating throw at idx " + throwIdx);
		Throw t = getThrow(throwIdx);
		applyUIStateToCurrentThrow(t);
		updateScoresFromThrowIdx(0);
		renderPage(pageIdx(throwIdx));
	}
	
	Throw getThrow(int throwIdx){
		if (throwIdx >= 0 && throwIdx < throwsList.size()){
			// throwNr is a prior throw
//			Log.i("GIP", "getThrow(): Getting prior throw at idx " + throwIdx);
			return throwsList.get(throwIdx);
		}
		else if(throwIdx == throwsList.size()){
			// throw number is the next throw
			// TODO: start as a new type "NOTTHROWN"
//			Log.i("GIP", "getThrow(): Making a new throw at idx " + throwIdx);
			Throw t = g.makeNewThrow(throwIdx);
			t.setThrowType(ThrowType.STRIKE);
			t.setThrowResult(ThrowResult.CATCH);
			throwsList.add(t);
			TextView tv = (TextView) findViewById(R.id.textView_throwCount);
			tv.setText("nThrows: " + throwsList.size());
			return t;
		}
		else{
			throw new RuntimeException("Tried to retrieve an invalid throw at idx "
					+ throwIdx + "/" + String.valueOf(throwsList.size()-1));
		}
	}
	Throw getPreviousThrow(int throwIdx){
		Throw t;
		if (throwIdx > 0){
			t =  throwsList.get(throwIdx-1);
		}
		else{
			throw new RuntimeException("Tried to retrieve an invalid throw at idx "
					+ throwIdx + "/" + String.valueOf(throwsList.size()-1));
		}
		return t;
	}
	Throw getPreviousThrow(){
		return getPreviousThrow(throwIdx);
	}
	Throw getNextThrow(int throwIdx){
		return getThrow(throwIdx+1);
	}
//	ThrowTableFragment getCurrentFragment(){
//		return fragArray.get(page_idx);
//	}
//	ThrowTableRow getThrowTableRow(Throw t){
//		int pidx = ThrowTableFragment.throwNrToPageIdx(t.getThrowNumber());
//		try{
//			return fragArray.get(pidx).getTableRow(t.getThrowNumber());
//		}
//		catch (ArrayIndexOutOfBoundsException e){
//			throw new RuntimeException("wrong page idx for throw nr "+
//					t.getThrowNumber()+", pidx = "+pidx+": "+e.getMessage());
//		}
//		
//	}
	
	int pageIdxMax() {
		return throwsList.size() / (2*ThrowTableFragment.N_ROWS);
	}	
	int pageIdx(int throwIdx) {
		if (throwIdx > throwsList.size()) {
			throwIdx = throwsList.size();
		}	
		int pidx = (throwIdx) / (2*ThrowTableFragment.N_ROWS);
		if (pidx < 0) {pidx = 0;}
		Log.i("GIP", "pageIdx(int) - Index is " + pidx + ".");
		return pidx;
	}
	int pageIdx() {
		return pageIdx(throwsList.size());
	}
	
	boolean isError(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_error);
		return cb.isChecked();
	}
	boolean isOwnGoal(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_ownGoal);
		return cb.isChecked();
	}
	boolean isGoaltend(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_goaltend);
		return cb.isChecked();
	}
	int getErrorScore(){
		NumberPicker np = (NumberPicker) findViewById(R.id.numPicker_errorScore);
		return np.getValue();
	}
	int getOwnGoalScore(){
		NumberPicker np = (NumberPicker) findViewById(R.id.numPicker_ownGoalScore);
		return np.getValue();
	}
	int getGoaltendScore(){
		NumberPicker np = (NumberPicker) findViewById(R.id.numPicker_goaltendScore);
		return np.getValue();
	}
	boolean isShort(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_short);
		return cb.isChecked();
	}
	boolean isTrap(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_trap);
		return cb.isChecked();
	}
	boolean isBroken(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_broken);
		return cb.isChecked();
	}
	boolean isFiredOn(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_firedOn);
		return cb.isChecked();
	}
	boolean isOnFire(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_onFire);
		return cb.isChecked();
	}
	boolean isDrinkHit(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_drinkHit);
		return cb.isChecked();
	}
	boolean isDrinkDrop(){
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox_drinkDrop);
		return cb.isChecked();
	}

	private void setIsFiredOn(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_firedOn)).setChecked(b);
	}
	private void setIsOnFire(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_onFire)).setChecked(b);
	}
	private void setIsDrinkDropped(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_drinkDrop)).setChecked(b);
	}
	private void setIsDrinkHit(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_drinkHit)).setChecked(b);
	}
	private void setIsBroken(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_broken)).setChecked(b);
	}
	private void setIsTrap(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_trap)).setChecked(b);
	}
	private void setIsShort(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_short)).setChecked(b);
	}
	
	private void setIsGoaltend(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_goaltend)).setChecked(b);
	}
	private void setIsOwnGoal(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_ownGoal)).setChecked(b);
	}
	private void setIsError(boolean b) {
		((CheckBox) findViewById(R.id.checkBox_error)).setChecked(b);
	}
	private void setGoaltendScore(int score) {
		NumberPicker p = (NumberPicker) findViewById(R.id.numPicker_goaltendScore);
		p.setValue(score);
	}
	private void setOwnGoalScore(int score) {
		NumberPicker p = (NumberPicker) findViewById(R.id.numPicker_ownGoalScore);
		p.setValue(score);
	}
	private void setErrorScore(int score) {
		NumberPicker p = (NumberPicker) findViewById(R.id.numPicker_errorScore);
		p.setValue(score);
	}
	
}
