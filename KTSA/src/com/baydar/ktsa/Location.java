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
		return Math.abs(this.lat - location.lat) + Math.abs(this.lon - location.lon);
	}

	public Location(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public String toString() {
		return this.lat + " " + this.lon;
	}
}
