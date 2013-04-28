package com.ultimatepolish.polishscorebook;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ultimatepolish.scorebookdb.Throw;

public class ThrowTableFragment extends Fragment {
	public static final String LOG_PREFIX = "TTFrag.";
	public static final String PAGE_IDX_KEY = "page_idx";
	public static final int N_ROWS = 20;
	public static int highlightedColor = Color.GRAY;
	public static int unhighlightedColor = ThrowTableRow.tableBackgroundColor;
	
	
	OnTableRowClickedListener mListener;
	
	static ThrowTableFragment newInstance() {	
		ThrowTableFragment f = new ThrowTableFragment();
        return f;
    }
	
	public static int throwNrToPageIdx(int throwNr){
		assert throwNr>=0;
		int global_ridx = throwNr/2;
		int pidx = global_ridx/N_ROWS;
		return pidx;
	}
	public static int throwNrToRowIdx(int throwNr) throws ArrayIndexOutOfBoundsException{
		return(throwNr/2)%N_ROWS;
	}
	public static int[] throwNrRange(int page_idx){
		int[] range = new int[2];
		range[0] = (2*N_ROWS)*page_idx;
		range[1] = range[0]+2*N_ROWS;
		return range;
	}
	public static int localThrowNrToGlobal(int page_idx, int local_throw_nr){
		return 2*N_ROWS*page_idx+local_throw_nr;
	}
	public void log(String msg){
		Log.i(GameInProgress.LOGTAG, LOG_PREFIX+msg);
	}
		
	
	public interface OnTableRowClickedListener {
		public void onThrowClicked(int local_throw_nr);
	}
	private OnClickListener throwClickedListener = new OnClickListener(){
    	public void onClick(View v){
    		int row, col, local_throw_nr;
    		ViewGroup p = (ViewGroup) v.getParent();
    		ViewGroup gp = (ViewGroup) p.getParent();
    		   		
    		col = p.indexOfChild(v);
    		row = gp.indexOfChild(p);
    		
    		  		
    		if (col>3){
    			return;
    		}
    		else{
    			local_throw_nr = 2*row;
    			if (col>=2){
    				local_throw_nr++;
    			}
    			mListener.onThrowClicked(local_throw_nr);
    		}
    		
    	}
    };
	
	
	@Override
	public void onAttach(Activity activity) {
		log("onAttach - attaching activity");
		super.onAttach(activity);
		try {
            mListener = (OnTableRowClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTableRowClickedListener");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("onCreate - creating fragment");
		super.onCreate(savedInstanceState);
		
	}
	
	
	


	@Override
	public void onResume() {
		log("onResume - resuming fragment");
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("onCreate - creating view");
		TableLayout layout;
		layout = (TableLayout) inflater.inflate(R.layout.fragment_throws_table, container, false);
		
		ThrowTableRow tr = null;
		for (int i=0; i<N_ROWS;i++){
			tr = ThrowTableRow.buildBlankRow(container.getContext());
			for (int j=0;j<tr.getChildCount();j++){
				tr.getChildAt(j).setOnClickListener(throwClickedListener);	
			}
			
			layout.addView(tr);
		}
		return  layout;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public void renderAsPage(int page_idx, ArrayList<Throw> throwArray){
		Throw t;
		int nThrows = throwArray.size();
		int[] range = ThrowTableFragment.throwNrRange(page_idx);
		
//		Toast.makeText(getApplicationContext(), 
//				"range for page "+pidx+" is "+range[0] +" to "+range[1], 
//				Toast.LENGTH_SHORT).show();
		
		if (nThrows<range[0]){
			return;
		}
		
		for (int i=range[0]; i<range[1];i++){
			if (i>=nThrows){
				break;
			}
			t = throwArray.get(i);
			renderThrow(t);
		}
	}
	private void renderThrow(Throw t){
		try{
			ThrowTableRow tr = getTableRow(t.getThrowNumber());
			tr.updateText(t);
		}
		catch (IndexOutOfBoundsException e){
			Toast.makeText(getActivity().getApplicationContext(), 
			"throw "+t.getThrowNumber()+" has no view on this page", 
			Toast.LENGTH_SHORT).show();
			return;
		}
		
	}
	
	public void highlightThrow(int throwNr){
		setThrowHighlighted(throwNr, true);
	}
	public void clearHighlighted(){
		for (int i = 0;i<2*N_ROWS; i++){
			setThrowHighlighted(i, false);
		}
	}
	
	private void setThrowHighlighted(int throwNr, boolean highlight) {
		if (throwNr<0){
			return;
		}
		ThrowTableRow tr;
		try{
			tr = getTableRow(throwNr);
		}
		catch (IndexOutOfBoundsException e){
			return;
		}
		
		
		TextView tv;
		int start, stop;
		if (Throw.isP1Throw(throwNr)){
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
	public ThrowTableRow getTableRow(Throw t){
		return getTableRow(t.getThrowNumber());
	}
	
	public ThrowTableRow getTableRow(int throwNr){
		TableLayout layout = getTableLayout();
		int ridx = ThrowTableFragment.throwNrToRowIdx(throwNr);
//		Log.i("GIP", "Fragment/getTableRow() - getting row for throw nr " +throwNr+", it's "+ ridx);
		ThrowTableRow tr;
		try{
			tr = (ThrowTableRow) layout.getChildAt(ridx);
		}
		catch (NullPointerException e){
			throw new IndexOutOfBoundsException("Child for throw nr "+throwNr +" dne at row "+ridx);
		}

		return tr;
	}
	TableLayout getTableLayout(){
		return (TableLayout) getView();
	}

	
	
	
}
