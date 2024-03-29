package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.j256.ormlite.dao.Dao;
import com.ultimatepolish.scorebookdb.ActiveGame;
import com.ultimatepolish.scorebookdb.DeadType;
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
	private View[] deadViews = new View[4];
	private View naView;
	
	Game g;
	ActiveGame ag;
	Throw uiThrow;
	Player[] p = new Player[2]; 
	Session s;
	Venue v;
	
	Dao<Game, Long> gDao;
	Dao<Throw, Long> tDao;
	Dao<Player, Long> pDao;
	Dao<Session, Long> sDao;
	Dao<Venue, Long> vDao;

//	int currentThrowType = ThrowType.NOT_THROWN;
//	int currentThrowResult = ThrowResult.CATCH;
	int currentDeadType = DeadType.ALIVE;
//	int[] currentFireCounts = {0, 0};
//	boolean currentIsTipped;
	
	// ownGoals: 0) linefault, 1) drink drop, 2) knocked pole, 3) knocked bottle, 4) bottle break
	boolean[] currentOwnGoals = {false, false, false, false, false};
	// defErrors: 0) goaltend, 1) grabbed, 2) drink hit, 3) drink drop, 4) knocked pole, 5) knocked bottle, 6) bottle break
	boolean[] currentDefErrors = {false, false, false, false, false, false, false};
	
	NumberPicker resultNp;
	
	
	// LISTENERS ==================================================
	private OnCheckedChangeListener checkboxChangedListener = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			updateActiveThrow();
		}
	};

    private OnValueChangeListener numberPickerChangeListener = new OnValueChangeListener() {
		public void onValueChange(NumberPicker parent, int oldVal, int newVal) {
			setThrowResult(newVal);
//			applyThrowResultToNumPicker(newVal);
			updateActiveThrow();
		}
	};

	private OnLongClickListener mLongClickListener = new OnLongClickListener() {
		@Override
        public boolean onLongClick(View view) {
			log("mLongClickListener(): " + view.getContentDescription() + " was long pressed");
			int buttonId = view.getId();
			boolean isDead = getDeadType()!=DeadType.ALIVE;
			
			if(isDead){
				setDeadType(DeadType.ALIVE);
			}
			else{
				switch (buttonId) {
					case R.id.gip_button_high:
						setDeadType(DeadType.HIGH);
						break;
					case R.id.gip_button_right:
						setDeadType(DeadType.RIGHT);
						break;
					case R.id.gip_button_low:
						setDeadType(DeadType.LOW);
						break;
					case R.id.gip_button_left:
						setDeadType(DeadType.LEFT);
						break;
					default:
						break;
				}
			}
			
			for (View vw: deadViews) {
				vw.setBackgroundColor(Color.LTGRAY);
			}
			
			if (getDeadType() > 0) {
				deadViews[getDeadType()-1].setBackgroundColor(Color.RED);
			}
			
			updateActiveThrow();
			throwTypeButtonPressed(view);
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
	
	public void throwTypeButtonPressed(View view){
		log("buttonPressed(): " + view.getContentDescription() + " was pressed");
		int buttonId = view.getId();
		
		boolean isNA = getThrowResult()==ThrowResult.NA;
			switch (buttonId) {
				case R.id.gip_button_high:
					uiThrow.setThrowType(ThrowType.BALL_HIGH);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_low:
					uiThrow.setThrowType(ThrowType.BALL_LOW);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_left:
					uiThrow.setThrowType(ThrowType.BALL_LEFT);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_right:
					uiThrow.setThrowType(ThrowType.BALL_RIGHT);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_trap:
					uiThrow.setThrowType(ThrowType.TRAP);
					setThrowResult(ThrowResult.NA);
					((ImageView) view).getDrawable().setLevel(2);
					break;
				case R.id.gip_button_short:
					uiThrow.setThrowType(ThrowType.SHORT);
					setThrowResult(ThrowResult.NA);
					break;
				case R.id.gip_button_strike:
					uiThrow.setThrowType(ThrowType.STRIKE);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_bottle:
					uiThrow.setThrowType(ThrowType.BOTTLE);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_pole:
					uiThrow.setThrowType(ThrowType.POLE);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_cup:
					uiThrow.setThrowType(ThrowType.CUP);
					if(isNA){
						setThrowResult(ThrowResult.CATCH);
					}
					break;
				case R.id.gip_button_fired_on:
					uiThrow.setThrowType(ThrowType.FIRED_ON);
					setThrowResult(ThrowResult.NA);
					break;
			}
			
			confirmThrow();
	}
	
	public void isTippedPressed(View view){
		uiThrow.isTipped = isTipped();
		renderPage(getPageIdx(ag.getActiveIdx()));
	}
	public void onFirePressed(View view){
		uiThrow.isOnFire = isOnFire();
		if (uiThrow.isOnFire){
			setThrowResult(ThrowResult.NA);
		}
		
		renderPage(getPageIdx(ag.getActiveIdx()));
	}
	
	public void errorButtonPressed(View view){
		log("errorButtonPressed(): " + view.getContentDescription() + " was pressed");
		int buttonId = view.getId();
		
		if (buttonId == R.id.gip_ownGoal) {
			OwnGoalDialog();
		} else if (buttonId == R.id.gip_playerError) {
			PlayerErrorDialog();
		}
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
    
    public void OwnGoalDialog() {   	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Own Goal")
			.setMultiChoiceItems(R.array.owngoals, currentOwnGoals, 
					new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if (isChecked) {
	                       // If the user checked the item, add it to the selected items
	                       currentOwnGoals[which] = true;
	                   } else { 
	                	   currentOwnGoals[which] = false;
	                   }
					updateActiveThrow();
				}
			})
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {

	               }
	           });
    	AlertDialog dialog = builder.create();
    	dialog.show();
    	
    }
    
    private void PlayerErrorDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Defensive Error")
			.setMultiChoiceItems(R.array.defErrors, currentDefErrors, 
					new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if (isChecked) {
	                       // If the user checked the item, add it to the selected items
	                       currentDefErrors[which] = true;
	                   } else { 
	                	   currentDefErrors[which] = false;
	                   }
					updateActiveThrow();
				}
			})
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   
	               }
	           });
    	AlertDialog dialog = builder.create();
    	dialog.show();
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
				pDao = Player.getDao(context);
				vDao = Venue.getDao(context);
				sDao = Session.getDao(context);
				
				
				g = gDao.queryForId(gId);
				pDao.refresh(g.getFirstPlayer());
				pDao.refresh(g.getSecondPlayer());
				sDao.refresh(g.getSession());
				vDao.refresh(g.getVenue());
				
				
				ag = new ActiveGame(g, context);
				uiThrow = ag.getActiveThrow();
				p[0] = g.getFirstPlayer();
				p[1] = g.getSecondPlayer();
				s = g.getSession();
				v = g.getVenue();
				
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
		
		deadViews[0] = findViewById(R.id.gip_dead_high);
		deadViews[1] = findViewById(R.id.gip_dead_right);
		deadViews[2] = findViewById(R.id.gip_dead_low);
		deadViews[3] = findViewById(R.id.gip_dead_left);
		
		naView = findViewById(R.id.gip_na_indicator);
	}
	private void initNumPickers(){
		// catch type numberpicker
		resultNp = (NumberPicker) findViewById(R.id.numPicker_catch);
		resultNp.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		String[] catchText = new String[5];
		catchText[0] = getString(R.string.gip_drop);
		catchText[1] = getString(R.string.gip_catch);
		catchText[2] = getString(R.string.gip_stalwart);
		catchText[3] = getString(R.string.gip_broken);
		catchText[4] = getString(R.string.gip_NA);
		resultNp.setMinValue(0);
		resultNp.setMaxValue(4);
		resultNp.setValue(1);
		resultNp.setDisplayedValues(catchText);
		resultNp.setOnValueChangedListener(numberPickerChangeListener); 
		
	}
	private void initListeners(){
		View view;
		view = findViewById(R.id.gip_button_high);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_left);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_right);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_low);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_trap);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_short);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_strike);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_pole);
		view.setOnLongClickListener(mLongClickListener);
		
		view = findViewById(R.id.gip_button_cup);
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
	private void updateActiveThrow(){
		log("updateThrow(): Updating throw at idx " + ag.getActiveIdx());
		setThrowFromUIState();
		renderPage(getPageIdx(ag.getActiveIdx()));
	}
	
	private void confirmThrow(){
		int activeIdx = ag.getActiveIdx();
		if ((activeIdx + 7) % 70 == 0) {
			Toast.makeText(getApplicationContext(), 
					"GTO in 3 innings", Toast.LENGTH_LONG).show();
		} else if ((activeIdx+1) % 70 == 0) {
			respectGentlemens();
		}
		gotoThrowIdx(activeIdx+1);
	}
	
	private void gotoThrowIdx(int newActiveIdx){
		log("gotoThrow() - Going from throw idx " + ag.getActiveIdx() + " to throw idx " + newActiveIdx + ".");
		
		//make sure the uiThrow has the latest state from the ui
		setThrowFromUIState();
		
		//switch the active throw in the underlying model and the ui
		ag.setActiveIdx(newActiveIdx);
		uiThrow = ag.getActiveThrow();
		
		//impose the state of the new uiThrow on the ui
		setUIState();
		
		//update the draw'rings
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
	private void setThrowFromUIState(){
		setThrowResultFromUI();
    	setSpecialMarksFromUI();
		ag.updateActiveThrow(uiThrow);
    }
	
//	private void setThrowFromUIState(Throw t){
//		log("setThrowFromUIState() - Applying state to throw idx " + t.getThrowIdx());
////    	applyUIThrowTypeToThrow(t);
//    	setThrowResultFromUI(t);
//    	setSpecialMarksFromUI(t);
//	}
//	private void applyUIThrowTypeToThrow(Throw t){
//		t.setThrowType(currentThrowType);
//	}
	private void setThrowResultFromUI(){
		int result = getThrowResultFromNumPicker();
		setThrowResult(result);
		log("throw result is " + ThrowResult.typeString[getThrowResult()]);
	}
	private void setSpecialMarksFromUI(){
			// ownGoals: 0) linefault, 1) drink drop, 2) knocked pole, 3) knocked bottle, 4) bottle break
			uiThrow.isLineFault = currentOwnGoals[0];
			uiThrow.isOffensiveDrinkDropped = currentOwnGoals[1];
			uiThrow.isOffensivePoleKnocked = currentOwnGoals[2];
			uiThrow.isOffensiveBottleKnocked = currentOwnGoals[3];
			uiThrow.isOffensiveBreakError = currentOwnGoals[4];
			
			// defErrors: 0) goaltend, 1) drink hit, 2) drink drop, 3) knocked pole, 4) knocked bottle, 5) bottle break
			uiThrow.isGoaltend = currentDefErrors[0];
			uiThrow.isGrabbed = currentDefErrors[1];
			uiThrow.isDrinkHit = currentDefErrors[2];
			uiThrow.isDefensiveDrinkDropped = currentDefErrors[3];
			uiThrow.isDefensivePoleKnocked = currentDefErrors[4];
			uiThrow.isDefensiveBottleKnocked = currentDefErrors[5];
			uiThrow.isDefensiveBreakError = currentDefErrors[6];
			
			setErrorButtonState();
	}
	//-------------------------------------------------------------
	
	//APPLY THROW STATE TO UI STATE ===============================
	private void setUIState(){
//		uiThrow = ag.getActiveThrow();
		setUIThrowResult();
		setUISpecialMarks();
	}
	private void setUIThrowResult() {
		applyThrowResultToNumPicker(uiThrow.getThrowResult());
//		currentThrowResult = t.getThrowResult();
	}
//	private void setUIThrowType(Throw t){
////		uiThrow.setThrowType(throwType) = t.getThrowType();
////		
////		// wait until after click event?
////		setThrowButtonState(ThrowType.BALL_HIGH, R.id.gip_button_high);
////		setThrowButtonState(ThrowType.BALL_LOW, R.id.gip_button_low);
////		setThrowButtonState(ThrowType.BALL_LEFT, R.id.gip_button_left);
////		setThrowButtonState(ThrowType.BALL_RIGHT, R.id.gip_button_right);
////		setThrowButtonState(ThrowType.TRAP, R.id.gip_button_trap);
////		setThrowButtonState(ThrowType.SHORT, R.id.gip_button_short);
////		setThrowButtonState(ThrowType.STRIKE, R.id.gip_button_strike);
////		setThrowButtonState(ThrowType.BOTTLE, R.id.gip_button_bottle);
////		setThrowButtonState(ThrowType.POLE, R.id.gip_button_pole);
////		setThrowButtonState(ThrowType.CUP, R.id.gip_button_cup);
//		if (t.isTipped) {
//			((ImageView) findViewById(R.id.gip_button_strike)).getDrawable().setLevel(3);
//		}
//		
//		setBrokenButtonState(t.getThrowType());
//	}
	private void setUISpecialMarks(){
		setTipped(uiThrow.isTipped);
		setOnFire(uiThrow.isOnFire);
//		currentFireCounts = uiThrow.getFireCounts();
		setDeadViews(uiThrow.getDeadType());
		
		// ownGoals: 0) linefault, 1) drink drop, 2) knocked pole, 3) knocked bottle, 4) bottle break
		currentOwnGoals[0] = uiThrow.isLineFault;
		currentOwnGoals[1] = uiThrow.isOffensiveDrinkDropped;
		currentOwnGoals[2] = uiThrow.isOffensivePoleKnocked;
		currentOwnGoals[3] = uiThrow.isOffensiveBottleKnocked;
		currentOwnGoals[4] = uiThrow.isOffensiveBreakError;
		
		// defErrors: 0) goaltend, 1) drink hit, 2) drink drop, 3) knocked pole, 4) knocked bottle, 5) bottle break
		currentDefErrors[0] = uiThrow.isGoaltend;
		currentDefErrors[1] = uiThrow.isGrabbed;
		currentDefErrors[2] = uiThrow.isDrinkHit;
		currentDefErrors[3] = uiThrow.isDefensiveDrinkDropped;
		currentDefErrors[4] = uiThrow.isDefensivePoleKnocked;
		currentDefErrors[5] = uiThrow.isDefensiveBottleKnocked;
		currentDefErrors[6] = uiThrow.isDefensiveBreakError;
		
		setErrorButtonState();
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

	private  void log(String msg){
		Log.i(LOGTAG, msg);
	}
	
	private  void logd(String msg){
		Log.d(LOGTAG, msg);
	}
	
	private  void loge(String msg, Exception e){
		Log.e(LOGTAG, msg+": "+e.getMessage());
	}

	protected int getThrowResultFromNumPicker() {
		int theResult = 0;
		switch (resultNp.getValue()) { 
		case 0:
			theResult = ThrowResult.DROP;
			break;
		case 1:
			theResult = ThrowResult.CATCH;
			break;
		case 2:
			theResult = ThrowResult.STALWART;
			break;
		case 3:
			theResult = ThrowResult.BROKEN;
			break;
		case 4:
			theResult = ThrowResult.NA;
			break;
		}
		return theResult;
	}
	
	protected void applyThrowResultToNumPicker(int result) {
		naView.setBackgroundColor(Color.LTGRAY);
		switch (result) { 
		case ThrowResult.DROP:
			resultNp.setValue(0);
			break;
		case ThrowResult.CATCH:
			resultNp.setValue(1);
			break;
		case ThrowResult.STALWART:
			resultNp.setValue(2);
			break;
		case ThrowResult.BROKEN:
			resultNp.setValue(3);
			break;
		case ThrowResult.NA:
			resultNp.setValue(4);
//			naView.setBackgroundColor(Color.RED);
			break;
		}
	}
	
	protected boolean isTipped(){
		ToggleButton tb = (ToggleButton) findViewById(R.id.gip_toggle_button_is_tipped);
		return tb.isChecked();
	}
	protected boolean isOnFire(){
		ToggleButton tb = (ToggleButton) findViewById(R.id.gip_toggle_button_on_fire);
		return tb.isChecked();
	}
	protected void setTipped(boolean b){
		ToggleButton tb = (ToggleButton) findViewById(R.id.gip_toggle_button_is_tipped);
		tb.setChecked(b);
	}
	protected void setOnFire(boolean b){
		ToggleButton tb = (ToggleButton) findViewById(R.id.gip_toggle_button_on_fire);
		tb.setChecked(b);
	}
	
	protected int getThrowType(){
		return uiThrow.getThrowType();
	}
	protected void setThrowType(int ThrowType){
		uiThrow.setThrowType(ThrowType);
	}
	
	protected int getDeadType(){
		return uiThrow.getDeadType();
	}
	protected void setDeadType(int deadType){
		uiThrow.setDeadType(deadType);
	}
	protected void setDeadViews(int deadType){
		for (View vw: deadViews) {
			vw.setBackgroundColor(Color.LTGRAY);
		}
		if (deadType > 0) {
			deadViews[deadType-1].setBackgroundColor(Color.RED);
		}
	}
	
	protected int getThrowResult(){
		return uiThrow.getThrowResult();
	}
	protected void setThrowResult(int throwResult){
		setThrowResult(throwResult);
		applyThrowResultToNumPicker(throwResult);
	}
	
	
	protected int getPageIdxMax() {
		return ag.nThrows() / (2*ThrowTableFragment.N_ROWS);
	}	
	
	protected int getPageIdx(int throwIdx) {
		if (throwIdx > ag.nThrows()) {
			throwIdx = ag.nThrows();
		}	
		int pidx = (throwIdx) / (2*ThrowTableFragment.N_ROWS);
		if (pidx < 0) {pidx = 0;}
		log("getPageIdx(int): Index is " + pidx + ".");
		return pidx;
	}
	
	protected int getPageIdx() {
		return getPageIdx(ag.nThrows());
	}
	
//	protected void setThrowButtonState(int throwType, int id) {
//		ImageView btn = (ImageView) findViewById(id);
//		
//		if (throwType == currentThrowType) {
//			btn.getDrawable().setLevel(1);
//		} else if (throwType == ThrowType.TRAP && currentThrowType == ThrowType.TRAP_REDEEMED) {
//			btn.getDrawable().setLevel(1);
//		} else {
//			btn.getDrawable().setLevel(0);
//		}
//	}
	
	protected void setBrokenButtonState(int throwType) {
		Drawable poleDwl = ((ImageView) findViewById(R.id.gip_button_pole)).getDrawable();
		Drawable cupDwl = ((ImageView) findViewById(R.id.gip_button_cup)).getDrawable();
		Drawable bottleDwl = ((ImageView) findViewById(R.id.gip_button_bottle)).getDrawable();
		
		if (uiThrow.getThrowResult() == ThrowResult.BROKEN) {
			switch (throwType) {
			case ThrowType.POLE:
				poleDwl.setLevel(3);
				cupDwl.setLevel(2);
				bottleDwl.setLevel(2);
				break;
			case ThrowType.CUP:
				poleDwl.setLevel(2);
				cupDwl.setLevel(3);
				bottleDwl.setLevel(2);
				break;
			case ThrowType.BOTTLE:
				poleDwl.setLevel(2);
				cupDwl.setLevel(2);
				bottleDwl.setLevel(3);
				break;
			}
		} else {
			switch (throwType) {
			case ThrowType.POLE:
				poleDwl.setLevel(1);
				cupDwl.setLevel(0);
				bottleDwl.setLevel(0);
				break;
			case ThrowType.CUP:
				poleDwl.setLevel(0);
				cupDwl.setLevel(1);
				bottleDwl.setLevel(0);
				break;
			case ThrowType.BOTTLE:
				poleDwl.setLevel(0);
				cupDwl.setLevel(0);
				bottleDwl.setLevel(1);
				break;
			}
		}
		
		
	}

	protected void setErrorButtonState() {
		TextView ogVw = (TextView) findViewById(R.id.gip_ownGoal);
		TextView deVw = (TextView) findViewById(R.id.gip_playerError);
		ogVw.setTextColor(Color.BLACK);
		deVw.setTextColor(Color.BLACK);
		for (boolean og: currentOwnGoals) {
			if (og) {ogVw.setTextColor(Color.RED);}
		}
		for (boolean de: currentDefErrors) {
			if (de) {deVw.setTextColor(Color.RED);}
		}
	}
}
