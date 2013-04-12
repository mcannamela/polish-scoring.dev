package com.ultimatepolish.polishscorebook;

import android.widget.TextView;

public class ViewHolder_Player {
	public TextView firstName;
    public TextView lastName;
    public TextView nickName;
    public TextView id;
    
    public String getFirstName(){
    	return firstName.getText().toString();
    }
    public String getLastName(){
    	return lastName.getText().toString();
    }
    public String getNickName(){
    	return nickName.getText().toString();
    }
    public Long getId(){
    	return Long.valueOf(id.getText().toString());
    }
    	
    
}
