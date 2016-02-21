package com.baydar.ktsa;

import java.io.Serializable;

public class Place implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7775869498773199481L;
	private int id;
	private int category_id;
	private int num_checkins;
	private double lon;
	private double lat;
	private Category category;
	private String name;
	private Location location;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Place(int id, int category_id, double lon, double lat, String name, int num_checkins) {
		super();
		this.id = id;
		this.category_id = category_id;
		this.lon = lon;
		this.lat = lat;
		this.name = name;
		location = new Location();
		this.location.lat = lat;
		this.location.lon = lon;
		this.num_checkins = num_checkins;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCategory_id() {
		return category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public int getNum_checkins() {
		return num_checkins;
	}

	public void setNum_checkins(int num_checkins) {
		this.num_checkins = num_checkins;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public Place(){
		
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	class Location implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6323526392577371814L;
		double lon;
		double lat;
	}
	
	

}
