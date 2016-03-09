package com.baydar.ktsa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
	private ArrayList<Paired> placeDistances = new ArrayList<Paired>();

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

	public ArrayList<Paired> getVisitedPlaces() {
		double alfa = 1.1;
		ArrayList<Paired> visitedPlaces = new ArrayList<Paired>();
		for (int i = 0; i < this.checkins.size(); i++) {
			if (!visitedPlaces.contains(this.checkins.get(i).getPlace())) {
				visitedPlaces.add(new Paired(this.checkins.get(i).getPlace().getId(), alfa));
			} else {
				for (Paired pairs : visitedPlaces) {
//					if (pairs.id.equals(this.checkins.get(i).getPlace_id())) {
					if (pairs.id == this.checkins.get(i).getPlace().getId()) {
						pairs.distance = pairs.distance * alfa;
					}
				}
			}
		}
		return visitedPlaces;
	}

	public Integer[] getVisitedCategories() {
		Integer[] visitedCategories = new Integer[Main.categories.size()];
		for (int i = 0; i < visitedCategories.length; i++) {
			visitedCategories[i] = 0;
		}
		for (int i = 0; i < this.checkins.size(); i++) {
			visitedCategories[this.checkins.get(i).getPlace().getCategory_id() - 1]++;
		}
		return visitedCategories;
	}

	public Integer getMostVisitedCategory() {
		Integer[] visitedCategories = new Integer[Main.categories.size()];
		for (int i = 0; i < visitedCategories.length; i++) {
			visitedCategories[i] = 0;
		}
		for (int i = 0; i < this.checkins.size(); i++) {
			visitedCategories[this.checkins.get(i).getPlace().getCategory_id() - 1]++;
		}
		int count = 0;
		int index = 0;
		for (int i = 0; i < visitedCategories.length; i++) {
			if (visitedCategories[i] > count) {
				count = visitedCategories[i];
				index = i;
			}
		}
		return index;
	}

	public void calculatePlaceDistances(int num) {
		ArrayList<Paired> pairs = new ArrayList<Paired>();
		for (Place place : Main.places.values()) {
			pairs.add(new Paired(place.getId(), this.getHomeLocation().getDistance(place.getLocation())));
		}
		Collections.sort(pairs);
		for (int i = 0; i < num; i++) {
			placeDistances.add(new Paired(pairs.get(i).id, pairs.get(i).distance));
		}
	}
	
	public ArrayList<Paired> getPlaceDistances(){
		return placeDistances;
	}

	public void calculateHomeLocationByAvg() {
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

	public void calculateHomeLocationByFreq() {
//		HashMap<String, Integer> places = new HashMap<String, Integer>();
		HashMap<Integer, Integer> places = new HashMap<Integer, Integer>();
		for (int i = 0; i < checkins.size(); i++) {
			if (places.containsKey(checkins.get(i).getPlace().getId())) {
				int val = places.get(checkins.get(i).getPlace().getId());
				places.put(checkins.get(i).getPlace().getId(), val + 1);
			} else {
				places.put(checkins.get(i).getPlace().getId(), 1);
			}
		}
		ArrayList<Pair> pair = new ArrayList<Pair>();
//		for (String key : places.keySet()) {
		for (Integer key : places.keySet()) {
			pair.add(new Pair(key, places.get(key)));
		}
		
		if(pair.size()==0){
			System.out.println(this.getId());
		}
		Collections.sort(pair);
		this.setHomeLocation(
				new Location(Main.places.get(pair.get(0).id).getLat(), Main.places.get(pair.get(0).id).getLon()));
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

	class Pair implements Comparable<Pair> {
//		String id;
		int id;
		int count;

//		public Pair(String id, int count) {
		public Pair(int id, int count) {
			this.id = id;
			this.count = count;
		}

		public int compareTo(Pair o) {
			if (o.count == this.count) {
				return 0;
			} else if (o.count < this.count) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}
