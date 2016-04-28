package com.baydar.ktsa;

import java.io.Serializable;
import java.util.ArrayList;

public class Place implements Serializable, Comparable<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7775869498773199481L;
	private String id;
	// private int id;
	private int category_id;
	private int num_checkins;
	private double lon;
	private double lat;
	private Category category;
	private String name;
	private Location location;
	private double time_category_1;
	private double time_category_2;
	private double time_category_3;
	private double time_category_4;
	private double[] time_category;
	
	public double[] getTime_category() {
		return time_category;
	}

	public void setTime_category(double[] time_category) {
		this.time_category = time_category;
	}

	public ArrayList<Integer> checkin = new ArrayList<Integer>();

	public double getTime_category_1() {
		return time_category_1;
	}

	public void setTime_category_1(double time_category_1) {
		this.time_category_1 = time_category_1;
	}

	public double getTime_category_2() {
		return time_category_2;
	}

	public void setTime_category_2(double time_category_2) {
		this.time_category_2 = time_category_2;
	}

	public double getTime_category_3() {
		return time_category_3;
	}

	public void setTime_category_3(double time_category_3) {
		this.time_category_3 = time_category_3;
	}

	public double getTime_category_4() {
		return time_category_4;
	}

	public void setTime_category_4(double time_category_4) {
		this.time_category_4 = time_category_4;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public Place(int id, int category_id, double lat, double lon, String
	// name, int num_checkins, Location location) {
	public Place(String id, int category_id, double lat, double lon, String name, int num_checkins, Location location,
			double time_category_1, double time_category_2, double time_category_3, double time_category_4) {
		super();
		time_category = new double[4];
		this.id = id;
		this.category_id = category_id;
		this.lon = lon;
		this.lat = lat;
		this.name = name;
		this.location = location;
		this.num_checkins = num_checkins;
		this.time_category[0] = time_category_1;
		this.time_category[1] = time_category_2;
		this.time_category[2] = time_category_3;
		this.time_category[3] = time_category_4;

	}

	public String getId() {
		// public int getId(){
		return id;
	}

	public void setId(String id) {
		// public void setId(int id){
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

	public Place() {

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

	@Override
	public int compareTo(Object arg0) {
		int numCount = ((Place) arg0).getNum_checkins();
		return numCount - this.getNum_checkins();
	}

}
