package com.ultimatepolish.polishscorebook;

import android.widget.TextView;

public class ViewHolder_Player {
	public TextView name;
    public TextView nickName;
    public TextView id;
    
    public String getName(){
    	return name.getText().toString();
    }
    public String getNickName(){
    	return nickName.getText().toString();
    }
    public Long getId(){
    	return Long.valueOf(id.getText().toString());
    }
    	
    
}
