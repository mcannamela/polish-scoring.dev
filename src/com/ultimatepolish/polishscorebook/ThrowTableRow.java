package com.ultimatepolish.polishscorebook;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TableRow;
import android.widget.TextView;

import com.ultimatepolish.scorebookdb.Throw;

public class ThrowTableRow extends TableRow {
	private Throw t;
	public static int tableTextSize = 20;
	public static int tableTextColor = Color.BLACK;
	public static int tableBackgroundColor = Color.WHITE;
	
	public ThrowTableRow(Context context) {
		super(context);
	}
	public ThrowTableRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ThrowTableRow(Throw t1, Throw t2, Context context) {
		super(context);
		this.t = t1;
		this.appendThrow(t1);
		this.appendThrow(t2);
		t2.setInitialScores(t1);
		int[] scores = t2.getFinalScores();
		this.appendScore(scores[1], scores[0]);
	}
	
	public ThrowTableRow(Throw t1,  Context context) {
		super(context);
		this.t = t1;
		this.appendThrow(t1);
		this.appendBlank();
		int[] scores = t1.getFinalScores();
		this.appendScore(scores[0], scores[1]);
	}
	public static ThrowTableRow buildBlankRow(Context context){
		ThrowTableRow tr = new ThrowTableRow(context);
		tr.appendBlank();
		tr.appendBlank();
		tr.appendBlank();
		return tr;
	}

	public static ThrowTableRow buildThrowRow(Throw t, Context context){
		ThrowTableRow tr = new ThrowTableRow(context);
		tr.t = t;
		TextView[] views = buildThrowViews(t, context);
		for (TextView tv: views){
			tr.addView(tv);
			}
		return tr;
	}
	public static TextView[] buildThrowViews(Throw t, Context context){
		TextView[] views = {new TextView(context), 
				new TextView(context)};

		views[0].setText(t.getThrowString());
		views[1].setText(t.getSpecialString());
		
		for (TextView tv: views){
			ThrowTableRow.formatTextView(tv);
		}
		return views;
	}
	public static TextView[] buildScoreViews(int p1Score, int p2Score, Context context){
		TextView[] views = {new TextView(context), 
				new TextView(context)
		};
		
		views[0].setText(String.valueOf(p1Score));
		views[1].setText(String.valueOf(p2Score));
		
		
		for (TextView tv: views){
			ThrowTableRow.formatTextView(tv);
			
		}
		return views;
	}
	
	public static void formatTextView(TextView v){
		v.setTextColor(tableTextColor);
		v.setTextSize(tableTextSize);
		v.setBackgroundColor(tableBackgroundColor);
		v.setGravity(Gravity.CENTER);
	}
	public void appendThrow(Throw t){
		TextView[] views = buildThrowViews(t, this.getContext());
		for (int i=0; i<views.length;i++){
			this.addView(views[i]);
		}
	}
	public void appendBlank(){
		TextView[] views = {new TextView(this.getContext()), 
							new TextView(this.getContext())};
		for (TextView tv: views){
			tv.setText(" ");
			ThrowTableRow.formatTextView(tv);
			this.addView(tv);
		}
	}
	
	public  void appendScore(int p1Score, int p2Score){
		TextView[] views = buildScoreViews(p1Score,p2Score, this.getContext());
		for (int i=0; i<views.length;i++){
			this.addView(views[i]);
		}
	}
	
	
	
	

	
}
