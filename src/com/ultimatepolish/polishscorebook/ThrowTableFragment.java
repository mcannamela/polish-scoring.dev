package com.ultimatepolish.polishscorebook;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

public class ThrowTableFragment extends Fragment {
	public static final String PAGE_IDX_KEY = "page_idx";
	public static final int N_ROWS = 20;
	
	
	static ThrowTableFragment newInstance(int position) {
		
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		TableLayout layout;
		layout = (TableLayout) inflater.inflate(R.layout.fragment_throws_table, container, false);
		
		ThrowTableRow tr = null;
		for (int i=0; i<N_ROWS;i++){
			tr = ThrowTableRow.buildBlankRow(container.getContext());
			layout.addView(tr);
		}
		return  layout;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public ThrowTableRow getTableRow(int throwNr){
		TableLayout layout = getTableLayout();
		int ridx = ThrowTableFragment.throwNrToRowIdx(throwNr);
		ThrowTableRow tr;
		try{
			tr = (ThrowTableRow) layout.getChildAt(ridx);
		}
		catch (NullPointerException e){
			throw new RuntimeException("Child for throw nr "+throwNr +" dne at row "+ridx);
		}

		return tr;
	}
	TableLayout getTableLayout(){
		return (TableLayout) getView();
	}
	
	
	

}
