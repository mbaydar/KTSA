package com.baydar.ktsa;

import java.io.Serializable;

public class Category implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4061318708224520077L;
	private int id;
	private String name;
	private int num_checkins;
	
	public Category(int id, String name, int num_checkins) {
		super();
		this.id = id;
		this.name = name;
		this.num_checkins = num_checkins;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNum_checkins() {
		return num_checkins;
	}

	public void setNum_checkins(int num_checkins) {
		this.num_checkins = num_checkins;
	}

	public Category(){
		
	}

}
