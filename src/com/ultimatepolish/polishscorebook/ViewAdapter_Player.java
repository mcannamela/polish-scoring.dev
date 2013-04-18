package com.ultimatepolish.polishscorebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ultimatepolish.scorebookdb.Player;

public class ViewAdapter_Player extends ArrayAdapter<Player> {
	private ArrayList<Player> entries;
    private Activity activity;
 
    public ViewAdapter_Player(Activity a, int resource, int textViewResourceId, ArrayList<Player> entries) {
        super(a, resource, textViewResourceId, entries);
        this.entries = entries;
        this.activity = a;
    }
     
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder_Player holder;
        if (v == null) {
            LayoutInflater vi =
                (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_player, null);
            holder = new ViewHolder_Player();
            holder.firstName = (TextView) v.findViewById(R.id.textView_firstName);
            holder.lastName = (TextView) v.findViewById(R.id.textView_lastName);
            holder.nickName = (TextView) v.findViewById(R.id.textView_nickName);
            holder.id = (TextView) v.findViewById(R.id.textView_playerId);
            v.setTag(holder);
        }
        else
            holder=(ViewHolder_Player)v.getTag();
 
        final Player p = entries.get(position);
        if (p != null) {
            holder.firstName.setText(p.getFirstName());
            holder.lastName.setText(p.getLastName());
            holder.nickName.setText("\"" + p.getNickName() + "\"");           
            holder.id.setText( String.valueOf(p.getId()));
        }
        return v;
    }
}
