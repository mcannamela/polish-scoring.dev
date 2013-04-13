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

public class ViewAdapter_Game extends ArrayAdapter<Game> {
	private ArrayList<Game> entries;
    private Activity activity;
 
    public ViewAdapter_Game(Activity a, int resource, ArrayList<Game> entries) {
        super(a, resource, entries);
        this.entries = entries;
        this.activity = a;
    }
     
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder_Game holder;
        if (v == null) {
            LayoutInflater vi =
                (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_game, null);
            holder = new ViewHolder_Game();
            holder.gameId = (TextView) v.findViewById(R.id.textView_gameId);
            holder.playerOne = (TextView) v.findViewById(R.id.textView_playerOne);
            holder.playerTwo = (TextView) v.findViewById(R.id.textView_playerTwo);
            holder.score = (TextView) v.findViewById(R.id.textView_score);
            v.setTag(holder);
        }
        else
            holder=(ViewHolder_Game) v.getTag();
 
        final Game g = entries.get(position);
        if (g != null) {
        	try{
        		Player[] players = g.getPlayers(v.getContext());
        		holder.gameId.setText(String.valueOf(g.getId()));
                holder.playerOne.setText(players[0].getNickName());
                holder.playerTwo.setText(players[1].getNickName());
                holder.score.setText(String.valueOf(g.getFirstPlayerScore()) + " / " + String.valueOf(g.getSecondPlayerScore()));
        	}
        	catch (SQLException e){
        		
        	}
            
        }
        return v;
    }
}
//firstPlayers.add(g.getPlayers(this)[0].getNickName());