package com.baydar.ktsa;

import java.io.Serializable;

public class Location implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1015867081024377140L;
	double lon;
	double lat;

	public Location() {

	}

	public double getDistance(Location location) {
		return distance(this.lat, this.lon, location.lat, location.lon);
//		return Math.abs(this.lat - location.lat) + Math.abs(this.lon - location.lon);
	}

	public Location(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public String toString() {
		return this.lat + " " + this.lon;
	}

	private static double distance(double lat1, double lon1, double lat2, double lon2) {
		if(lat1==lat2 && lon1==lon2){
			return 0;
		}
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return (dist);
	}

	/*
	 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	 */
	/* :: This function converts decimal degrees to radians : */
	/*
	 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*
	 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	 */
	/* :: This function converts radians to decimal degrees : */
	/*
	 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	 */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

}
