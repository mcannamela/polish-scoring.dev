package com.ultimatepolish.polishscorebook;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ultimatepolish.scorebookdb.Game;
import com.ultimatepolish.scorebookdb.Player;
import com.ultimatepolish.scorebookdb.Venue;

public class ViewAdapter_Venue extends ArrayAdapter<Venue> {
	private ArrayList<Venue> entries;
    private Activity activity;
 
    public ViewAdapter_Venue(Activity a, int resource, ArrayList<Venue> entries) {
        super(a, resource, entries);
        this.entries = entries;
        this.activity = a;
    }
     
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder_Venue holder;
        if (v == null) {
            LayoutInflater vi =
                (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_venue, null);
            holder = new ViewHolder_Venue();
            holder.venueId = (TextView) v.findViewById(R.id.textView_venueId);
            holder.venueName = (TextView) v.findViewById(R.id.textView_venueName);
            v.setTag(holder);
        }
        else
            holder=(ViewHolder_Venue) v.getTag();
 
        final Venue g = entries.get(position);
        if (g != null) {
    		holder.venueId.setText(String.valueOf(g.getId()));
            holder.venueName.setText(String.valueOf(g.getName())); 
        }
        return v;
    }
}
//firstPlayers.add(g.getPlayers(this)[0].getNickName());