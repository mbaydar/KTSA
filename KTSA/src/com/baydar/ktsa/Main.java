package com.baydar.ktsa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public class Main {

	public static final DateTimeZone jodaTzUTC = DateTimeZone.forID("EET");
	static HashMap<Integer, User> users = new HashMap<Integer, User>();
	// static HashMap<Integer, Place> places = new HashMap<Integer, Place>(); //
	// gowalla
	static HashMap<String, Place> places = new HashMap<String, Place>(); //
	// foursquare
	static HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
	static ArrayList<Checkin> checkins = new ArrayList<Checkin>();
	static HashMap<Integer, Checkin> checkinhm = new HashMap<Integer, Checkin>();
	static ArrayList<Checkin> predictedCheckins = new ArrayList<Checkin>();
	static ArrayList<Place> placesArray = new ArrayList<Place>();
	static Place[] mostPopularPlaces = new Place[100];
	static Place[] mostPopularNPlaces;
	static Place mostPopularPlace;
	static String database_name = "foursquare";
	static String city = "Austin";
	static int year = 110;

	static double wdistance = 0;
	static double wvisitedP = 1;
	static double wvisitedC = 0;
	static double wpopular = 0;
	static double wtime = 0;

	static int closePlaceNumber = 0;

	static double avgDistance = 0;

	public static void main(String[] args) {

		loadData(database_name);
		Collections.sort(placesArray); // Sort by checkin nums
		setMostPopularPlaces();
		calculateUsersHomeLocations();
//		calculateUsersPlaceDistances(1000);
		setActiveUsers();

		// printCategories();

		testAll();

		// findAvgFriendNum();
		// findAvgDistance();
		// findoutliners();
		// System.out.println((double) closePlaceNumber / 3000);
		// calculateSigma();
		// calculateWeights();
		// getAvgUniquePlaceVisited();
		// getAvgCheckinNumByUniquePlaceVisited();
		// getMaxCheckinNumByUniquePlaceVisited();
		// getMonthlyCheckinNumber(6);
		// getActiveUserNumByCity();

		// calculatePlaceTimes(); // for once do it for gowalla, too
		// calculateAverageDistances();
		// calculateMaxAvgDistances();
	}

	public static void printCategories() {
		for (Place place : placesArray) {
			if (place.getCategory_id() == 0) {
				System.out.println(place.getId());
			}
		}
	}

	public static void findAvgFriendNum() {
		double friendNum = 0;
		int activeUsers = 0;
		for (User user : users.values()) {
			if (user.isIs_active()) {
				friendNum += user.getNum_friends();
				activeUsers++;
			}
		}
		System.out.println(friendNum / activeUsers);
	}

	public static void findAvgDistance() {
		getPredictedCheckins(5);
		for (int i = 0; i < predictedCheckins.size(); i++) {
			User user = users.get(predictedCheckins.get(i).getUser_id());
			Place prePlace = places.get(predictedCheckins.get(i).getPlace_id());
			int closePlaceNumberLimit = 1000;

			// Ignore all but close places
			ArrayList<Paired> placeToPlaceDistances = new ArrayList<Paired>();
			for (Place place : places.values()) {
				placeToPlaceDistances
						.add(new Paired(place.getId(), prePlace.getLocation().getDistance(place.getLocation())));
			}
			Collections.sort(placeToPlaceDistances);
			if (placeToPlaceDistances.get(1000).distance > 10) {
				System.out.println(prePlace.getId() + "wtf" + placeToPlaceDistances.get(1000).distance + " "
						+ places.get(placeToPlaceDistances.get(1000).id).getId());
			}
			avgDistance += placeToPlaceDistances.get(1000).distance;
		}
		System.out.println("avg dist : " + avgDistance / 3000);
	}

	public static void findoutliners() {
		for (User user : users.values()) {
			if (user.getNum_checkins() > 128) {
				System.out.println(user.getId());
			}
		}
	}

	// Test System
	public static void testAll() {
		double total = 0;
		for (int k = 5; k < 6; k++) {
			getPredictedCheckins(k);
			// calculateWeights();
			// calculateUsersPlaceDistances(1000);
			long tStart = System.currentTimeMillis();
			for (int i = 10; i < 11; i++) {
				for (int j = 1; j < 11; j++) {
					selectPredictionMethod(i, j * 10);
				}
				// for(int j=0;j<testVal.length;j++){
				// for(int t=0;t<testVal.length;t++){
				// for(int m=0;m<testVal.length;m++){
				// for(int l=0;l<testVal.length;l++){
				// wdistance = testVal[j];
				// wvisitedP = testVal[t];
				// wvisitedC = testVal[m];
				// wpopular = testVal[l];
				// makePredictions(i);
				// }
				// }
				// }
				// }
			}
			long tEnd = System.currentTimeMillis();
			long tDelta = tEnd - tStart;
			double elapsedSeconds = tDelta / 1000.0;
			total += elapsedSeconds;
			System.out.println(elapsedSeconds + " time");
		}
		System.out.println(total + " total time");
	}

	// Prediction Method
	// choice = 1 popular, = 2 closest, 3 = close-popular, 4 = popular-close, 5
	// = category-based, 6 = new-method, 7 = former visits
	public static void selectPredictionMethod(int choice, int listSize) {
		double apr = 0;
		int correctPredictions = 0;
		double prec = 0;
		if (choice == 1) {
			logResults("Popular Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = popularPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 2) {
			logResults("Close Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = closestPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 3) {
			logResults("Close Popular Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = closePopularPrediction(predictedCheckins.get(i), 1000, listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 4) {
			logResults("Popular Close Predictions");
			setMostPopularNPlaces(1000);
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = popularClosePrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 5) {
			logResults("Category-based Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = categoryPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 6) {
			logResults("New Method Predictions\n Distance : " + wdistance + " VisitedPlace : " + wvisitedP
					+ " VisitedCategory : " + wvisitedC + " PopularPlace : " + wpopular + " Time " + wtime);
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = newMethod(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 7) {
			logResults("Former visit Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = formerVisitPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 8) {
			logResults("Check-in Location Used Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = checkinLocationUsedPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 9) {
			logResults("Friendship Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = friendshipPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		} else if (choice == 10) {
			logResults("Proposed Method Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = proposedMethodPrediction(predictedCheckins.get(i), listSize);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				apr += result;
				giveBackCheckin(predictedCheckins.get(i));
			}
		}
		prec = prec / correctPredictions;
		apr = apr / predictedCheckins.size();
		System.out.println(correctPredictions + " " + prec);
		System.out.println(predictedCheckins.size());
		System.out.println((double) correctPredictions / predictedCheckins.size());
		System.out.println("apr :" + apr);
		logResults("\nCorrect Predictions: " + correctPredictions + "\nTotal Predictions: " + predictedCheckins.size()
				+ "\nAccuracy: " + ((double) correctPredictions / predictedCheckins.size()) + "\nPrecision : " + prec
				+ "\nList Size: " + listSize + "\n");
	}

	// Calculate sigma distribution of user check-ins
	public static void calculateSigma() {
		int activeUsers = 0;
		int rangeUsers = 0;
		double sum = 0;
		int numCheckins = 0;
		for (User user : users.values()) {
			if (user.isIs_active()) {
				activeUsers++;
				sum += user.getNum_checkins();
			}
		}
		System.out.println("Active-user checkin : " + sum + " Total Checkins :" + checkins.size());
		double m = sum / activeUsers;
		sum = 0;
		for (User user : users.values()) {
			if (user.isIs_active()) {
				sum += Math.pow(user.getNum_checkins() - m, 2);
			}
		}
		sum = sum / activeUsers;
		sum = Math.sqrt(sum);
		System.out.println(sum + " " + m);

		for (User user : users.values()) {
			if (user.isIs_active()) {
				if (user.getNum_checkins() > m - sum && user.getNum_checkins() < m + sum) {
					numCheckins += user.getNum_checkins();
					rangeUsers++;
				}
			}
		}
		System.out.println(
				"Kalan check-in : " + numCheckins + " range users: " + rangeUsers + " active users :" + activeUsers);

	}

	// Set active users whose check-ins over 8
	public static void setActiveUsers() {
		int activeCount = 0;
		int freqCount = 0;
		for (User user : users.values()) {
			if (user.getNum_checkins() > 0) {
				activeCount++;
				if (user.getNum_checkins() > 8) {
					freqCount++;
					user.setIs_active(true);
				} else {
					user.setIs_active(false);
				}
			} else {
				user.setIs_active(false);
			}
		}
		System.out.println(activeCount + " " + freqCount);
	}

	// Print Max Check-in Number By Unique Place Visited By Users
	public static void getMaxCheckinNumByUniquePlaceVisited() {
		int activeUserCount = 0;
		double maxCheckinCount = 0;
		for (User user : users.values()) {
			if (user.getNum_checkins() > 0) {
				activeUserCount++;
				maxCheckinCount += user.getVisitedPlaces().get(user.getVisitedPlaces().size() - 1).distance;
			}
		}
		System.out.println((double) maxCheckinCount / activeUserCount);
	}

	// Print Average Check-in Number By Users
	public static void getAvgCheckinNumByUniquePlaceVisited() {
		int activeUserCount = 0;
		int checkinCount = 0;
		for (User user : users.values()) {
			if (user.getNum_checkins() > 0) {
				activeUserCount++;
				checkinCount += (double) user.getNum_checkins() / user.getVisitedPlaces().size();
			}
		}
		System.out.println((double) checkinCount / activeUserCount);
	}

	// Print Average Visited Place Number By Users
	public static void getAvgUniquePlaceVisited() {
		int activeUserCount = 0;
		int uniquePlaceCount = 0;
		for (User user : users.values()) {
			if (user.getNum_checkins() > 0) {
				activeUserCount++;
				uniquePlaceCount += user.getVisitedLocations().size();
			}
		}
		System.out.println((double) uniquePlaceCount / activeUserCount);
	}

	// Print Active User Number On Selected City
	public static void getActiveUserNumByCity() {
		int count = 0;
		int checkinCount = 0;
		for (User user : users.values()) {
			if (user.getNum_checkins() > 0) {
				count++;
				checkinCount += user.getNum_checkins();
			}
		}
		System.out.println(count + " " + (double) checkinCount / count);
	}

	// Print Average Check-in Distances to User's Home
	public static void calculateAverageDistances() {
		double userDistance = 0;
		double totalAvgDistance = 0;
		double distance = 0;
		int userCount = 0;
		for (User user : users.values()) {
			ArrayList<Checkin> userCheckins = user.getCheckins();
			userDistance = 0;
			if (user.getNum_checkins() > 0) {
				for (Checkin checkin : userCheckins) {
					distance = user.getHomeLocation().getDistance(checkin.getPlace().getLocation());
					userDistance += distance;
				}
				totalAvgDistance += userDistance / userCheckins.size();
				userCount++;
			}
		}
		System.out.println(totalAvgDistance / userCount);
	}

	// Print Max Check-in Distance to User's Home
	public static void calculateMaxAvgDistances() {
		double userDistance = 0;
		double totalAvgDistance = 0;
		double distance = 0;
		int userCount = 0;
		for (User user : users.values()) {
			ArrayList<Checkin> userCheckins = user.getCheckins();
			userDistance = 0;
			if (user.getNum_checkins() > 0) {
				for (Checkin checkin : userCheckins) {
					distance = user.getHomeLocation().getDistance(checkin.getPlace().getLocation());
					if (distance > userDistance) {
						userDistance = distance;
					}
				}
				totalAvgDistance += userDistance;
				userCount++;
			}
		}
		System.out.println(totalAvgDistance / userCount);
	}

	// Calculate and Export to DB Place Popularity Time Ranges
	public static void calculatePlaceTimes() {
		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<Double> time1 = new ArrayList<Double>();
		ArrayList<Double> time2 = new ArrayList<Double>();
		ArrayList<Double> time3 = new ArrayList<Double>();
		ArrayList<Double> time4 = new ArrayList<Double>();

		Connection c = null;
		Statement stmt = null;
		int counter = 0;
		for (Place place : placesArray) {
			if (place.getNum_checkins() > 0) {
				int cat1 = 0;
				int cat2 = 0;
				int cat3 = 0;
				int cat4 = 0;
				counter++;
				for (int i = 0; i < place.checkin.size(); i++) {

					LocalDateTime ldt = checkinhm.get(place.checkin.get(i)).getTimestamp();
					int hour = ldt.getHourOfDay();
					if (hour > 0 && hour <= 6) {
						cat1++;
					} else if (hour > 6 && hour <= 12) {
						cat2++;
					} else if (hour > 12 && hour <= 18) {
						cat3++;
					} else {
						cat4++;
					}

				}
				int total = cat1 + cat2 + cat3 + cat4;
				System.out.println(counter);
				if (total == 0) {
					// try {
					// Class.forName("org.postgresql.Driver");
					// c =
					// DriverManager.getConnection("jdbc:postgresql://localhost:5432/"
					// + database_name, "postgres",
					// "02741903");
					// c.setAutoCommit(false);
					// // System.out.println("Opened database successfully");
					//
					// stmt = c.createStatement();
					// String sql = "delete from place where id=" +
					// place.getId() + ";";
					// stmt.executeUpdate(sql);
					// c.commit();
					// stmt.close();
					// c.close();
					// } catch (Exception e) {
					// System.err.println(e.getClass().getName() + ": " +
					// e.getMessage());
					// System.exit(0);
					// }
				} else {
					ids.add(place.getId());
					time1.add((double) cat1 / total);
					time2.add((double) cat2 / total);
					time3.add((double) cat3 / total);
					time4.add((double) cat4 / total);
					// try {
					// Class.forName("org.postgresql.Driver");
					// c =
					// DriverManager.getConnection("jdbc:postgresql://localhost:5432/"
					// + database_name, "postgres",
					// "02741903");
					// c.setAutoCommit(false);
					// // System.out.println("Opened database successfully");
					//
					// stmt = c.createStatement();
					// String sql = "UPDATE places set time_category_1 = " +
					// (double) cat1 / total
					// + ", time_category_2 = " + (double) cat2 / total + ",
					// time_category_3 = "
					// + (double) cat3 / total + ", time_category_4 = " +
					// (double) cat4 / total + " where id="
					// + place.getId() + ";";
					// stmt.executeUpdate(sql);
					// c.commit();
					// stmt.close();
					// c.close();
					// } catch (Exception e) {
					// System.err.println(e.getClass().getName() + ": " +
					// e.getMessage());
					// System.exit(0);
					// }
				}
			}
		}

		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/foursquare_austin", "postgres",
					"02741903");
			PreparedStatement ps = c.prepareStatement(
					"update places set time_category_1 = ?, time_category_2 =?,time_category_3 = ? , time_category_4 = ? where id = ?");
			if (true) {
				c.setAutoCommit(false);
			}
			for (int i = 0; i < time1.size(); i++) {
				ps.setDouble(1, time1.get(i));
				ps.setDouble(2, time2.get(i));
				ps.setDouble(3, time3.get(i));
				ps.setDouble(4, time4.get(i));
				ps.setInt(5, Integer.parseInt(ids.get(i)));
				ps.addBatch();
			}
			ps.executeBatch();
			if (true) {
				c.setAutoCommit(true);
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + " : " + e.getMessage());
			System.exit(0);
		}
	}

	// Proposed new method for check-in prediction
	public static double newMethod(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		// wdistance = user.wdistance;
		// wpopular = user.wpopular;
		// wtime = user.wtime;
		// wvisitedC = user.wvisitedC;
		// wvisitedP = user.wvisitedP;
		Place prePlace = predictedCheckin.getPlace();

		if (user.getPlaceDistances().size() == 0) {
			user.calculatePlaceDistances(1000);
		}
		ArrayList<Paired> placeDistances = user.getPlaceDistances();
		ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();
		double maxVisitedPlace = 0;
		for (int i = 0; i < visitedPlaces.size(); i++) {
			if (visitedPlaces.get(i).distance > maxVisitedPlace) {
				maxVisitedPlace = visitedPlaces.get(i).distance;
			}
		}
		int maxPlaceCheckin = placesArray.get(0).getNum_checkins();

		Integer[] visitedCategories = user.getVisitedCategories();

		int maxVisitedCategory = 0;
		for (int i = 0; i < visitedCategories.length; i++) {
			if (visitedCategories[i] > maxVisitedCategory) {
				maxVisitedCategory = visitedCategories[i];
			}
		}

		double maxPlaceDistance = 0;

		for (int i = 0; i < placeDistances.size(); i++) {
			if (placeDistances.get(i).distance > maxPlaceDistance) {
				maxPlaceDistance = placeDistances.get(i).distance;
			}
		}
		HashMap<String, PlaceRank> rankPlaces = new HashMap<String, PlaceRank>();
		// HashMap<Integer, PlaceRank> rankPlaces = new HashMap<Integer,
		// PlaceRank>();

		// Add popularity and visited categories to ranks
		for (int i = 0; i < placesArray.size(); i++) {

			rankPlaces
					.put(placesArray.get(i).getId(),
							new PlaceRank(placesArray.get(i).getId(), ((double) (placesArray.get(i).getNum_checkins()
									/ maxPlaceCheckin) * wpopular)
									+ (wvisitedC * (double) visitedCategories[placesArray.get(i).getCategory_id() - 1]
											/ maxVisitedCategory)));

			// if (((double) placesArray.get(i).getNum_checkins() /
			// maxPlaceCheckin * teta) > 1) {
			// System.out.println((double) placesArray.get(i).getNum_checkins()
			// / maxPlaceCheckin * teta);
			// }
			// if ((gamma * (double)
			// visitedCategories[placesArray.get(i).getCategory_id() - 1]
			// / maxVisitedCategory) > 2) {
			// System.out.println((gamma * (double)
			// visitedCategories[placesArray.get(i).getCategory_id() - 1]
			// / maxVisitedCategory));
			// }

		}

		// Add place distances to rank
		for (int i = 0; i < placeDistances.size(); i++) {
			double rank = rankPlaces.get(placeDistances.get(i).id).rankPoint;
			rankPlaces.put(placeDistances.get(i).id, new PlaceRank(placeDistances.get(i).id,
					rank + wdistance * (1 - (double) placeDistances.get(i).distance / maxPlaceDistance)));
			// if (rank + alfa - ((double) placeDistances.get(i).distance /
			// maxPlaceDistance) > 4) {
			// System.out.println(rank + alfa - ((double)
			// placeDistances.get(i).distance / maxPlaceDistance));
			// }
		}

		// Add visited places to ranks
		for (int i = 0; i < visitedPlaces.size(); i++) {
			double rank = rankPlaces.get(visitedPlaces.get(i).id).rankPoint;
			rankPlaces.put(visitedPlaces.get(i).id, new PlaceRank(visitedPlaces.get(i).id,
					rank + (wvisitedP * (double) visitedPlaces.get(i).distance / maxVisitedPlace)));
			// if (rank + (beta * (double) visitedPlaces.get(i).distance /
			// maxVisitedPlace) > 6) {
			// System.out.println(rank + (beta * (double)
			// visitedPlaces.get(i).distance / maxVisitedPlace));
			// }
		}

		int timeInterval = getTimeCategory(predictedCheckin);

		// Add time category to ranks
		for (int i = 0; i < placesArray.size(); i++) {
			double rank = rankPlaces.get(placesArray.get(i).getId()).rankPoint;
			rankPlaces.put(placesArray.get(i).getId(), new PlaceRank(placesArray.get(i).getId(),
					rank + (wtime * placesArray.get(i).getTime_category()[timeInterval])));
			// if (timeInterval == 0) {
			// rankPlaces.put(placesArray.get(i).getId(), new
			// PlaceRank(placesArray.get(i).getId(),
			// rank + (wtime * placesArray.get(i).getTime_category_1())));
			// } else if (timeInterval == 1) {
			// rankPlaces.put(placesArray.get(i).getId(), new
			// PlaceRank(placesArray.get(i).getId(),
			// rank + (wtime * placesArray.get(i).getTime_category_2())));
			// } else if (timeInterval == 2) {
			// rankPlaces.put(placesArray.get(i).getId(), new
			// PlaceRank(placesArray.get(i).getId(),
			// rank + (wtime * placesArray.get(i).getTime_category_3())));
			// } else if (timeInterval == 3) {
			// rankPlaces.put(placesArray.get(i).getId(), new
			// PlaceRank(placesArray.get(i).getId(),
			// rank + (wtime * placesArray.get(i).getTime_category_4())));
			// }
		}

		// // Ignore all but close places
		// ArrayList<Paired> pairs = new ArrayList<Paired>();
		// for (Place place : places.values()) {
		// pairs.add(new Paired(place.getId(),
		// prePlace.getLocation().getDistance(place.getLocation())));
		// }
		//
		// Collections.sort(pairs);
		// boolean first = true;
		// // System.out.println(pairs.get(250).distance);
		// for (int i = 0; i < pairs.size(); i++) {
		// if (pairs.get(i).distance > 0.25) {
		// if (first) {
		// closePlaceNumber += i;
		// first = false;
		// }
		// double rank = rankPlaces.get(pairs.get(i).id).rankPoint;
		// rankPlaces.put(pairs.get(i).id, new PlaceRank(pairs.get(i).id,
		// -100));
		// }
		// }
		// // Ignore all but close

		ArrayList<PlaceRank> rankedPlaces = new ArrayList<PlaceRank>();
		for (PlaceRank pr : rankPlaces.values()) {
			rankedPlaces.add(pr);
		}
		Collections.sort(rankedPlaces);

		for (int i = 0; i < num; i++) {
			if (rankedPlaces.get(i).id.equals(predictedCheckin.getPlace_id())) {
				// if (rankedPlaces.get(i).id ==
				// predictedCheckin.getPlace().getId()) {
				// System.out.println(i + " " + rankedPlaces.get(i).rankPoint +
				// " " + rankedPlaces.get(i).id + " "
				// + predictedCheckin.getPlace_id());
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	// Former visits for check-in prediction
	public static double formerVisitPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();

		ArrayList<PlaceRank> rankedPlaces = new ArrayList<PlaceRank>();
		// for (Place place : places.values()) {
		// rankedPlaces.add(new PlaceRank(place.getId(),
		// ThreadLocalRandom.current().nextDouble(-100, 0)));
		// }
		for (Paired pair : visitedPlaces) {
			rankedPlaces.add(new PlaceRank(pair.id, pair.distance));
		}
		Collections.sort(rankedPlaces);

		for (int i = 0; i < num; i++) {
			if (i >= rankedPlaces.size()) {
				break;
			}
			if (rankedPlaces.get(i).id.equals(predictedCheckin.getPlace_id())) {
				// if (rankedPlaces.get(i).id ==
				// predictedCheckin.getPlace().getId()) {
				// System.out.println(i + " " + rankedPlaces.get(i).rankPoint +
				// " " + rankedPlaces.get(i).id + " "
				// + predictedCheckin.getPlace_id());
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	// Check-in Location Used For Prediction
	public static double checkinLocationUsedPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		Place prePlace = places.get(predictedCheckin.getPlace_id());
		int closePlaceNumberLimit = 1000;

		// Ignore all but close places
		ArrayList<Paired> placeToPlaceDistances = new ArrayList<Paired>();
		for (Place place : places.values()) {
			placeToPlaceDistances
					.add(new Paired(place.getId(), prePlace.getLocation().getDistance(place.getLocation())));
		}
		Collections.sort(placeToPlaceDistances);
		int choice = 6;
		ArrayList<PlaceRank> ranks = new ArrayList<PlaceRank>();
		for (int i = 0; i < closePlaceNumberLimit; i++) {

			if (choice == 0) {
				ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id,
						places.get(placeToPlaceDistances.get(i).id).getNum_checkins()));
				// Using place popularity
			} else if (choice == 1) {
				ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id, places.get(placeToPlaceDistances.get(i).id)
						.getTime_category()[getTimeCategory(predictedCheckin)]));
				// Using time category
			} else if (choice == 2) {
				ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id,
						places.get(placeToPlaceDistances.get(i).id)
								.getTime_category()[getTimeCategory(predictedCheckin)]
								* places.get(placeToPlaceDistances.get(i).id).getNum_checkins()));
				// Using time_category and*popularity together
			} else if (choice == 3) {
				ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id,
						places.get(placeToPlaceDistances.get(i).id)
								.getTime_category()[getTimeCategory(predictedCheckin)]
								+ places.get(placeToPlaceDistances.get(i).id).getNum_checkins()
										/ mostPopularPlace.getNum_checkins()));
				// Using time_category and + popularity together
			} else if (choice == 4) {
				ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id, ThreadLocalRandom.current().nextDouble(0, 1))); // Random
			} else if (choice == 5) {
				// Former visits
				ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();
				for (int j = 0; j < visitedPlaces.size(); j++) {
					if (visitedPlaces.get(j).id.equals(placeToPlaceDistances.get(i).id)) {
						ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id, visitedPlaces.get(j).distance));
						break;
					}
				}
			}
		}

		if (choice == 6) { // category_pref[time_range*popularity]
			Place[] closePlaces = new Place[closePlaceNumberLimit];
			for (int i = 0; i < closePlaceNumberLimit; i++) {
				closePlaces[i] = places.get(placeToPlaceDistances.get(i).id);
			}
			Integer[] visitedCategories = user.getVisitedCategories();
			int timeRange = getTimeCategory(predictedCheckin);
			Place[] mostPopularByCategories = getMostPopularByCategoriesWithTimeRange(closePlaces, visitedCategories,
					num, timeRange);
			for (int k = 0; k < mostPopularByCategories.length; k++) {
				ranks.add(new PlaceRank(mostPopularByCategories[k].getId(), mostPopularByCategories[k].getNum_checkins()
						* mostPopularByCategories[k].getTime_category()[timeRange]));
			}
		}

		if (choice == 7) { // category_pref[popularity]
			Place[] closePlaces = new Place[closePlaceNumberLimit];
			for (int i = 0; i < closePlaceNumberLimit; i++) {
				closePlaces[i] = places.get(placeToPlaceDistances.get(i).id);
			}
			Integer[] visitedCategories = user.getVisitedCategories();
			Place[] mostPopularByCategories = getMostPopularByCategoriesFromGivenPlaces(closePlaces, visitedCategories,
					num);
			for (int k = 0; k < mostPopularByCategories.length; k++) {
				ranks.add(new PlaceRank(mostPopularByCategories[k].getId(),
						mostPopularByCategories[k].getNum_checkins()));
			}
		}

		Collections.sort(ranks);
		if (ranks.size() < num) {
			num = ranks.size();
			System.out.println(ranks.size());
		}
		for (int i = 0; i < num; i++) {
			if (ranks.get(i).id.equals(predictedCheckin.getPlace_id())) {
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	// Check-in Location Used For Prediction
	public static double proposedMethodPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		Place prePlace = places.get(predictedCheckin.getPlace_id());
		int closePlaceNumberLimit = 1000;

		// Ignore all but close places
		ArrayList<Paired> placeToPlaceDistances = new ArrayList<Paired>();
		for (Place place : places.values()) {
			placeToPlaceDistances
					.add(new Paired(place.getId(), prePlace.getLocation().getDistance(place.getLocation())));
		}
		Collections.sort(placeToPlaceDistances);

		ArrayList<PlaceRank> ranks = new ArrayList<PlaceRank>();
		ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();
		double maxVisitedNumber = visitedPlaces.get(0).distance;
		for (int i = 0; i < closePlaceNumberLimit; i++) {
			// Former visits
			for (int j = 0; j < visitedPlaces.size(); j++) {
				if (visitedPlaces.get(j).id.equals(placeToPlaceDistances.get(i).id)) {
					ranks.add(new PlaceRank(placeToPlaceDistances.get(i).id,
							visitedPlaces.get(j).distance / maxVisitedNumber));
					break;
				}
			}
			if (ranks.size() == num / 2) {
				break;
			}
		}

		int remaining = num - ranks.size();
		// category_pref[time_range*popularity]
		Place[] closePlaces = new Place[closePlaceNumberLimit];
		for (int i = 0; i < closePlaceNumberLimit; i++) {
			closePlaces[i] = places.get(placeToPlaceDistances.get(i).id);
		}
		Integer[] visitedCategories = user.getVisitedCategories();
		int timeRange = getTimeCategory(predictedCheckin);
		Place[] mostPopularByCategories = getMostPopularByCategoriesWithTimeRange(closePlaces, visitedCategories, num,
				timeRange);
		double maxPopularityNumber = 0;
		for (int i = 0; i < mostPopularByCategories.length; i++) {
			if (maxPopularityNumber < mostPopularByCategories[i].getNum_checkins()) {
				maxPopularityNumber = mostPopularByCategories[i].getNum_checkins();
			}
		}

		int counter = 0;
		for (int k = 0; k < mostPopularByCategories.length; k++) {
			if (!ranks.contains(places.get(mostPopularByCategories[k].getId()))) {
				ranks.add(new PlaceRank(mostPopularByCategories[k].getId(),
						mostPopularByCategories[k].getNum_checkins() / maxPopularityNumber));
				counter++;
				if (counter == remaining) {
					break;
				}
			}
		}

		Collections.sort(ranks);
		if (ranks.size() < num) {
			num = ranks.size();
			System.out.println(ranks.size());
		}
		for (int i = 0; i < num; i++) {
			if (ranks.get(i).id.equals(predictedCheckin.getPlace_id())) {
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	public static double friendshipPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		HashMap<String, PlaceRank> ranks = new HashMap<String, PlaceRank>();
		int choice = 0; // 0 for total visits, 1 for only first visits
		if (choice == 0) {
			if (user.getNum_friends() > 0) {
				for (int i = 0; i < user.getNum_friends(); i++) {
					User friend = user.getFriend(i);
					for (int j = 0; j < friend.getNum_checkins(); j++) {
						double rank = 0;
						if (ranks.get(friend.getCheckin(j).getPlace_id()) != null) {
							rank = ranks.get(friend.getCheckin(j).getPlace_id()).rankPoint;
						}
						ranks.put(friend.getCheckins().get(j).getPlace_id(),
								new PlaceRank(friend.getCheckins().get(j).getPlace_id(), rank + 1));
					}
				}
			}
		} else if (choice == 1) {
			if (user.getNum_friends() > 0) {
				for (int i = 0; i < user.getNum_friends(); i++) {
					User friend = user.getFriend(i);
					ArrayList<Paired> visitedPlaces = friend.getVisitedPlaces();
					for (int j = 0; j < visitedPlaces.size(); j++) {
						double rank = 0;
						if (ranks.get(visitedPlaces.get(j).id) != null) {
							rank = ranks.get(visitedPlaces.get(j).id).rankPoint;
						}
						ranks.put(visitedPlaces.get(j).id, new PlaceRank(visitedPlaces.get(j).id, rank + 1));
					}
				}
			}
		}
		ArrayList<PlaceRank> placeRanks = new ArrayList<PlaceRank>();

		for (PlaceRank pr : ranks.values()) {
			placeRanks.add(pr);
		}
		Collections.sort(placeRanks);
		if (placeRanks.size() < num) {
			num = placeRanks.size();
		}
		for (int i = 0; i < num; i++) {
			if (predictedCheckin.getPlace().getId().equals(placeRanks.get(i).id)) {
				return (double) (num - i) / (double) num;
			}
		}

		return 0;
	}

	// Category-based prediction
	public static double categoryPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		// Category mostVisited = categories.get(user.getMostVisitedCategory());
		// Place[] mostPopularByCategory = getMostPopularByCategory(mostVisited,
		// num);

		Integer[] visitedCategories = user.getVisitedCategories();
		Place[] mostPopularByCategories = getMostPopularByCategories(visitedCategories, num);

		for (int i = 0; i < mostPopularByCategories.length; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(mostPopularByCategory[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == mostPopularByCategories[i].getId()) {
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	// Get top N most popular places
	public static Place[] getMostPopularByCategories(Integer[] visitedCategories, int num) {
		ArrayList<Place> categoryPlace = new ArrayList<Place>();
		double sum = 0;
		for (int i : visitedCategories) {
			sum += i;
		}
		double[] categoryRate = new double[visitedCategories.length];
		for (int i = 0; i < categoryRate.length; i++) {
			categoryRate[i] = visitedCategories[i] / sum;
		}
		for (int i = 0; i < categoryRate.length; i++) {
			for (int j = 0; j < Math.ceil(categoryRate[i] * num); j++) {
				for (Place place : placesArray) {
					if (place.getCategory().getId() == i + 1) {
						if (categoryPlace.contains(place)) {
							continue;
						} else {
							categoryPlace.add(place);
							break;
						}
					}
				}
			}
		}
		int size = categoryPlace.size();
		if (size < num) {
			for (int i = 0; i < num - size; i++) {
				categoryPlace.add(placesArray.get(i));
			}
		}
		Collections.sort(categoryPlace);
		Place[] returnPlace = new Place[num];
		for (int i = 0; i < num; i++) {
			returnPlace[i] = categoryPlace.get(i);
		}
		return returnPlace;
	}

	// Get top N most popular places from given places
	public static Place[] getMostPopularByCategoriesFromGivenPlaces(Place[] closePlaces, Integer[] visitedCategories,
			int num) {
		ArrayList<Place> categoryPlace = new ArrayList<Place>();
		double sum = 0;
		for (int i : visitedCategories) {
			sum += i;
		}
		double[] categoryRate = new double[visitedCategories.length];
		for (int i = 0; i < categoryRate.length; i++) {
			categoryRate[i] = visitedCategories[i] / sum;
		}

		ArrayList<PlaceRank> placePopularity = new ArrayList<PlaceRank>();
		for (int i = 0; i < closePlaces.length; i++) {
			placePopularity.add(new PlaceRank(closePlaces[i].getId(), closePlaces[i].getNum_checkins()));
		}
		Collections.sort(placePopularity);

		for (int i = 0; i < categoryRate.length; i++) {
			for (int j = 0; j < Math.ceil(categoryRate[i] * num); j++) {
				for (int k = 0; k < placePopularity.size(); k++) {
					Place place = places.get(placePopularity.get(k).id);
					if (place.getCategory().getId() == i + 1) {
						if (categoryPlace.contains(place)) {
							continue;
						} else {
							categoryPlace.add(place);
							break;
						}
					}
				}
			}
		}
		int size = categoryPlace.size();
		if (size < num) {
			for (int i = 0; i < num - size; i++) {
				categoryPlace.add(placesArray.get(i));
			}
		}
		Collections.sort(categoryPlace);
		Place[] returnPlace = new Place[num];
		for (int i = 0; i < num; i++) {
			returnPlace[i] = categoryPlace.get(i);
		}
		return returnPlace;
	}

	// Get top N most popular places
	public static Place[] getMostPopularByCategoriesWithTimeRange(Place[] closePlaces, Integer[] visitedCategories,
			int num, int timeRange) {
		ArrayList<Place> categoryPlace = new ArrayList<Place>();
		double sum = 0;
		for (int i : visitedCategories) {
			sum += i;
		}
		double[] categoryRate = new double[visitedCategories.length];
		for (int i = 0; i < categoryRate.length; i++) {
			categoryRate[i] = visitedCategories[i] / sum;
		}

		ArrayList<PlaceRank> placePopularityByTimeRange = new ArrayList<PlaceRank>();
		for (int i = 0; i < closePlaces.length; i++) {
			placePopularityByTimeRange.add(new PlaceRank(closePlaces[i].getId(),
					closePlaces[i].getNum_checkins() * closePlaces[i].getTime_category()[timeRange]));
		}
		Collections.sort(placePopularityByTimeRange);
		for (int i = 0; i < categoryRate.length; i++) {
			for (int j = 0; j < Math.ceil(categoryRate[i] * num); j++) {
				for (int k = 0; k < placePopularityByTimeRange.size(); k++) {
					Place place = places.get(placePopularityByTimeRange.get(k).id);
					if (place.getCategory().getId() == i + 1) {
						if (categoryPlace.contains(place)) {
							continue;
						} else {
							categoryPlace.add(place);
							break;
						}
					}
				}
			}
		}
		int size = categoryPlace.size();
		if (size < num) {
			for (int i = 0; i < num - size; i++) {
				categoryPlace.add(placesArray.get(i));
			}
		}
		ArrayList<Place> orderedPlaces = new ArrayList<Place>();
		for (int i = 0; i < categoryPlace.size(); i++) {
			orderedPlaces.add(
					new Place(categoryPlace.get(i).getId() + "", categoryPlace.get(i).getCategory_id(), 0.0, 0.0, "",
							(int) Math.ceil(categoryPlace.get(i).getNum_checkins()
									* categoryPlace.get(i).getTime_category()[timeRange]),
							new Location(0.0, 0.0), 0.0, 0.0, 0.0, 0.0));
		}
		Collections.sort(orderedPlaces);
		Place[] returnPlace = new Place[num];
		for (int i = 0; i < num; i++) {
			returnPlace[i] = orderedPlaces.get(i);
		}
		return returnPlace;
	}

	// Get most popular places by category
	@SuppressWarnings("unchecked")
	public static Place[] getMostPopularByCategory(Category mostVisited, int num) {
		ArrayList<Place> categoryPlace = new ArrayList<Place>();
		for (int i = 0; i < placesArray.size(); i++) {
			if (placesArray.get(i).getCategory() == mostVisited) {
				categoryPlace.add(placesArray.get(i));
			}
		}
		int size = categoryPlace.size();
		if (size < num) {
			for (int i = 0; i < num - size; i++) {
				categoryPlace.add(placesArray.get(i));
			}
		}
		Collections.sort(categoryPlace);
		Place[] returnPlace = new Place[num];
		for (int i = 0; i < num; i++) {
			returnPlace[i] = categoryPlace.get(i);
		}
		return returnPlace;
	}

	// Users Home Locations Are Calculated
	public static void calculateUsersHomeLocations() {
		int counter = 0;
		for (User user : users.values()) {
			System.out.println(counter++);
			// user.calculateHomeLocationByAvg();
			user.calculateHomeLocationByFreq();
			// System.out.println(user.getHomeLocation());
		}
	}

	// Calculate and set top N closest Places to Users
	public static void calculateUsersPlaceDistances(int num) {
		int i = 0;
		for (User user : users.values()) {
			if (user.getNum_checkins() > 0) {
				user.calculatePlaceDistances(num);
				i++;
				System.out.println(i);
			}
		}
	}

	// Most Popular Places Are Setted
	@SuppressWarnings("unchecked")
	public static void setMostPopularPlaces() {
		Collections.sort(placesArray);
		for (int i = 0; i < mostPopularPlaces.length; i++) {
			mostPopularPlaces[i] = placesArray.get(i);
		}
		mostPopularPlace = mostPopularPlaces[0];
	}

	// Set most popular N places
	@SuppressWarnings("unchecked")
	public static void setMostPopularNPlaces(int size) {
		Collections.sort(placesArray);
		mostPopularNPlaces = new Place[size];
		for (int i = 0; i < mostPopularNPlaces.length; i++) {
			mostPopularNPlaces[i] = placesArray.get(i);
		}
	}

	// get top N most popular places from given places list
	public static Place[] getMostPopularNPlaces(Place[] place, int size) {
		Arrays.sort(place);
		mostPopularNPlaces = new Place[size];
		for (int i = 0; i < mostPopularNPlaces.length; i++) {
			mostPopularNPlaces[i] = place[i];
		}
		return mostPopularNPlaces;
	}

	// First find closest than popular
	public static double closePopularPrediction(Checkin predictedCheckin, int closeNum, int popularNum) {
		User user = users.get(predictedCheckin.getUser_id());
		Place[] closestPlaces = getClosestPlaces(user, closeNum);
		Place[] closePopularPlaces = getMostPopularNPlaces(closestPlaces, popularNum);
		for (int i = 0; i < popularNum; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(closePopularPlaces[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == closePopularPlaces[i].getId()) {
				return (double) (popularNum - i) / (double) popularNum;
			}
		}
		return 0;
	}

	// First find populars than closest
	public static double popularClosePrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		Place[] closestPlaces = getClosestNPlaces(user, num);
		for (int i = 0; i < num; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(closestPlaces[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == closestPlaces[i].getId()) {
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	// Popular Prediction Method
	public static double popularPrediction(Checkin predictedCheckin, int num) {
		for (int j = 0; j < num; j++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(mostPopularPlaces[j].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == mostPopularPlaces[j].getId()) {
				return (double) (num - j) / (double) num;
			}
		}
		return 0;
	}

	// Close Prediction Method
	// Id equals control with equals not ==
	public static double closestPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		Place[] closestPlaces = getClosestPlaces(user, num);
		for (int i = 0; i < num; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(closestPlaces[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == closestPlaces[i].getId()) {
				return (double) (num - i) / (double) num;
			}
		}
		return 0;
	}

	// Get place distances to user
	public static ArrayList<Paired> getPlaceDistances(User user) {
		ArrayList<Paired> pairs = new ArrayList<Paired>();
		for (Place place : places.values()) {
			pairs.add(new Paired(place.getId(), user.getHomeLocation().getDistance(place.getLocation())));
		}
		return pairs;
	}

	// Closest Places For All Places Returned
	public static Place[] getClosestPlaces(User user, int num) {
		Place[] closestPlaces = new Place[num];
		ArrayList<Paired> pairs = user.getPlaceDistances();
		for (int i = 0; i < num; i++) {
			closestPlaces[i] = places.get(pairs.get(i).id);
		}
		return closestPlaces;
	}

	// Closest Places For N Places Returned
	public static Place[] getClosestNPlaces(User user, int num) {
		Place[] closestPlaces = new Place[num];
		ArrayList<Paired> pairs = new ArrayList<Paired>();
		for (int i = 0; i < mostPopularNPlaces.length; i++) {
			pairs.add(new Paired(mostPopularNPlaces[i].getId(),
					user.getHomeLocation().getDistance(mostPopularNPlaces[i].getLocation())));
		}
		Collections.sort(pairs);
		for (int i = 0; i < num; i++) {
			closestPlaces[i] = places.get(pairs.get(i).id);
		}
		return closestPlaces;
	}

	// public static boolean makePrediction(Checkin predictedCheckin) {
	// if (predictedCheckin.getPlace().getId() == mostPopularPlace.getId()) {
	// return true;
	// } else
	// return false;
	// }
	//
	// public static boolean makePrediction(Checkin predictedCheckin, Place
	// place) {
	// if (predictedCheckin.getPlace().getId() == place.getId()) {
	// return true;
	// } else
	// return false;
	// }

	// Most Popular Place Returned
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

	// Checkins which are going to be predicted
	public static void getPredictedCheckins(int startMonth) {
		// predictedCheckins.clear();
		// Timestamp ts = new Timestamp(year, startMonth, 20, 0, 0, 0, 0);
		// LocalDateTime startDate = new LocalDateTime(ts.getTime(), jodaTzUTC);
		// Timestamp ts2 = new Timestamp(year, startMonth + 1, 20, 0, 0, 0, 0);
		// LocalDateTime endDate = new LocalDateTime(ts2.getTime(), jodaTzUTC);
		// logResults("Start Date : " + startDate.toString() + " End Date:" +
		// endDate.toString() + "\n");
		int count = 0;
		//
		// for (int i = 0; i < checkins.size(); i++) {
		// if (checkins.get(i).getTimestamp().isAfter(startDate) &&
		// checkins.get(i).getTimestamp().isBefore(endDate)
		// && users.get(checkins.get(i).getUser_id()).isIs_active()) {
		// predictedCheckins.add(checkins.get(i));
		// // deleteForPrediction(checkins.get(i));
		// count++;
		// }
		// if (count == 60000) {
		// break;
		// }
		// }
		// for (int i = 0; i < checkins.size(); i++) {
		// // if (count == 10000) {
		// // break;
		// // }
		// if (users.get(checkins.get(i).getUser_id()).isIs_active()) {
		// predictedCheckins.add(checkins.get(i));
		// count++;
		// }
		// }
		ArrayList<Integer> randomNums = new ArrayList<Integer>();
		for (int i = 0; i < checkins.size(); i++) {
			randomNums.add(i);
		}
		long seed = System.nanoTime();
		Collections.shuffle(randomNums, new Random(seed));
		for (int i = 0; i < 10000; i++) {
			if (users.get(checkins.get(randomNums.get(i)).getUser_id()).isIs_active()) {
				predictedCheckins.add(checkins.get(randomNums.get(i)));
			}
		}

	}

	// Calculate weights according to given condition
	public static void calculateWeights() {
		// FOR WEKA OUTPUT
		// double w1 = 0;
		// double w2 = 0;
		// double w3 = 0;
		// double w4 = 0;
		// double w5 = 0;
		// int counter = 0;
		// for (User user : users.values()) {
		// System.out.println(counter);
		// counter++;
		// ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();
		// for (int i = 0; i < visitedPlaces.size(); i++) {
		// if (user.getPlaceDistances().size() == 0) {
		// user.calculatePlaceDistances(1000);
		// }
		// // System.out.println(user.getId() + " "+
		// // checkins.get(i).getPlace_id() + " " +
		// // user.getNum_checkins());
		// Place prePlace = places.get(visitedPlaces.get(i).id);
		//
		// Collections.sort(visitedPlaces);
		// double maxVisitedPlace = visitedPlaces.get(visitedPlaces.size() -
		// 1).distance;
		//
		// int placeVisitNum = user.getVisitPlaceNum(prePlace);
		// w2 = (double) placeVisitNum / maxVisitedPlace;
		//
		// double dist = user.getPlaceDistance(prePlace);
		//
		// w1 = 1 - (double) dist / user.getMaxPlaceDistance();
		// int maxPlaceCheckin = placesArray.get(0).getNum_checkins();
		// int placeCheckin = prePlace.getNum_checkins();
		//
		// w4 = (double) placeCheckin / maxPlaceCheckin;
		//
		// Integer[] visitedCategories = user.getVisitedCategories();
		// int visitedCategory = user.getVisitCatNum(prePlace.getCategory());
		// int maxVisitedCategory = 0;
		// for (int k = 0; k < visitedCategories.length; k++) {
		// if (visitedCategories[k] > maxVisitedCategory) {
		// maxVisitedCategory = visitedCategories[k];
		// }
		// }
		// w3 = (double) visitedCategory / maxVisitedCategory;
		//
		// ArrayList<Checkin> userCheckins = user.getCheckins();
		// Checkin selectedCheckin = null;
		// for (Checkin checkin : userCheckins) {
		// if (checkin.getPlace_id().equals(prePlace.getId())) {
		// selectedCheckin = checkin;
		// }
		// }
		//
		// int time_cat = getTimeCategory(selectedCheckin);
		// if (time_cat == 1) {
		// w5 = prePlace.getTime_category_1();
		// } else if (time_cat == 2) {
		// w5 = prePlace.getTime_category_2();
		// } else if (time_cat == 3) {
		// w5 = prePlace.getTime_category_3();
		// } else if (time_cat == 4) {
		// w5 = prePlace.getTime_category_4();
		// }
		// // }
		// try {
		// PrintWriter out = new PrintWriter(new BufferedWriter(new
		// FileWriter("weka.txt", true)));
		// out.println(w1 + "," + w4 + "," + w3 + "," + w5 + "," + w2 + "," +
		// w2);
		// out.close();
		// } catch (IOException e) {
		// // exception handling left as an exercise for the reader
		// }
		// }
		// }

		// FOR PERSONAL WEIGHTS
		double w1 = 0;
		double w2 = 0;
		double w3 = 0;
		double w4 = 0;
		double w5 = 0;
		int counter = 0;
		for (User user : users.values()) {
			w1 = 0;
			w2 = 0;
			w3 = 0;
			w4 = 0;
			w5 = 0;
			System.out.println(counter);
			ArrayList<Checkin> userCheckins = user.getCheckins();
			for (Checkin checkin : userCheckins) {
				Place prePlace = checkin.getPlace();
				if (user.getPlaceDistances().size() == 0) {
					user.calculatePlaceDistances(1000);
				}
				ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();
				Collections.sort(visitedPlaces);
				double maxVisitedPlace = visitedPlaces.get(visitedPlaces.size() - 1).distance;

				int placeVisitNum = user.getVisitPlaceNum(prePlace);
				w2 = (double) placeVisitNum / maxVisitedPlace;

				double dist = user.getPlaceDistance(prePlace);

				w1 = 1 - (double) dist / user.getMaxPlaceDistance();
				int maxPlaceCheckin = placesArray.get(0).getNum_checkins();
				int placeCheckin = checkin.getPlace().getNum_checkins();

				w4 = (double) placeCheckin / maxPlaceCheckin;

				Integer[] visitedCategories = user.getVisitedCategories();
				int visitedCategory = user.getVisitCatNum(prePlace.getCategory());
				int maxVisitedCategory = 0;
				for (int k = 0; k < visitedCategories.length; k++) {
					if (visitedCategories[k] > maxVisitedCategory) {
						maxVisitedCategory = visitedCategories[k];
					}
				}
				w3 = (double) visitedCategory / maxVisitedCategory;

				int time_cat = getTimeCategory(checkin);
				w5 = prePlace.getTime_category()[time_cat];

			}
			user.wdistance = w1 / user.getNum_checkins();
			user.wpopular = w4 / user.getNum_checkins();
			user.wvisitedP = w2 / user.getNum_checkins();
			user.wtime = w5 / user.getNum_checkins();
			user.wvisitedC = w3 / user.getNum_checkins();
			counter++;
		}

		// for (int i = 0; i < checkins.size(); i++) {
		// // if (!predictedCheckins.contains(checkins.get(i))) {
		// System.out.println(counter);
		// counter++;
		// User user = users.get(checkins.get(i).getUser_id());
		// Place prePlace = checkins.get(i).getPlace();
		// if (user.getPlaceDistances().size() == 0) {
		// user.calculatePlaceDistances(1000);
		// }
		// // System.out.println(user.getId() + " "+
		// // checkins.get(i).getPlace_id() + " " +
		// // user.getNum_checkins());
		//
		// ArrayList<Paired> visitedPlaces = user.getVisitedPlaces();
		//
		// Collections.sort(visitedPlaces);
		// double maxVisitedPlace = visitedPlaces.get(visitedPlaces.size() -
		// 1).distance;
		//
		// int placeVisitNum = user.getVisitPlaceNum(prePlace);
		// w2 = (double) placeVisitNum / maxVisitedPlace;
		//
		// double dist = user.getPlaceDistance(prePlace);
		//
		// w1 = 1 - (double) dist / user.getMaxPlaceDistance();
		// int maxPlaceCheckin = placesArray.get(0).getNum_checkins();
		// int placeCheckin = checkins.get(i).getPlace().getNum_checkins();
		//
		// w4 = (double) placeCheckin / maxPlaceCheckin;
		//
		// Integer[] visitedCategories = user.getVisitedCategories();
		// int visitedCategory = user.getVisitCatNum(prePlace.getCategory());
		// int maxVisitedCategory = 0;
		// for (int k = 0; k < visitedCategories.length; k++) {
		// if (visitedCategories[k] > maxVisitedCategory) {
		// maxVisitedCategory = visitedCategories[k];
		// }
		// }
		// w3 = (double) visitedCategory / maxVisitedCategory;
		//
		// int time_cat = getTimeCategory(checkins.get(i));
		// if (time_cat == 1) {
		// w5 = prePlace.getTime_category_1();
		// } else if (time_cat == 2) {
		// w5 = prePlace.getTime_category_2();
		// } else if (time_cat == 3) {
		// w5 = prePlace.getTime_category_3();
		// } else if (time_cat == 4) {
		// w5 = prePlace.getTime_category_4();
		// }
		// // }
		// try {
		// PrintWriter out = new PrintWriter(new BufferedWriter(new
		// FileWriter("weka.txt", true)));
		// out.println(w1 + "," + w4 + "," + w3 + "," + w5 + "," + w2);
		// out.close();
		// } catch (IOException e) {
		// // exception handling left as an exercise for the reader
		// }
		// }

		// wdistance = w1 / checkins.size();
		// wvisitedP = w2 / checkins.size();
		// wvisitedC = w3 / checkins.size();
		// wpopular = w4 / checkins.size();
		// wtime = w5 / checkins.size();
	}

	// Return time category for given check-in
	public static int getTimeCategory(Checkin checkin) {
		LocalDateTime ldt = checkin.getTimestamp();
		int hour = ldt.getHourOfDay();
		if (hour > 0 && hour <= 6) {
			return 0;
		} else if (hour > 6 && hour <= 12) {
			return 1;
		} else if (hour > 12 && hour <= 18) {
			return 2;
		} else {
			return 3;
		}
	}

	// Delete check-in from the system before prediction
	public static void deleteForPrediction(Checkin checkin) {
		User user = users.get(checkin.getUser_id());
		user.deleteCheckin(checkin);
		places.get(checkin.getPlace_id()).setNum_checkins(places.get(checkin.getPlace_id()).getNum_checkins() - 1);
	}

	// Give back the predicted check-in to system
	public static void giveBackCheckin(Checkin checkin) {
		User user = users.get(checkin.getUser_id());
		user.addCheckin(checkin);
		places.get(checkin.getPlace_id()).setNum_checkins(places.get(checkin.getPlace_id()).getNum_checkins() + 1);
	}

	// Monthly checkin number calculated
	public static void getMonthlyCheckinNumber(int startMonth) {
		Date startDate = new Date(111, startMonth, 20);
		Date endDate = new Date(111, startMonth + 1, 20);
		int checkinCount = 0;
		int userCount = 0;

		// for (int i = 0; i < checkins.size(); i++) {
		// if (checkins.get(i).getTimestamp().after(startDate) &&
		// checkins.get(i).getTimestamp().before(endDate)) {
		// checkinCount++;
		// predictedCheckins.add(checkins.get(i));
		// }
		// }

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

	// Data load method
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
			System.out.println("Opened database successfully users");

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
				users.put(id, new User(id, 0, 0));
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
			System.out.println("Opened database successfully categories");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM categories;");
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = null;
				if (!database_name.contains("foursquare")) {
					name = rs.getString("name"); // for gowalla : name ,
				} else {
					name = rs.getString("category");
				}
				// foursquare : category
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
			System.out.println("Opened database successfully places");

			stmt = c.createStatement();
			ResultSet rs = null;
			if (database_name.contains("foursquare")) {
				rs = stmt.executeQuery("SELECT * FROM place;"); // foursquare
																// :
																// place
																// ,
																// gowalla
																// :
																// places
			} else {
				rs = stmt.executeQuery("SELECT * FROM places where city = '" + city + "';");
			}
			while (rs.next()) {
				String id = null;
				if (database_name.contains("foursquare")) {
					id = rs.getString("id");
				} else {
					int id_i = rs.getInt("id");
					id = id_i + "";
				}
				// String id = rs.getString("id");
				String name = rs.getString("name");
				int category_id = rs.getInt("category_id");
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");
				int num_checkins = rs.getInt("num_checkins");

				double time_category_1 = rs.getDouble("time_category_1");
				double time_category_2 = rs.getDouble("time_category_2");
				double time_category_3 = rs.getDouble("time_category_3");
				double time_category_4 = rs.getDouble("time_category_4");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				if (num_checkins > 0) {
					Place place = new Place(id + "", category_id, lat, lon, name, num_checkins, new Location(lat, lon),
							time_category_1, time_category_2, time_category_3, time_category_4);
					place.setCategory(categories.get(category_id));
					places.put(id + "", place);
					placesArray.add(place);
				}
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
			System.out.println("Opened database successfully checkins");

			stmt = c.createStatement();
			String sql = "SELECT * FROM checkins;";
			if (database_name.equals("gowalla_u")) {
				sql = sql.substring(0, sql.length() - 1) + " where city = '" + city + "';";
			}
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
				String place_id = null;
				Timestamp ts;
				if (database_name.contains("foursquare")) {
					place_id = rs.getString("place_id");
					ts = rs.getTimestamp("time");
				} else {
					int place_id_i = rs.getInt("place_id");
					place_id = place_id_i + "";
					ts = rs.getTimestamp("timestamp");
				}
				// int num_checkins = rs.getInt("num_checkins");
				LocalDateTime ldt = new LocalDateTime(ts.getTime(), jodaTzUTC);
				// Date timestamp = rs.getDate("timestamp");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Checkin checkin = new Checkin(id, user_id, place_id, 0, ldt);
				checkin.setPlace(places.get(place_id));
				checkins.add(checkin);
				checkinhm.put(checkin.getId(), checkin); /////
				// places.get(place_id).checkin.add(checkin.getId()); //////
				users.get(user_id).addCheckin(checkin);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		// Friends load
		if (database_name.equals("gowalla_u")) {
			try {
				Class.forName("org.postgresql.Driver");
				c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
						"02741903");
				c.setAutoCommit(false);
				System.out.println("Opened database successfully friends");

				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM friendships;");
				while (rs.next()) {
					int id = rs.getInt("user1");
					int friend_id = rs.getInt("user2");
					if (users.containsKey(id) && users.containsKey(friend_id)) {
						users.get(id).addFriend(users.get(friend_id));
					}
				}
				rs.close();
				stmt.close();
				c.close();
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}
		}
		// System.out.println("1000000");
		// For loading whole gowalla database
		// if (!database_name.equals("foursquare")) {
		// for (int i = 1; i < 8; i++) {
		// try {
		// Class.forName("org.postgresql.Driver");
		// c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" +
		// database_name, "postgres",
		// "02741903");
		// c.setAutoCommit(false);
		// System.out.println("Opened database successfully");
		//
		// stmt = c.createStatement();
		// ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id >"
		// + ((i * 1000000))
		// + " and id < " + (((i + 1) * 1000000) + 1) + ";");
		// while (rs.next()) {
		// int id = rs.getInt("id");
		// int user_id = rs.getInt("user_id");
		// int place_id_i = rs.getInt("place_id");
		// String place_id = place_id_i + "";
		// int num_checkins = rs.getInt("num_checkins");
		// Timestamp ts = rs.getTimestamp("timestamp");
		// LocalDateTime ldt = new LocalDateTime(ts.getTime(), jodaTzUTC);
		// // System.out.println("ID = " + num_checkins);
		// // System.out.println("user_id = " + id);
		// //
		// // System.out.println();
		// Checkin checkin = new Checkin(id, user_id, place_id, num_checkins,
		// ldt);
		// checkin.setPlace(places.get(place_id));
		// checkins.add(checkin);
		//// checkinhm.put(checkin.getId(), checkin); /////
		//// places.get(place_id).checkin.add(checkin.getId()); //////
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
		// database_name, "postgres",
		// "02741903");
		// c.setAutoCommit(false);
		// System.out.println("Opened database successfully");
		//
		// stmt = c.createStatement();
		// ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id >
		// 8000000;");
		// while (rs.next()) {
		// int id = rs.getInt("id");
		// int user_id = rs.getInt("user_id");
		// int place_id_i = rs.getInt("place_id");
		// String place_id = place_id_i + "";
		// int num_checkins = rs.getInt("num_checkins");
		// Timestamp ts = rs.getTimestamp("timestamp");
		// LocalDateTime ldt = new LocalDateTime(ts.getTime(), jodaTzUTC);
		// // System.out.println("ID = " + num_checkins);
		// // System.out.println("user_id = " + id);
		// //
		// // System.out.println();
		// Checkin checkin = new Checkin(id, user_id, place_id, num_checkins,
		// ldt);
		// checkin.setPlace(places.get(place_id));
		// checkins.add(checkin);
		//// checkinhm.put(checkin.getId(), checkin); /////
		//// places.get(place_id).checkin.add(checkin.getId()); //////
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
		// }
		// System.out.println(users.get(14462276).getCheckins().get(0).getPlace().getCategory().getName());
		System.out.println(checkins.size());
		System.out.println("Operation done successfully");
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println(elapsedSeconds);
	}

	// Data write to plain text file
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

	// Log results for debugging
	public static void logResults(String result) {
		try {
			File newTextFile = new File("results.txt");

			FileWriter fw = new FileWriter(newTextFile, true);
			fw.write(result);
			fw.close();

		} catch (IOException iox) {
			// do stuff with exception
			iox.printStackTrace();
		}
	}

}
