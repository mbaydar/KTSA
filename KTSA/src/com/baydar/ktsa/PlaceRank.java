package com.baydar.ktsa;

public class PlaceRank implements Comparable<PlaceRank>{

	String id;
//	int id;
	double rankPoint;

	public PlaceRank(String id, double rankPoint) {
//	public PlaceRank(int id, double rankPoint) {
		this.id = id;
		this.rankPoint = rankPoint;
	}
	
	public int compareTo(PlaceRank o) {
		if (o.rankPoint == this.rankPoint) {
			return 0;
		} else if (o.rankPoint > this.rankPoint) {
			return 1;
		} else {
			return -1;
		}
	}


}
