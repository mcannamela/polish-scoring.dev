package com.ultimatepolish.polishscorebook;

import android.widget.TextView;

public class ViewHolder_Session {
	public TextView sessionId;
    public TextView sessionName;
    
    public String getSessionId(){
    	return sessionId.getText().toString();
    }
    public String getSessionName(){
    	return sessionName.getText().toString();
    }
    
}
