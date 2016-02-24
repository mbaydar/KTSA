package com.baydar.ktsa;

public class Pair implements Comparable<Pair> {

	String id;
	double distance;

	public Pair(String id, double distance) {
		this.id = id;
		this.distance = distance;
	}

	@Override
	public int compareTo(Pair o) {
		if (o.distance == this.distance) {
			return 0;
		} else if (o.distance < this.distance) {
			return 1;
		} else {
			return -1;
		}
	}

}
