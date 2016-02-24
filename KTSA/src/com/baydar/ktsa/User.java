package com.baydar.ktsa;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3806172629221184023L;
	private int id;
	private int num_checkins;
	private int num_friends;
	private boolean is_active;
	private Location homeLocation;
	private ArrayList<Checkin> checkins = new ArrayList<Checkin>();

	public ArrayList<Checkin> getCheckins() {
		return checkins;
	}

	public void setCheckins(ArrayList<Checkin> checkins) {
		this.checkins = checkins;
	}

	public void addCheckin(Checkin checkin) {
		this.checkins.add(checkin);
	}

	public ArrayList<User> getFriends() {
		return null;
	}

	public boolean isVisitedPlace(Place place) {
		for (int i = 0; i < this.checkins.size(); i++) {
			if (this.checkins.get(i).getPlace() == place) {
				return true;
			}
		}
		return false;
	}

	public boolean isVisitedCategory(Category category) {
		for (int i = 0; i < this.checkins.size(); i++) {
			if (this.checkins.get(i).getPlace().getCategory() == category) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<Place> getVisitedPlaces() {
		ArrayList<Place> visitedPlaces = new ArrayList<Place>();
		for (int i = 0; i < this.checkins.size(); i++) {
			if (!visitedPlaces.contains(this.checkins.get(i).getPlace())) {
				visitedPlaces.add(this.checkins.get(i).getPlace());
			}
		}
		return visitedPlaces;
	}

	public ArrayList<Category> getVisitedCategories() {
		ArrayList<Category> visitedCategories = new ArrayList<Category>();
		for (int i = 0; i < this.checkins.size(); i++) {
			if (!visitedCategories.contains(this.checkins.get(i).getPlace().getCategory())) {
				visitedCategories.add(this.checkins.get(i).getPlace().getCategory());
			}
		}
		return visitedCategories;
	}

	public void calculateHomeLocation() {
		double lat = 0;
		double lon = 0;
		for (int i = 0; i < checkins.size(); i++) {
			lat += checkins.get(i).getPlace().getLat();
			lon += checkins.get(i).getPlace().getLon();
		}
		lat = lat / checkins.size();
		lon = lon / checkins.size();
		this.setHomeLocation(new Location(lat, lon));

	}

	public ArrayList<Location> getVisitedLocations() {
		ArrayList<Location> visitedLocations = new ArrayList<Location>();
		for (int i = 0; i < this.checkins.size(); i++) {
			if (!visitedLocations.contains(this.checkins.get(i).getPlace().getLocation())) {
				visitedLocations.add(this.checkins.get(i).getPlace().getLocation());
			}
		}
		return visitedLocations;
	}

	public User(int id, int num_checkins, int num_friends) {
		super();
		this.id = id;
		this.num_checkins = num_checkins;
		this.num_friends = num_friends;
		this.is_active = true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNum_checkins() {
		return num_checkins;
	}

	public void setNum_checkins(int num_checkins) {
		this.num_checkins = num_checkins;
	}

	public int getNum_friends() {
		return num_friends;
	}

	public void setNum_friends(int num_friends) {
		this.num_friends = num_friends;
	}

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public User() {

	}

	public Location getHomeLocation() {
		return homeLocation;
	}

	public void setHomeLocation(Location homeLocation) {
		this.homeLocation = homeLocation;
	}

}
