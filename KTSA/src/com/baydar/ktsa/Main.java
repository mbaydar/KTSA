package com.baydar.ktsa;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class Main {

	static HashMap<Integer, User> users = new HashMap<Integer, User>();
	// HashMap<Integer, Place> places = new HashMap<Integer, Place>();
	static HashMap<String, Place> places = new HashMap<String, Place>();
	static HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
	static ArrayList<Checkin> checkins = new ArrayList<Checkin>();
	static ArrayList<Checkin> predictedCheckins = new ArrayList<Checkin>();
	static ArrayList<Place> placesArray = new ArrayList<Place>();
	static Place mostPopularPlace;
	static Place[] mostPopularPlaces = new Place[50];

	public static void main(String[] args) {
		String database_name = "foursquare";
		loadData(database_name);
		// getMonthlyCheckinNumber(6);
		mostPopularPlace = getMostPopularPlace();
		setMostPopularPlaces();
		getPredictedCheckins();
		makePredictions();
	}

	private static void setMostPopularPlaces() {
		Collections.sort(placesArray);
		for (int i = 0; i < mostPopularPlaces.length; i++) {
			mostPopularPlaces[i] = placesArray.get(i);
			System.out.println(placesArray.get(i).getNum_checkins());
		}
	}

	public static void makePredictions() {
		int correctPredictions = 0;
		for (int i = 0; i < predictedCheckins.size(); i++) {
			for (int j = 0; j < mostPopularPlaces.length; j++) {
				if (makePrediction(predictedCheckins.get(i),mostPopularPlaces[j])) {
					correctPredictions++;
				}
			}
		}
		System.out.println(correctPredictions);
		System.out.println(predictedCheckins.size());
		System.out.println((double)correctPredictions/predictedCheckins.size());
	}

	public static boolean makePrediction(Checkin predictedCheckin) {
		if (predictedCheckin.getPlace().getId() == mostPopularPlace.getId()) {
			return true;
		} else
			return false;
	}
	
	public static boolean makePrediction(Checkin predictedCheckin, Place place) {
		if (predictedCheckin.getPlace().getId() == place.getId()) {
			return true;
		} else
			return false;
	}
	

	public static Place getMostPopularPlace() {
		Place mostPopular = null;
		int mostCount = 0;
		for (Place place : places.values()) {
			if (place.getNum_checkins() > mostCount) {
				mostCount = place.getNum_checkins();
				mostPopular = place;
			}
		}
		return mostPopular;
	}

	public static void getPredictedCheckins() {
		int startMonth = 6;
		Date startDate = new Date(111, startMonth, 20);
		Date endDate = new Date(111, startMonth + 1, 20);
		for (int i = 0; i < checkins.size(); i++) {
			if (checkins.get(i).getTimestamp().after(startDate) && checkins.get(i).getTimestamp().before(endDate)) {
				predictedCheckins.add(checkins.get(i));
			}
		}
	}

	public static void getMonthlyCheckinNumber(int startMonth) {
		Date startDate = new Date(110, startMonth, 20);
		Date endDate = new Date(111, startMonth + 1, 20);
		int checkinCount = 0;
		int userCount = 0;
		for (int i = 0; i < checkins.size(); i++) {
			if (checkins.get(i).getTimestamp().after(startDate) && checkins.get(i).getTimestamp().before(endDate)) {
				checkinCount++;
				predictedCheckins.add(checkins.get(i));
			}
		}
		// for (User user : users.values()) {
		// for (int i = 0; i < user.getCheckins().size(); i++) {
		// if (user.getCheckins().get(i).getTimestamp().after(startDate)
		// && user.getCheckins().get(i).getTimestamp().before(endDate)) {
		// userCount++;
		// break;
		// }
		// }
		// }
		System.out.println(checkinCount);
		// System.out.println(userCount);
		System.out.println(startDate.toString());
		System.out.println(endDate.toString());
	}

	public static void loadData(String database_name) {
		Connection c = null;
		Statement stmt = null;
		long tStart = System.currentTimeMillis();
		// User load
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
					"02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users;");
			while (rs.next()) {
				int id = rs.getInt("user_id");
				// int num_friends = rs.getInt("num_friends");
				// //gowalla_specific
				int num_checkins = rs.getInt("num_checkins");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				users.put(id, new User(id, num_checkins, 0));
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
					"02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM categories;");
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("category"); // for gowalla : name
				int num_checkins = rs.getInt("num_checkins");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				categories.put(id, new Category(id, name, num_checkins));
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
					"02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM place;");
			while (rs.next()) {
				// int id = rs.getInt("id");
				String id = rs.getString("id");
				String name = rs.getString("name");
				int category_id = rs.getInt("category_id");
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");
				int num_checkins = rs.getInt("num_checkins");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Place place = new Place(id, category_id, lat, lon, name, num_checkins);
				place.setCategory(categories.get(category_id));
				places.put(id, place);
				placesArray.add(place);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
					"02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM checkins;");
			while (rs.next()) {
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
				String place_id = rs.getString("place_id");
				// int num_checkins = rs.getInt("num_checkins");
				Date timestamp = rs.getDate("time");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Checkin checkin = new Checkin(id, user_id, place_id, 0, timestamp);
				checkin.setPlace(places.get(place_id));
				checkins.add(checkin);
				users.get(user_id).addCheckin(checkin);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		// System.out.println("1000000");

		// for (int i = 1; i < 7; i++) {
		// try {
		// Class.forName("org.postgresql.Driver");
		// c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" +
		// database_name, "postgres", "02741903");
		// c.setAutoCommit(false);
		// System.out.println("Opened database successfully");
		//
		// stmt = c.createStatement();
		// ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id >"
		// + ((i * 1000000) - 1)
		// + " and id < " + ((i + 1 * 1000000)) + ";");
		// while (rs.next()) {
		// int id = rs.getInt("id");
		// int user_id = rs.getInt("user_id");
		// int place_id = rs.getInt("place_id");
		// int num_checkins = rs.getInt("num_checkins");
		// Date timestamp = rs.getDate("timestamp");
		// // System.out.println("ID = " + num_checkins);
		// // System.out.println("user_id = " + id);
		// //
		// // System.out.println();
		// Checkin checkin = new Checkin(id, user_id, place_id, num_checkins,
		// timestamp);
		// checkin.setPlace(places.get(place_id));
		// checkins.add(checkin);
		// users.get(user_id).addCheckin(checkin);
		// }
		// rs.close();
		// stmt.close();
		// c.close();
		// } catch (Exception e) {
		// System.err.println(e.getClass().getName() + ": " + e.getMessage());
		// System.exit(0);
		// }
		// System.out.println(i * 1000000);
		// }
		//
		// try {
		// Class.forName("org.postgresql.Driver");
		// c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" +
		// database_name, "postgres", "02741903");
		// c.setAutoCommit(false);
		// System.out.println("Opened database successfully");
		//
		// stmt = c.createStatement();
		// ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id >
		// 7999999;");
		// while (rs.next()) {
		// int id = rs.getInt("id");
		// int user_id = rs.getInt("user_id");
		// int place_id = rs.getInt("place_id");
		// int num_checkins = rs.getInt("num_checkins");
		// Date timestamp = rs.getDate("timestamp");
		// // System.out.println("ID = " + num_checkins);
		// // System.out.println("user_id = " + id);
		// //
		// // System.out.println();
		// Checkin checkin = new Checkin(id, user_id, place_id, num_checkins,
		// timestamp);
		// checkin.setPlace(places.get(place_id));
		// checkins.add(checkin);
		// users.get(user_id).addCheckin(checkin);
		// }
		// rs.close();
		// stmt.close();
		// c.close();
		// } catch (Exception e) {
		// System.err.println(e.getClass().getName() + ": " + e.getMessage());
		// System.exit(0);
		// }
		// System.out.println("10000000");

		System.out.println(users.get(14462276).getCheckins().get(0).getPlace().getCategory().getName());
		System.out.println(checkins.size());
		System.out.println("Operation done successfully");
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println(elapsedSeconds);
	}

	public static void writeData(String database_name) {
		try {
			FileOutputStream fout = new FileOutputStream("./users_u" + database_name + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			for (User user : users.values()) {
				oos.writeObject(user);
			}
			oos.close();
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			FileOutputStream fout = new FileOutputStream("./checkins_u" + database_name + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			for (Checkin checkin : checkins) {
				oos.writeObject(checkin);
			}
			oos.close();
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			FileOutputStream fout = new FileOutputStream("./places_u" + database_name + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			for (Place place : places.values()) {
				oos.writeObject(place);
			}
			oos.close();
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			FileOutputStream fout = new FileOutputStream("./categories_u" + database_name + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			for (Category category : categories.values()) {
				oos.writeObject(category);
			}
			oos.close();
			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
