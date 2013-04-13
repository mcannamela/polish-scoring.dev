package com.ultimatepolish.polishscorebook;

import android.widget.TextView;

public class ViewHolder_Venue {
	public TextView venueId;
    public TextView venueName;
    
    public String getVenueId(){
    	return venueId.getText().toString();
    }
    public String getVenueName(){
    	return venueName.getText().toString();
    }
    
}
