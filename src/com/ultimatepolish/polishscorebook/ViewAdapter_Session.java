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

import com.ultimatepolish.scorebookdb.Session;

public class ViewAdapter_Session extends ArrayAdapter<Session> {
	private ArrayList<Session> entries;
    private Activity activity;
 
    public ViewAdapter_Session(Activity a, int resource, ArrayList<Session> entries) {
        super(a, resource, entries);
        this.entries = entries;
        this.activity = a;
    }
     
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder_Session holder;
        if (v == null) {
            LayoutInflater vi =
                (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_session, null);
            holder = new ViewHolder_Session();
            holder.sessionId = (TextView) v.findViewById(R.id.textView_sessionId);
            holder.sessionName = (TextView) v.findViewById(R.id.textView_sessionName);
            v.setTag(holder);
        }
        else
            holder=(ViewHolder_Session) v.getTag();
 
        final Session s = entries.get(position);
        if (s != null) {
    		holder.sessionId.setText(String.valueOf(s.getId()));
            holder.sessionName.setText(String.valueOf(s.getSessionName()));
        }
        return v;
    }
}
//firstPlayers.add(g.getPlayers(this)[0].getNickName());