package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.ActiveGame;
import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Session;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.ThrowResult;
import com.ultimatepolish.scorebookdb.ThrowType;
import com.ultimatepolish.scorebookdb.Venue;

public class GameInProgress extends MenuContainerActivity 
								implements ThrowTableFragment.OnTableRowClickedListener{

	public static String LOGTAG = "GIP";
	private FragmentArrayAdapter vpAdapter;
	private List<ThrowTableFragment> fragmentArray = new ArrayList<ThrowTableFragment>(0);
	private ViewPager vp;
	
	Game g;
	ActiveGame ag;
	Throw uiThrow;
	Player[] p = new Player[2]; 
	Session s;
	Venue v;
	
	Dao<Game, Long> gDao;
	Dao<Throw, Long>tDao; 

	int currentThrowType = ThrowType.NOT_THROWN;
	
	// LISTENERS ==================================================
	private OnCheckedChangeListener checkboxChangedListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			updateActiveThrow();
		}
	};

    private OnValueChangeListener numberPickerChangeListener = new OnValueChangeListener() {
		public void onValueChange(NumberPicker parent, int oldVal, int newVal) {
			updateActiveThrow();
		}
	};

	private OnLongClickListener mLongClickListener = new OnLongClickListener() {
		@Override
        public boolean onLongClick(View view) {
			log("mLongClickListener(): " + view.getContentDescription() + " was long pressed");
			int buttonId = view.getId();
			
			switch (buttonId) {
				case R.id.gip_button_strike:
					if (isDrinkHit() == false) {
						setIsDrinkHit(true);
					} else {
						setIsDrinkHit(false);
					}
					break;
				case R.id.gip_button_bottle:
					if (isBroken() == false) {
						setIsBroken(true);
					} else {
						setIsBroken(false);
					}
					break;
				default:
					if (isShort() == false) {
						setIsShort(true);
					} else {
						setIsShort(false);
					}
					break;
			}
			
			buttonPressed(view);
            return true;
        }
	};
	
	private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener{
		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			renderPage(position, false);
		}
    }
	
	
	
	public void onThrowClicked(int local_throw_idx){
		int global_throw_idx = ThrowTableFragment.localThrowIdxToGlobal(vp.getCurrentItem(), local_throw_idx);
		if (global_throw_idx > ag.nThrows() - 1) {
			global_throw_idx = ag.nThrows() - 1;
		}
		gotoThrowIdx(global_throw_idx);
	}
	
	public void buttonPressed(View view){
		log("buttonPressed(): " + view.getContentDescription() + " was pressed");
		int buttonId = view.getId();
		
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
		
//		updateActiveThrow();
		confirmThrow();
	}
	//==================================================
	
