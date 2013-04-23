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
	
	
	int page_idx = -1;
	
	static ThrowTableFragment newInstance(int position) {
		
		ThrowTableFragment f = new ThrowTableFragment();

        // Supply pager index as an argument.
        Bundle args = new Bundle();
        args.putInt(PAGE_IDX_KEY, position);
        f.setArguments(args);

        return f;
    }
	
	public static int throwNrToPageIdx(int throwNr){
		int global_ridx = throwNr/2;
		int pidx = global_ridx/N_ROWS;
		return pidx;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		page_idx = getArguments().getInt(PAGE_IDX_KEY);
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
		int ridx = throwNrToRowIdx(throwNr);
		return (ThrowTableRow) layout.getChildAt(ridx);
	}
	TableLayout getTableLayout(){
		return (TableLayout) getView();
	}
	
	int throwNrToRowIdx(int throwNr) throws ArrayIndexOutOfBoundsException{
		int pidx = throwNrToPageIdx(throwNr);
		if (pidx!=page_idx){
			throw new ArrayIndexOutOfBoundsException("throw dne on this page");
		}
		int ridx = (throwNr/2)%N_ROWS;
		return ridx;
	}
	
	int[] throwNrRange(){
		int[] range = new int[2];
		range[0] = (2*N_ROWS)*page_idx;
		range[1] = range[0]+2*N_ROWS;
		
		return range;
	}
	

}
