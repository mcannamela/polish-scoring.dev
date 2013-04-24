package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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
	
	private static int N_PAGES = 10;
	
	private FragmentArrayAdapter vpAdapter;
	private ArrayList<ThrowTableFragment> fragArray = new ArrayList<ThrowTableFragment>(N_PAGES);
	int page_idx = 0;
	
	Game g;
	Player[] p = new Player[2]; 
	Session s;
	Venue v;
	
	Dao<Game, Long> gDao;
	Dao<Throw, Long>tDao; 
	ArrayList<Throw> throwArray = new ArrayList<Throw>(100);

	int throwNr = -1;
	int currentThrowType = ThrowType.STRIKE;
	
	
	ArrayList<Integer> errorScores = new ArrayList<Integer>();
	ArrayList<Integer> ownGoalScores = new ArrayList<Integer>();
	ArrayList<Integer> goaltendScores = new ArrayList<Integer>();
	
	NumberPicker errorNumPicker;
	NumberPicker ownGoalNumPicker;
	NumberPicker goaltendNumPicker;
	
	//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	//:::::::::::::::listeners:::::::::::::::::::::::::::::::::::::::::
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
		int global_throw_nr = ThrowTableFragment.localThrowNrToGlobal(page_idx, local_throw_nr);
		changeCurrentThrow(global_throw_nr);
		
	}

    private class FragmentArrayAdapter extends FragmentPagerAdapter{

    	public FragmentArrayAdapter(FragmentManager fm) {
            super(fm);
        }
    	
		@Override
		public int getCount() {
			return fragArray.size();
		}
		
	    @Override
		public Fragment getItem(int position) {
	    	return fragArray.get(position);
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
			tv.setText("page: "+position);
			page_idx = position;
			renderPage(page_idx);
		}
    	
    }
    
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	
    
    //========================= initialization =======================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_in_progress);
		
		Intent intent = getIntent();
		Long gid = intent.getLongExtra("GID", -1);
		
		initGame(gid);
		initThrows();
		
		initMetadata();
		initNumPickers();
		initListeners();
		initTableHeaders();
		
		Log.i("GIP", "onCreate() - about to create fragments");
		initTableFragments();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	@Override
	protected void onResume(){
		super.onResume();
		initThrows();
		
		ViewPager vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
		FragmentArrayAdapter ad = (FragmentArrayAdapter) vp.getAdapter(); 
		vp.setCurrentItem(0);
		assert page_idx==0;
		Log.i("GIP", "onResume() - vp's adapter has  "+ad.getCount() +" items");
		
		int initThrowNr = 0;
		if (throwArray.size()>0){
			initThrowNr = throwArray.size()-1;
		}
		Log.i("GIP", "onResume() - about to change current throw to "+initThrowNr);
		changeCurrentThrow(initThrowNr);
		
	}
	@Override
	protected void onRestart(){
		super.onRestart();
//		initThrows();
	}	
	
	@Override
	protected void onPause() {
		super.onPause();
		saveAllThrows();
		saveGame();
		updateThrowScoresFrom(0);
	}
	@Override
    protected void onStop() {
    	super.onStop();
    }
	
	private void initGame(long gid){
		Context context = getApplicationContext();
		if (gid!=-1){
			try{
				gDao = Game.getDao(context);
				tDao = Throw.getDao(context);
				
				g = gDao.queryForId(gid);
				p = g.getPlayers(context);
				s = g.getSession(context);
				v = g.getVenue(context);
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
						e.getMessage(), 
						Toast.LENGTH_LONG).show();
			}
		}
	}
	private void initMetadata(){
		DateFormat df = new SimpleDateFormat("EEE MMM dd, yyyy @HH:mm");
		TextView tv;
		
		tv = (TextView) findViewById(R.id.textView_players);
		tv.setText(p[0].getDisplayName() + " " + getString(R.string.gip_vs_text) + " " + p[1].getDisplayName());
		
		tv = (TextView) findViewById(R.id.textView_session);
		tv.setText(getString(R.string.gip_session_text) + " " + s.getSessionName());
		
		tv = (TextView) findViewById(R.id.textView_venue);
		if (v!=null){
			tv.setText(getString(R.string.gip_venue_text) + " " + v.getName());
		}
		else{
			tv.setText(getString(R.string.gip_venue_text) + " " + "vIsNull");
		}
		
		tv = (TextView) findViewById(R.id.textView_datePlayed);
		tv.setText(df.format(g.getDatePlayed()));
		
		tv = (TextView) findViewById(R.id.textView_gid);
		tv.setText(getString(R.string.gip_gamenum_text) + String.valueOf(g.getId()));
	}
	private void initTableFragments(){
		fragArray.clear();
		ThrowTableFragment frag = null;
        for (int i=0;i<N_PAGES;i++){
        	frag = ThrowTableFragment.newInstance();
        	fragArray.add(frag);
        }
        
        FragmentManager fragMan = getFragmentManager();
        vpAdapter = new FragmentArrayAdapter(fragMan);
        ViewPager vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(new MyPageChangeListener());
        
        vp.setCurrentItem(0);
        Log.i("GIP", "initTableFragments() - fragments created, adapter has "+vpAdapter.getCount() +"items");
	}
	private void initThrows(){

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
		
		if (tList.isEmpty()) {
			Toast.makeText(getApplicationContext(), 
			"throw list is empty", 
			Toast.LENGTH_SHORT).show();
		}
		else {
			Collections.sort(tList);
			tList.get(0).setInitialScores(g.makeNewThrow(-1));
		}
		
		throwArray.clear();
		//determine max throw number
		int maxThrowNr = 0;
		for (Throw t: tList){
			if (t.getThrowNumber()>maxThrowNr){
				maxThrowNr = t.getThrowNumber();
			}
		}
		//fill array with nulls
		for(int i = 0; i<(maxThrowNr+1);i++){
			throwArray.add(null);
		}
		//insert all valid throws into the 
		int idx;
		for (Throw t: tList){
			idx = t.getThrowNumber();
			if (idx>0){
				throwArray.set(idx, t);
			}
			else{
				try{
					tDao.delete(t);
					Log.i("GIP", "initThrows() - deleted a throw with a negative throw number");
				}
				catch (SQLException e){
					Log.e("GIP", "initThrows() - failed to delete a throw with a negative throw number");
				}
			}
		}
		
		//fill any nulls with a caught strike
		Throw t;
		for (int i=0; i<throwArray.size();i++){
			t = throwArray.get(i);
			if (t==null){
				Log.i("GIP", "initThrows() - missing throw number "+i+", will be inserted");
				t = g.makeNewThrow(throwNr);
				t.setThrowType(ThrowType.STRIKE);
				t.setThrowResult(ThrowResult.CATCH);
				t.setThrowNumber(i);
				throwArray.set(i,t);
			}
		}
		
		TextView tv = (TextView) findViewById(R.id.textView_throwCount);
		tv.setText("nThrows: "+throwArray.size());
	
	}
	private void initTableHeaders(){
		TextView tv;
		
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
		NumberPicker np = (NumberPicker) findViewById(R.id.numPicker_catch);
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
		
		NumberPicker p; 
		
		p = (NumberPicker) findViewById(R.id.numPicker_errorScore);
		p.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		p.setMinValue(0);
		p.setMaxValue(3);
		errorNumPicker = p;
		p.setOnValueChangedListener(numberPickerChangeListener);
		
		p = (NumberPicker) findViewById(R.id.numPicker_ownGoalScore);
		p.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		p.setMinValue(2); 
		p.setMaxValue(3);
		ownGoalNumPicker = p;
		p.setOnValueChangedListener(numberPickerChangeListener);
		
		p = (NumberPicker) findViewById(R.id.numPicker_goaltendScore);
		p.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		p.setMinValue(2);
		p.setMaxValue(3);
		goaltendNumPicker = p;
		p.setOnValueChangedListener(numberPickerChangeListener);
		
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
	
    //=================================================================
	
	/////////////////////////////////////////////////////////
    /////////////// apply the state of the ui to a throw/////
    private void applyUIStateToCurrentThrow(Throw t){
    	Log.i("GIP", "applyUIStateToCurrentThrow() - applying state to throw "+t.getThrowNumber());
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
		t.setInitialScores(getPreviousThrow(t.getThrowNumber()));
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
		ThrowTableFragment frag =fragArray.get(pidx); 
		int[] range = ThrowTableFragment.throwNrRange(pidx);
		frag.renderAsPage(pidx, throwArray);
		frag.clearHighlighted();
		
		if (throwNr>=range[0] && throwNr<range[1]){
			frag.highlightThrow(throwNr);
		}
	}
	
	private void updateCurrentScore(){
		Throw lastThrow = getThrow(throwArray.size()-1);
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
		changeCurrentThrow(throwNr+1);
	}
		
	void changeCurrentThrow(int newThrowNr){
		Log.i("GIP", "changeCurrentThrow() - current throw nr is "+throwNr +", will change it to "+newThrowNr);
		Throw t = getThrow(throwNr);
		Log.i("GIP", "changeCurrentThrow() - retrieved throw "+t.getThrowNumber()+" from array");
		applyUIStateToCurrentThrow(getThrow(throwNr));
		
		if (throwNr>=0){
			try{
				saveThrow(getThrow(throwNr));
			}
			catch (SQLException e){
				Toast.makeText(getApplicationContext(), 
				"could not save throw "+throwNr+", "+e.getMessage(), 
				Toast.LENGTH_LONG).show();
			}
		}
		updateThrowScoresFrom(0);
		
		int oldThrowNr = throwNr;
		throwNr = newThrowNr;
		Log.i("GIP", "changeCurrentThrow() - throwNr is now "+throwNr);
		
		t = getThrow(throwNr);
		Log.i("GIP", "changeCurrentThrow() - retrieved throw "+t.getThrowNumber()+" from array");
		
		applyCurrentThrowToUIState(t);
		
		
		int new_page_idx = ThrowTableFragment.throwNrToPageIdx(newThrowNr);
		ViewPager vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
		FragmentArrayAdapter ad = (FragmentArrayAdapter) vp.getAdapter(); 
//		Log.i("GIP", "changeCurrentThrow() - vp's adapter has  "+ad.getCount() +" items");
		try{
			vp.setCurrentItem(new_page_idx);
			assert page_idx==new_page_idx;
			
			renderPage(page_idx);
		}
		catch (NullPointerException e){
			Log.e("GIP", "changeCurrentThrow() - failed to change page");
		}
		
		updateCurrentScore();
		saveGame();
		
	}
	private void updateThrowScoreFrom(Throw t){
		updateThrowScoresFrom(t.getThrowNumber());
	}
	private void updateThrowScoresFrom(int throwNr){
		Throw t,u;
		for (int i=throwNr; i<throwArray.size(); i++){
			t = getThrow(i);
			u = getPreviousThrow(i);
			t.setInitialScores(u);
			Log.i("GIP", "Setting initial scores of throw "+t.getThrowNumber()+" to final scores of throw "+u.getThrowNumber());
		}
	}
	void saveAllThrows(){
		for(Throw t: throwArray){
			try{
				saveThrow(t);
			}
			catch(SQLException e){
				Toast.makeText(getApplicationContext(), 
						"could not save throw "+t.getThrowNumber()+", "+e.getMessage(), 
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	void saveThrow(Throw t) throws SQLException{
		
		HashMap<String,Object> m = new HashMap<String,Object>();
		m.put(Throw.THROW_NUMBER, t.getThrowNumber());
		m.put(Throw.GAME_ID, t.getGameId());
		List<Throw> tList = tDao.queryForFieldValuesArgs(m);
		if (tList.isEmpty()){
			tDao.create(t);
		}
		else{
			t.setId(tList.get(0).getId());
			tDao.update(t);
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
		String buttonText = (String) ((Button) view).getText();
		
		if (buttonText.equals(getString(R.string.gip_ballHigh_button))){		
			currentThrowType = ThrowType.BALL_HIGH;
		}
		if (buttonText.equals(getString(R.string.gip_ballLow_button))){
			currentThrowType = ThrowType.BALL_LOW;
		}
		if (buttonText.equals(getString(R.string.gip_ballLeft_button))){
			currentThrowType = ThrowType.BALL_LEFT;
		}
		if (buttonText.equals(getString(R.string.gip_ballRight_button))){
			currentThrowType = ThrowType.BALL_RIGHT;
		}
		if (buttonText.equals(getString(R.string.gip_strike_button))){
			currentThrowType = ThrowType.STRIKE;
		}
		if (buttonText.equals(getString(R.string.gip_bottle_button))){
			currentThrowType = ThrowType.BOTTLE;
		}
		if (buttonText.equals(getString(R.string.gip_pole_button))){
			currentThrowType = ThrowType.POLE;
		}
		if (buttonText.equals(getString(R.string.gip_cup_button))){
			currentThrowType = ThrowType.CUP;
		}
		
		updateThrow();
		confirmThrow();
	}
	
	private void updateThrow(){
		Throw t = getThrow(throwNr);
		applyUIStateToCurrentThrow(t);
		updateThrowScoresFrom(0);
		renderPage(page_idx);
	}
	
	
		
	Throw getThrow(int throwNr){
		if (throwNr<throwArray.size() && throwNr>=0){
			return throwArray.get(throwNr);
		}
		else if(throwNr==throwArray.size() ){
			Throw t = g.makeNewThrow(throwNr);
			t.setThrowType(ThrowType.STRIKE);
			t.setThrowResult(ThrowResult.CATCH);
			throwArray.add(t);
			TextView tv = (TextView) findViewById(R.id.textView_throwCount);
			tv.setText("nThrows: "+throwArray.size());
			return t;
		}
		else if (throwNr==-1){
			Throw t = g.makeNewThrow(throwNr);
			t.setThrowType(ThrowType.STRIKE);
			t.setThrowResult(ThrowResult.CATCH);
			return t;
		}
		else{
			throw new RuntimeException("tried to get a throw from the "+
						"future, throwNr "+throwNr +" of "+throwArray.size());
		}
	}
	
	Throw getPreviousThrow(int throwNr){
		Throw t;
		if (throwNr>0){
			t =  throwArray.get(throwNr-1);
		}
		else{
			t = g.makeNewThrow(-1);
			t.setThrowResult(ThrowResult.CATCH);
			t.setThrowType(ThrowType.STRIKE);
		}
		return t;
	}
	Throw getPreviousThrow(){
		return getPreviousThrow(throwNr);
	}
	
	Throw getNextThrow(int throwNr){
		return getThrow(throwNr+1);
	}
	ThrowTableFragment getCurrentFragment(){
		return fragArray.get(page_idx);
	}
	ThrowTableRow getThrowTableRow(Throw t){
		int pidx = ThrowTableFragment.throwNrToPageIdx(t.getThrowNumber());
		try{
			return fragArray.get(pidx).getTableRow(t.getThrowNumber());
		}
		catch (ArrayIndexOutOfBoundsException e){
			throw new RuntimeException("wrong page idx for throw nr "+
					t.getThrowNumber()+", pidx = "+pidx+": "+e.getMessage());
		}
		
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
		int val = errorNumPicker.getValue();
		return val;
	}
	int getOwnGoalScore(){
		int val = ownGoalNumPicker.getValue();
		return val;
	}
	int getGoaltendScore(){
		int val = goaltendNumPicker.getValue();
		return val;
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
