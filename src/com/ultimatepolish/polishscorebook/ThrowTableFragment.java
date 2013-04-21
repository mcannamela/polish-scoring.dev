package com.ultimatepolish.polishscorebook;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

public class ThrowTableFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		TableLayout layout;
//		layout = new TableLayout(getActivity().getApplicationContext());
		 layout = (TableLayout) inflater.inflate(R.layout.fragment_throws_table, container, false);
		return  layout;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	

}