//############################### INNER CLASSES ############################
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
	    
	    @Override
		public CharSequence getPageTitle(int position) {
//	    	tv.setText("nThrows: "+ throwsList.size());
	    	
	    	String title = "Page " + String.valueOf(position+1);
			return title;
		}

    }
    
    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
	    private float MIN_SCALE = 0.85f;
	    private float MIN_ALPHA = 0.5f;

	    public void transformPage(View view, float position) {
	        int pageWidth = view.getWidth();
	        int pageHeight = view.getHeight();

	        if (position < -1) { // [-Infinity,-1)
	            // This page is way off-screen to the left.
	            view.setAlpha(0);

	        } else if (position <= 1) { // [-1,1]
	            // Modify the default slide transition to shrink the page as well
	            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
	            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
	            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
	            if (position < 0) {
	                view.setTranslationX(horzMargin - vertMargin / 2);
	            } else {
	                view.setTranslationX(-horzMargin + vertMargin / 2);
	            }

	            // Scale the page down (between MIN_SCALE and 1)
	            view.setScaleX(scaleFactor);
	            view.setScaleY(scaleFactor);

	            // Fade the page relative to its size.
	            view.setAlpha(MIN_ALPHA +
	                    (scaleFactor - MIN_SCALE) /
	                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));

	        } else { // (1,+Infinity]
	            // This page is way off-screen to the right.
	            view.setAlpha(0);
	        }
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
                       }
                   	});
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
  //#################################################################
    
    // ANDROID CALLBACKS =============================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate(): creating GIP");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_in_progress);
		
		Intent intent = getIntent();
		Long gId = intent.getLongExtra("GID", -1);
		
		initGame(gId);
		initMetadata();
		initNumPickers();
		initListeners();
		
		log("onCreate(): about to create fragments");
		initTableFragments();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	@Override
	protected void onResume(){
		super.onResume();
		log("onResume(): vp's adapter has " + vpAdapter.getCount() + " items");
		gotoThrowIdx(ag.getActiveIdx());
	}
	@Override
	protected void onRestart(){
		super.onRestart();
	}	
	@Override
	protected void onPause() {
		super.onPause();
		ag.saveAllThrows();
		saveGame(true);
	}
	@Override
    protected void onStop() {
    	super.onStop();
    }
	//=================================================================
	
	// INITIALIZATION =============================================
	private void initGame(long gId){
		Context context = getApplicationContext();
		if (gId!=-1){
			try{
				gDao = Game.getDao(context);
				tDao = Throw.getDao(context);
				
				g = gDao.queryForId(gId);
				ag = new ActiveGame(g, context);
				uiThrow = ag.getActiveThrow();
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
		
		View view;
		view = findViewById(R.id.gip_button_high);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_left);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_right);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_low);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_strike);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_bottle);
		view.setOnLongClickListener(mLongClickListener);
		
	}

	private void initTableFragments(){
		fragmentArray.clear();
		
//		ThrowTableFragment.N_ROWS = 10;

		ThrowTableFragment frag = ThrowTableFragment.newInstance(0, getApplicationContext());
		fragmentArray.add(frag);

        vpAdapter = new FragmentArrayAdapter(getFragmentManager());
        vp = (ViewPager) findViewById(R.id.viewPager_throwsTables);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(new MyPageChangeListener());
//        vp.setPageTransformer(true, new ZoomOutPageTransformer());
        
//        vp.setCurrentItem(0);
//        log("initTableFragments() - Viewpager has limit of " + vp.getOffscreenPageLimit());
//        log("initTableFragments() - fragments created, adapter has " + vpAdapter.getCount() + " items");
	}
	
    //=================================================================
	
	
	//STATE LOGIC AND PROGRAM FLOW +++++++++++++++++++++++++++++++++++++
	void updateActiveThrow(){
		log("updateThrow(): Updating throw at idx " + ag.getActiveIdx());
		applyUIStateToActiveThrow();
		renderPage(getPageIdx(ag.getActiveIdx()));
	}
	
	void confirmThrow(){
		int activeIdx = ag.getActiveIdx();
		if ((activeIdx + 7) % 70 == 0) {
			Toast.makeText(getApplicationContext(), 
					"GTO in 3 innings", Toast.LENGTH_LONG).show();
		} else if ((activeIdx+1) % 70 == 0) {
			respectGentlemens();
		}
		gotoThrowIdx(activeIdx+1);
	}
	
	void gotoThrowIdx(int newActiveIdx){
		log("gotoThrow() - Going from throw idx " + ag.getActiveIdx() + " to throw idx " + newActiveIdx + ".");
		
		applyUIStateToActiveThrow();
		ag.setActiveIdx(newActiveIdx);
		applyActiveThrowToUIState();
		
		int idx = ag.getActiveIdx();
		assert idx == newActiveIdx;
		try{			
			renderPage(getPageIdx(idx));
			log("gotoThrow() - Changed to page " + getPageIdx(idx) + ".");
		}
		catch (NullPointerException e){
			loge("gotoThrow() - Failed to change to page " + getPageIdx(idx) + ".", e);
		}
		
		ag.saveGame();
	}
	
	private void respectGentlemens(){
		GentlemensDialogFragment frag = new GentlemensDialogFragment();
		frag.show(getFragmentManager(), "gentlemens");
	}

	private void saveGame(){
		saveGame(false);
	}
	
	private void saveGame(boolean onExit){
		if (onExit) {
			Toast.makeText(getApplicationContext(), "Saving the game...", 2).show();
		}
		ag.saveGame();
		
		if (onExit){
			Toast.makeText(getApplicationContext(), "Game saved.", Toast.LENGTH_SHORT).show();
		}
		
	}
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	
    //-  SET THROW FROM UI STATE ---------------------------------------------
	private void applyUIStateToActiveThrow(){
		applyUIStateToThrow(uiThrow);
		ag.updateActiveThrow(uiThrow);
    }
	
	private void applyUIStateToThrow(Throw t){
		log("applyUIStateToCurrentThrow() - Applying state to throw idx " + t.getThrowIdx());
    	applyUIThrowTypeToThrow(t);
    	applyUIThrowResultToThrow(t);
    	applyUISpecialMarksToThrow(t);
	}
    
	private void applyUIThrowTypeToThrow(Throw t){
		t.setThrowType(currentThrowType);
	}
	private void applyUIThrowResultToThrow(Throw t){
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
	private void applyUISpecialMarksToThrow(Throw t){
//		t.isError = isError();
		t.isGoaltend = isGoaltend();
//		t.isOwnGoal = isOwnGoal();
//		if (t.isError){
//			t.setErrorScore(getErrorScore());
//		}
//		if (t.isOwnGoal){
//			t.setOwnGoalScore(getOwnGoalScore());
//		}
//		if (t.isGoaltend){
//			t.setGoaltendScore(getGoaltendScore());
//		}
		
		
//		t.isShort = isShort();
//		t.isTrap=isTrap();
//		t.isBroken = isBroken();
		
//		t.isDrinkDropped = isDrinkDrop();
//		t.isDrinkHit = isDrinkHit();
//		
//		t.isOnFire=isOnFire();
//		t.isFiredOn=isFiredOn();
	}
	//-------------------------------------------------------------
	
	//APPLY THROW STATE TO UI STATE ===============================
	private void applyActiveThrowToUIState(){
		uiThrow = ag.getActiveThrow();
		applyThrowToUIState(uiThrow);
	}
	private void applyThrowToUIState(Throw t){
		setThrowType(t);
		setThrowResult(t);
		setSpecialMarks(t);
	}
	private void setSpecialMarks(Throw t){
//		setIsError(t.isError);
//		setIsOwnGoal(t.isOwnGoal);
//		setIsGoaltend(t.isGoaltend);
//		setErrorScore(t.getErrorScore());
//		setOwnGoalScore(t.getOwnGoalScore());
//		setGoaltendScore(t.getGoaltendScore());
		
//		setIsShort(t.isShort);
//		setIsTrap(t.isTrap);
//		setIsBroken(t.isBroken);
//		setIsDrinkHit(t.isDrinkHit);
//		setIsDrinkDropped(t.isDrinkDropped);
//		setIsOnFire(t.isOnFire);
//		setIsFiredOn(t.isFiredOn);
	}
	private void setThrowType(Throw t){
		currentThrowType = t.getThrowType();
		
		// wait until after click event?
		setThrowButtonState(ThrowType.BALL_HIGH, R.id.gip_button_high);
		setThrowButtonState(ThrowType.BALL_LOW, R.id.gip_button_low);
		setThrowButtonState(ThrowType.BALL_LEFT, R.id.gip_button_left);
		setThrowButtonState(ThrowType.BALL_RIGHT, R.id.gip_button_right);
		setThrowButtonState(ThrowType.STRIKE, R.id.gip_button_strike);
		setThrowButtonState(ThrowType.BOTTLE, R.id.gip_button_bottle);
		setThrowButtonState(ThrowType.POLE, R.id.gip_button_pole);
		setThrowButtonState(ThrowType.CUP, R.id.gip_button_cup);
		
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
	//===================================================================
	
	//{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
	//{{{{{{{{{{{{{{{{{{{{{{{{{Draw the scores{{{{{{{{{{{{{{{{{{{{{{{

	private void renderPage(int pidx){
		renderPage(pidx, true);
	}

	private void renderPage(int pidx, boolean setVpItem){
		ThrowTableFragment frag;
		while (pidx >= fragmentArray.size()) {
			frag = ThrowTableFragment.newInstance(pidx, getApplicationContext());
        	fragmentArray.add(frag);
		}
		if (setVpItem){
			vp.setCurrentItem(pidx);
		}
		logd("renderPage(): vp currentitem is " + vp.getCurrentItem() + " of " + vp.getChildCount() + " children");
		
		frag = fragmentArray.get(pidx);
		logd("renderPage() - got fragment");
		int[] range = ThrowTableFragment.throwIdxRange(pidx);
		logd("renderPage() - got throw range");
		frag.renderAsPage(pidx, ag.getThrows());
		log("renderPage() - rendered as page "+pidx);
		frag.clearHighlighted();
		logd("renderPage() - cleared highlighted");
		
		int idx = ag.getActiveIdx();
		if (idx >= range[0] && idx < range[1]){
			frag.highlightThrow(idx);
		}
	}
	
	//{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{

	public void log(String msg){
		Log.i(LOGTAG, msg);
	}
	public void logd(String msg){
		Log.d(LOGTAG, msg);
	}
	public void loge(String msg, Exception e){
		Log.e(LOGTAG, msg+": "+e.getMessage());
	}

	
	int getPageIdxMax() {
		return ag.nThrows() / (2*ThrowTableFragment.N_ROWS);
	}	
	int getPageIdx(int throwIdx) {
		if (throwIdx > ag.nThrows()) {
			throwIdx = ag.nThrows();
		}	
		int pidx = (throwIdx) / (2*ThrowTableFragment.N_ROWS);
		if (pidx < 0) {pidx = 0;}
		log("getPageIdx(int): Index is " + pidx + ".");
		return pidx;
	}
	int getPageIdx() {
		return getPageIdx(ag.nThrows());
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
	private void setThrowButtonState(int throwType, int id) {
		View btn = findViewById(id);
//		ImageButton btn = view;
		if (throwType == currentThrowType) {btn.setPressed(true);}
		else {btn.setPressed(false);}
	}
	
	
	
}
