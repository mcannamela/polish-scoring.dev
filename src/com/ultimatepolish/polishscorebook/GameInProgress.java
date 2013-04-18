package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class GameInProgress extends MenuContainerActivity {
	public static int highlightedColor = Color.GRAY;
	public static int unhighlightedColor = ThrowTableRow.tableBackgroundColor;
	
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
	
//	TableLayout throwsTable;
	
	//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	//:::::::::::::::listeners:::::::::::::::::::::::::::::::::::::::::
	private OnCheckedChangeListener throwResultChangedListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
        	updateThrow(); 
        }
    };
    
    private OnValueChangeListener numberPickerChangeListener = new OnValueChangeListener() {
		public void onValueChange(NumberPicker parent, int oldVal, int newVal) {
			updateThrow();
		}
	};
    
    private OnClickListener throwClickedListener = new OnClickListener(){
    	public void onClick(View v){
    		int row, col, newThrowNr;
    		ViewGroup p = (ViewGroup) v.getParent();
    		ViewGroup gp = (ViewGroup) p.getParent();
    		   		
    		col = p.indexOfChild(v);
    		row = gp.indexOfChild(p);
    		
    		newThrowNr = tableRowColToThrowNr(row, col);
    		
//    		Toast.makeText(getApplicationContext(), 
//					"("+row+", "+col+", "+newThrowNr+")", 
//					Toast.LENGTH_SHORT).show();
    		
    		if (col>3){
    			return;
    		}
    		else{
    			changeCurrentThrow(newThrowNr);
    		}
    		
    	}
    };
    
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	
    
    //========================= initialization =======================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		throwsTable = (TableLayout) findViewById(R.id.tableLayout_throws);
		
		
		
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_game_in_progress);
		Intent intent = getIntent();
		Long gid = intent.getLongExtra("GID", -1); 
		Context context = getApplicationContext();
		
		TableLayout throwsTable = getThrowsTable();
		throwsTable.removeViews(0, throwsTable.getChildCount());
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
		displayMetadata();
		initNumPickers();
		initTableRows();
		initThrows();
		changeCurrentThrow(0);
		
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
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.addButton).setVisible(false);
		return true;
	}
	@Override
	protected void onResume(){
		super.onResume();
		initThrows();
	}
	@Override
	protected void onRestart(){
		super.onRestart();
		initThrows();
	}	
	@Override
    protected void onStop() {
    	super.onStop();
//    	finish();
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
		for (Throw t: tList){
			throwArray.add(t);
		}
		
		renderThrows();
		
		for (Throw t: throwArray){
			try{
				saveThrow(t);
			}
			catch (SQLException e){
				//TODO:put something useful here
			}
		}
		
	}
	private void initTableRows(){
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
	private void displayMetadata(){
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
    //=================================================================
	
	/////////////////////////////////////////////////////////
    /////////////// apply the state of the ui to a throw/////
    private void applyUIStateToCurrentThrow(Throw t){
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
	private void renderThrows(){
		renderThrows(0);
	}
	private void renderThrows(int fromThrowNumber){
		Throw t,pt;
		
		//add new views, skipping p1 throws unless it is the last throw
		for(int i=fromThrowNumber;i<throwArray.size();i++){
			t = getThrow(i);
			pt = getPreviousThrow(i);
			t.setInitialScores(pt);
			
//			if (i==fromThrowNumber){
//				renderThrow(t);
//			}
//			else{
//				renderScore(t);
//			}
			renderThrow(t);
			
		}		
	}
	
	private void renderThrow(Throw t){
		ThrowTableRow tr = getThrowTableRow(t);
		tr.updateText(t);
	}
	private void renderScore(Throw t){
		ThrowTableRow tr = getThrowTableRow(t);
		int[] score = t.getFinalScores();
		if (isP1Throw(t.getThrowNumber())){
			tr.updateScoreText(score[0], score[1]);
		}
		else{
			tr.updateScoreText(score[1], score[0]);
		}
	}
	private void updateCurrentScore(){
		int lastThrowNumber = throwArray.size()-1;
		Throw lastThrow = getThrow(lastThrowNumber);
		int[] scores = lastThrow.getFinalScores();
		if (isP1Throw(lastThrowNumber)){
			g.setFirstPlayerScore(scores[0]);
			g.setSecondPlayerScore(scores[1]);
		}
		else{
			g.setFirstPlayerScore(scores[1]);
			g.setSecondPlayerScore(scores[0]);
		}
		
	}
	
	private void setThrowHighlighted(int throwNr, boolean highlight) {
		
		ThrowTableRow tr = getThrowTableRow(getThrow(throwNr));
		
		TextView tv;
		int start, stop;
		if (isP1Throw()){
			start = 0;
			stop = 2;
		}
		else{
			start = 2;
			stop = 4;
		}
		for (int i=start;i<stop;i++){
			tv = (TextView) tr.getChildAt(i);
			if (highlight){
				tv.setBackgroundColor(highlightedColor);
			}
			else{
				tv.setBackgroundColor(unhighlightedColor);
			}
		}
	}
	
	//{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{

	
	public void confirmThrow(){
		changeCurrentThrow(throwNr+1);
	}
	public void confirmThrow(View view){
		changeCurrentThrow(throwNr+1);
		
//		try{
//			Dao<Throw, Long> d = Throw.getDao(getApplicationContext());
//			long  nThrows = d.countOf();
//			Toast.makeText(getApplicationContext(), 
//					"there are  "+nThrows+" throws in the db ", 
//					Toast.LENGTH_SHORT).show();
//		}
//		catch (SQLException e){
//			Toast.makeText(getApplicationContext(), 
//					e.getMessage(), 
//					Toast.LENGTH_SHORT).show();
//		}
	}
	
	void changeCurrentThrow(int newThrowNr){
		setThrowHighlighted(throwNr, false);
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
		
//		Toast.makeText(getApplicationContext(), 
//				"old: "+throwNr+", new: "+newThrowNr, 
//				Toast.LENGTH_SHORT).show();
		
		int oldThrowNr = throwNr;
		throwNr = newThrowNr;
		
		setScrollPosition();
		
		Throw t = getThrow(throwNr);
		Throw u = getPreviousThrow(throwNr);
		t.setInitialScores(u);
		applyCurrentThrowToUIState(t);
		try{
			saveThrow(getThrow(throwNr));
		}
		catch (SQLException e){
			Toast.makeText(getApplicationContext(), 
			"could not save throw "+throwNr+", "+e.getMessage(), 
			Toast.LENGTH_LONG).show();
		}
		
		renderThrows(oldThrowNr);
		
		setThrowHighlighted(newThrowNr, true);
		
		updateCurrentScore();
		saveGame();
		
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
	
	
	private void setScrollPosition() {
		// TODO Auto-generated method stub
	}
	
	public void checkboxClicked(View view){
		updateThrow();
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
		
//		switch (currentThrowType){
//		case ThrowType.STRIKE:
//		case ThrowType.BALL_HIGH:
//		case ThrowType.BALL_LEFT:
//		case ThrowType.BALL_RIGHT:
//		case ThrowType.BALL_LOW:
//			confirmThrow(findViewById(R.id.button_confirm));
//		}
		
		
	}
	
	private void updateThrow(){
		Throw t = getThrow(throwNr);
		applyUIStateToCurrentThrow(t);
		renderThrows();
	}
	
		
	boolean isP1Throw(){
		return isP1Throw(throwNr);
	}
	boolean isP1Throw(int throwNr){
		return (throwNr%2)==0;
	}
	int throwNumberToTableRow(){
		return throwNumberToTableRow(throwNr);
	}
	int throwNumberToTableRow(int throwNr){
		return throwNr/2;
	}
	int tableRowColToThrowNr(int row, int col){
		int throwNr = 2*row;
		if (col>=2){
			throwNr++;
		}
		return throwNr;
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
			return t;
		}
		else if (throwNr==-1){
			Throw t = g.makeNewThrow(throwNr);
			t.setThrowType(ThrowType.STRIKE);
			t.setThrowResult(ThrowResult.CATCH);
			return t;
		}
		else{
			throw new RuntimeException("tried to get a throw from the future");
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
	ThrowTableRow getThrowTableRow(Throw t){
		int throwNr = t.getThrowNumber();
		int rowIdx = throwNumberToTableRow(throwNr);
		TableLayout throwsTable = getThrowsTable();
		if (rowIdx<throwsTable.getChildCount()){
			return (ThrowTableRow) throwsTable.getChildAt(rowIdx);
		}
		else if (rowIdx==throwsTable.getChildCount()){
			ThrowTableRow tr = ThrowTableRow.buildBlankRow(getApplicationContext());
			for (int i=0;i<tr.getChildCount();i++){
				tr.getChildAt(i).setOnClickListener(throwClickedListener);
			}
			throwsTable.addView(tr);
			return tr;
		}
		else{
			throw new RuntimeException("tried to get a ThrowTableRow from the future");
		}
		
	}
	
	private TableLayout getThrowsTable(){
		return (TableLayout) findViewById(R.id.tableLayout_throws);
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
