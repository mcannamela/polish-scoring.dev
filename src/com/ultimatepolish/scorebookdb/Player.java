package com.ultimatepolish.scorebookdb;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Player{
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String NICK_NAME = "nickName";
	
	
	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(columnName=FIRST_NAME, canBeNull=false, uniqueCombo=true)
	private String firstName;
	
	@DatabaseField(columnName=LAST_NAME,canBeNull=false, uniqueCombo=true)
	private String lastName;

	@DatabaseField(columnName=NICK_NAME,canBeNull=false, uniqueCombo=true)
	private String nickName;
	
	@DatabaseField
	public boolean throwsRightHanded;
	
	@DatabaseField
	public boolean throwsLeftHanded;
	
	@DatabaseField
	private int height_cm;
	
	@DatabaseField
	private int weight_kg;

	@DatabaseField
	private int  nGames;
	
	@DatabaseField
	private int  nWins;
	
	@DatabaseField
	private int  nLosses;
	
	Player(){}

	public Player(  String firstName, 
					String lastName, 
					String nickName,
					boolean throwsRightHanded, 
					boolean throwsLeftHanded, 
					int height_cm,
					int weight_kg) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.nickName = nickName;
		this.throwsRightHanded = throwsRightHanded;
		this.throwsLeftHanded = throwsLeftHanded;
		this.height_cm = height_cm;
		this.weight_kg = weight_kg;
	}
	
	public static Dao<Player, Long> getDao(Context context) throws SQLException{
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Player, Long> d = helper.getPlayerDao();
		return d;
	}
	public static long getIdByNames(String first, String last, String nick, Context context) throws SQLException{
		Player p = getByNames(first, last, nick, context);
		if (p==null){
			return -1;
		}
		return p.getId();
	}
	
	public static Player getByNames(String first, String last, String nick, Context context) throws SQLException{
		List<Player> pList = null;
		HashMap<String,Object> m = buildNameMap(first, last, nick);
		
		pList = getDao(context).queryForFieldValuesArgs(m);
		if (pList.isEmpty()){
			return null;
		}
		else{
			return pList.get(0);
		}
	}
	
	public static boolean exists(String first, String last, String nick, Context context) throws SQLException{
		if (first==null || last==null || nick==null){
			return false;
		}
		List<Player> pList = null;
		HashMap<String,Object> m = buildNameMap(first, last, nick);
		
		pList = getDao(context).queryForFieldValuesArgs(m);
		if (pList.isEmpty()){
			return false;
		}
		else{
			return true;
		}
	}
	
	public boolean exists(Context context) throws SQLException{
		return exists(firstName, lastName, nickName, context);
	}
		
	public static ArrayList<Player> getAll(Context context) throws SQLException{
		Dao<Player, Long> d = Player.getDao(context);
		ArrayList<Player> players = new ArrayList<Player>();
		for(Player p:d){
			players.add(p);
		}
		return players;
	}
	public static HashMap<String,Object> buildNameMap(String first, String last, String nick){
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put(FIRST_NAME, first.toLowerCase(Locale.US));
		m.put(LAST_NAME, last.toLowerCase(Locale.US));
		m.put(NICK_NAME, nick.toLowerCase(Locale.US));
		return m;
	}
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayName(){
		return firstName+" \""+nickName+"\" "+lastName;
	}
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	

	public int getHeight_cm() {
		return height_cm;
	}

	public void setHeight_cm(int height_cm) {
		this.height_cm = height_cm;
	}

	public int getWeight_kg() {
		return weight_kg;
	}

	public void setWeight_kg(int weight_kg) {
		this.weight_kg = weight_kg;
	}


	public int getnGames() {
		return nGames;
	}

	public void setnGames(int nGames) {
		this.nGames = nGames;
	}

	public int getnWins() {
		return nWins;
	}

	public void setnWins(int nWins) {
		this.nWins = nWins;
	}

	public int getnLosses() {
		return nLosses;
	}

	public void setnLosses(int nLosses) {
		this.nLosses = nLosses;
	}
	
	
}
