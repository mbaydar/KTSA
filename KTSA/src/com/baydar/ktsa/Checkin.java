package com.baydar.ktsa;

import java.io.Serializable;
import java.util.Date;

public class Checkin implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5235355871062461987L;
	private int id;
	private int user_id;
	private int place_id;
	private int num_checkins;
	private Date timestamp;
	private Place place;
	
	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public Checkin(){
		
	}

	public Checkin(int id, int user_id, int place_id, int num_checkins, Date timestamp) {
		super();
		this.id = id;
		this.user_id = user_id;
		this.place_id = place_id;
		this.num_checkins = num_checkins;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getPlace_id() {
		return place_id;
	}

	public void setPlace_id(int place_id) {
		this.place_id = place_id;
	}

	public int getNum_checkins() {
		return num_checkins;
	}

	public void setNum_checkins(int num_checkins) {
		this.num_checkins = num_checkins;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	

}
