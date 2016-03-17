package com.baydar.ktsa;

public class Paired implements Comparable<Paired> {

	String id;
//	int id;
	double distance;

	public Paired(String id, double distance) {
//	public Paired(int id, double distance) {
		this.id = id;
		this.distance = distance;
	}

	public int compareTo(Paired o) {
		if (o.distance > this.distance) {
			return -1;
		} else if (o.distance < this.distance) {
			return 1;
		} else {
			return 0;
		}
	}

}
