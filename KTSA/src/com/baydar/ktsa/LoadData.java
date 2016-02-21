package com.baydar.ktsa;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class LoadData {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		HashMap<Integer, User> users = new HashMap<Integer, User>();
		HashMap<Integer, Place> places = new HashMap<Integer, Place>();
		HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
		ArrayList<Checkin> checkins = new ArrayList<Checkin>();
		try {

			FileInputStream fin = new FileInputStream("./categoriesHM.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			categories = (HashMap<Integer, Category>) ois.readObject();
			ois.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Category");
		try {

			FileInputStream fin = new FileInputStream("./placesHM.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			places = (HashMap<Integer, Place>) ois.readObject();
			ois.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Place");
		try {

			FileInputStream fin = new FileInputStream("./checkinsAL.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			checkins = (ArrayList<Checkin>) ois.readObject();
			ois.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Checkin");
		try {

			FileInputStream fin = new FileInputStream("./usersHM.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			users = (HashMap<Integer, User>) ois.readObject();
			ois.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(users.get(517337).getCheckins().get(0).getPlace().getCategory().getName());
	}

}
