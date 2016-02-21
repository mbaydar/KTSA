package com.baydar.ktsa;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class LoadPlainData {

	static HashMap<Integer, User> users = new HashMap<Integer, User>();
	static HashMap<Integer, Place> places = new HashMap<Integer, Place>();
	static HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
	static ArrayList<Checkin> checkins = new ArrayList<Checkin>();

	public static void main(String[] args) {

		long tStart = System.currentTimeMillis();
		// try {
		// FileInputStream fin = new FileInputStream("./categories.dat");
		// ObjectInputStream ois = new ObjectInputStream(fin);
		// for(int i=0;i<283;i++){
		// Category category = (Category)ois.readObject();
		// categories.put(category.getId(), category);
		// }
		// ois.close();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// System.out.println(((Category)categories.values().toArray()[100]).getName());
		// try {
		//
		// FileInputStream fin = new FileInputStream("./places.dat");
		// ObjectInputStream ois = new ObjectInputStream(fin);
		// for(int i=0;i<1542000;i++){
		// Place place = (Place)ois.readObject();
		// places.put(place.getId(), place);
		// }
		// ois.close();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// System.out.println("Place");
		// try {
		//
		// FileInputStream fin = new FileInputStream("./checkins.dat");
		// ObjectInputStream ois = new ObjectInputStream(fin);
		// for(int i=0;i<10097800;i++){
		// Checkin checkin = (Checkin)ois.readObject();
		// checkins.add(checkin);
		// }
		// ois.close();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// System.out.println("Checkin");
		try {

			FileInputStream fin = new FileInputStream("./users.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			for (int i = 0; i < 183709; i++) {
				User user = (User) ois.readObject();
				users.put(user.getId(), user);
			}
			ois.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(users.get(517337).getCheckins().get(0).getPlace().getCategory().getName());
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println(elapsedSeconds);
		
		while(true){
			dummyMethod();
		}
	}
	
	public static void dummyMethod(){
		System.out.println("Burdayiz");
		System.out.println(users.get(214750).getCheckins().get(0).getTimestamp().getTime());
		
	}

}
