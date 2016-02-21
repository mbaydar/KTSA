package com.baydar.ktsa;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		HashMap<Integer, User> users = new HashMap<Integer, User>();
		HashMap<Integer, Place> places = new HashMap<Integer, Place>();
		HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
		ArrayList<Checkin> checkins = new ArrayList<Checkin>();
		Connection c = null;
		Statement stmt = null;
		long tStart = System.currentTimeMillis();
		// User load
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gowalla_u", "postgres", "02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users;");
			while (rs.next()) {
				int id = rs.getInt("user_id");
				int num_friends = rs.getInt("num_friends");
				int num_checkins = rs.getInt("num_checkins");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				users.put(id, new User(id, num_checkins, num_friends));
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
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gowalla_u", "postgres", "02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM categories;");
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
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
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gowalla_u", "postgres", "02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM places;");
			while (rs.next()) {
				int id = rs.getInt("id");
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
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gowalla_u", "postgres", "02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id < 1000000;");
			while (rs.next()) {
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
				int place_id = rs.getInt("place_id");
				int num_checkins = rs.getInt("num_checkins");
				Date timestamp = rs.getDate("timestamp");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Checkin checkin = new Checkin(id, user_id, place_id, num_checkins, timestamp);
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
		System.out.println("1000000");
		
		for(int i=1;i<7;i++){
			try {
				Class.forName("org.postgresql.Driver");
				c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gowalla_u", "postgres", "02741903");
				c.setAutoCommit(false);
				System.out.println("Opened database successfully");
	
				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id >" +  ((i*1000000)-1) + " and id < " + ((i+1*1000000)) +";");
				while (rs.next()) {
					int id = rs.getInt("id");
					int user_id = rs.getInt("user_id");
					int place_id = rs.getInt("place_id");
					int num_checkins = rs.getInt("num_checkins");
					Date timestamp = rs.getDate("timestamp");
					// System.out.println("ID = " + num_checkins);
					// System.out.println("user_id = " + id);
					//
					// System.out.println();
					Checkin checkin = new Checkin(id, user_id, place_id, num_checkins, timestamp);
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
			System.out.println(i*1000000);
		}
		
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gowalla_u", "postgres", "02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id > 7999999;");
			while (rs.next()) {
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
				int place_id = rs.getInt("place_id");
				int num_checkins = rs.getInt("num_checkins");
				Date timestamp = rs.getDate("timestamp");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Checkin checkin = new Checkin(id, user_id, place_id, num_checkins, timestamp);
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
		System.out.println("10000000");

		System.out.println(users.get(517337).getCheckins().get(0).getPlace().getCategory().getName());
		System.out.println("Operation done successfully");
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println(elapsedSeconds);
		
		
//		try {
//			FileOutputStream fout = new FileOutputStream("./users_u.dat");
//			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			for (User user : users.values()) {
//				oos.writeObject(user);
//			}
//			oos.close();
//			System.out.println("Done");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//		try {
//			FileOutputStream fout = new FileOutputStream("./checkins_u.dat");
//			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			for (Checkin checkin : checkins) {
//				oos.writeObject(checkin);
//			}
//			oos.close();
//			System.out.println("Done");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//		try {
//			FileOutputStream fout = new FileOutputStream("./places_u.dat");
//			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			for (Place place : places.values()) {
//				oos.writeObject(place);
//			}
//			oos.close();
//			System.out.println("Done");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//		try {
//			FileOutputStream fout = new FileOutputStream("./categories_u.dat");
//			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			for (Category category : categories.values()) {
//				oos.writeObject(category);
//			}
//			oos.close();
//			System.out.println("Done");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}

}
