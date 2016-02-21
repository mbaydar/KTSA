package com.baydar.ktsa;

import java.io.Serializable;

public class Friendship implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7938880745526964927L;
	private int user_id;
	private int friend_id;
	
	public Friendship(int user_id, int friend_id) {
		super();
		this.user_id = user_id;
		this.friend_id = friend_id;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getFriend_id() {
		return friend_id;
	}

	public void setFriend_id(int friend_id) {
		this.friend_id = friend_id;
	}

	public Friendship(){
		
	}
	
	

}
