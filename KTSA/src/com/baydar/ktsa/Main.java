package com.baydar.ktsa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

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
	static ArrayList<Checkin> predictedCheckins = new ArrayList<Checkin>();
	static ArrayList<Place> placesArray = new ArrayList<Place>();
	static Place mostPopularPlace;
	static Place[] mostPopularPlaces = new Place[50];
	static Place[] mostPopularNPlaces;

	static int[] testVal;

	static double wdistance = 1;
	static double wvisitedP = 5;
	static double wvisitedC = 1;
	static double wpopular = 3;
	static double wtime = 2;

	public static void main(String[] args) {

		testVal = new int[2];
		testVal[0] = 5;
		testVal[1] = 15;

		String database_name = "gowalla_u";
		loadData(database_name);
		// getMonthlyCheckinNumber(6);
		mostPopularPlace = getMostPopularPlace();
		setMostPopularPlaces();
		calculateUsersHomeLocations();
		Collections.sort(placesArray); // Sort by checkin nums
		// calculateUsersPlaceDistances(1000);
		// calculatePlaceTimes(); for once do it for gowalla, too
		// testAll();
		calculateAverageDistances();
		calculateMaxAvgDistances();
	}

	public static void calculateAverageDistances() {
		double userDistance = 0;
		double totalAvgDistance = 0;
		double distance = 0;
		for (User user : users.values()) {
			ArrayList<Checkin> userCheckins = user.getCheckins();
			userDistance = 0;
			for (Checkin checkin : userCheckins) {
				distance = user.getHomeLocation().getDistance(checkin.getPlace().getLocation());
				userDistance += distance;
			}
			totalAvgDistance += userDistance / userCheckins.size();
		}
		System.out.println(totalAvgDistance / users.size());
	}

	public static void calculateMaxAvgDistances() {
		double userDistance = 0;
		double totalAvgDistance = 0;
		double distance = 0;
		for (User user : users.values()) {
			ArrayList<Checkin> userCheckins = user.getCheckins();
			userDistance = 0;
			for (Checkin checkin : userCheckins) {
				distance = user.getHomeLocation().getDistance(checkin.getPlace().getLocation());
				if (distance > userDistance) {
					userDistance = distance;
				}
			}
			totalAvgDistance += userDistance;
		}
		System.out.println(totalAvgDistance / users.size());
	}

	public static void calculatePlaceTimes() {

		Connection c = null;
		Statement stmt = null;
		int counter = 0;
		for (Place place : placesArray) {
			if (place.getNum_checkins() < 10) {
				int cat1 = 0;
				int cat2 = 0;
				int cat3 = 0;
				int cat4 = 0;
				counter++;
				for (Checkin checkin : checkins) {
					if (checkin.getPlace_id().equals(place.getId())) {
						LocalDateTime ldt = checkin.getTimestamp();
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
				}
				int total = cat1 + cat2 + cat3 + cat4;
				System.out.println(counter);
				if (total == 0) {
					try {
						Class.forName("org.postgresql.Driver");
						c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/foursquare", "postgres",
								"02741903");
						c.setAutoCommit(false);
						// System.out.println("Opened database successfully");

						stmt = c.createStatement();
						String sql = "delete from place where id='" + place.getId() + "';";
						stmt.executeUpdate(sql);
						c.commit();
						stmt.close();
						c.close();
					} catch (Exception e) {
						System.err.println(e.getClass().getName() + ": " + e.getMessage());
						System.exit(0);
					}
				} else {
					try {
						Class.forName("org.postgresql.Driver");
						c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/foursquare", "postgres",
								"02741903");
						c.setAutoCommit(false);
						// System.out.println("Opened database successfully");

						stmt = c.createStatement();
						String sql = "UPDATE place set time_category_1 = " + (double) cat1 / total
								+ ", time_category_2 = " + (double) cat2 / total + ", time_category_3 = "
								+ (double) cat3 / total + ", time_category_4 = " + (double) cat4 / total + " where id='"
								+ place.getId() + "';";
						stmt.executeUpdate(sql);
						c.commit();
						stmt.close();
						c.close();
					} catch (Exception e) {
						System.err.println(e.getClass().getName() + ": " + e.getMessage());
						System.exit(0);
					}
				}
			}
		}
	}

	public static void testAll() {
		double total = 0;
		for (int k = 1; k < 2; k++) {
			getPredictedCheckins(k);
			// calculateUsersPlaceDistances(1000);
			long tStart = System.currentTimeMillis();
			for (int i = 6; i < 7; i++) {

				makePredictions(i);

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
			System.out.println(elapsedSeconds);
		}
		System.out.println(total);
	}

	// Prediction Method
	// choice = 1 popular, = 2 closest,
	public static void makePredictions(int choice) {
		int correctPredictions = 0;
		double prec = 0;
		if (choice == 1) {
			logResults("Popular Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				if (popularPrediction(predictedCheckins.get(i))) {
					correctPredictions++;
				}
			}
		} else if (choice == 2) {
			logResults("Close Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				if (closestPrediction(predictedCheckins.get(i), 50)) {
					correctPredictions++;
				}
			}
		} else if (choice == 3) {
			logResults("Close Popular Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				if (closePopularPrediction(predictedCheckins.get(i), 1000, 50)) {
					correctPredictions++;
				}
			}
		} else if (choice == 4) {
			logResults("Popular Close Predictions");
			setMostPopularNPlaces(1000);
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				if (popularClosePrediction(predictedCheckins.get(i), 50)) {
					correctPredictions++;
				}
			}
		} else if (choice == 5) {
			logResults("Category-based Predictions");
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				if (categoryPrediction(predictedCheckins.get(i), 50)) {
					correctPredictions++;
				}
			}
		} else if (choice == 6) {
			logResults("New Method Predictions\n Distance : " + wdistance + " VisitedPlace : " + wvisitedP
					+ " VisitedCategory : " + wvisitedC + " PopularPlace : " + wpopular + " Time " + wtime);
			prec = 0;
			for (int i = 0; i < predictedCheckins.size(); i++) {
				System.out.println(i);
				deleteForPrediction(predictedCheckins.get(i));
				double result = newMethod(predictedCheckins.get(i), 10);
				if (result != 0) {
					correctPredictions++;
					prec += result;
				}
				giveBackCheckin(predictedCheckins.get(i));
			}
			prec = prec / correctPredictions;
		}

		System.out.println(correctPredictions + " " + prec);
		System.out.println(predictedCheckins.size());
		System.out.println((double) correctPredictions / predictedCheckins.size());
		logResults("\nCorrect Predictions: " + correctPredictions + "\nTotal Predictions: " + predictedCheckins.size()
				+ "\nAccuracy: " + ((double) correctPredictions / predictedCheckins.size()) + "\nPrecision : " + prec
				+ "\n");
	}

	public static double newMethod(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
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
		for (int i = 0; i < placesArray.size(); i++) {

			rankPlaces.put(placesArray.get(i).getId(),
					new PlaceRank(placesArray.get(i).getId(),
							((double) placesArray.get(i).getNum_checkins() / maxPlaceCheckin * wpopular)
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
		int hour = predictedCheckin.getTimestamp().getHourOfDay();
		int timeInterval = 0;
		if (hour > 0 && hour <= 6) {
			timeInterval = 0;
		} else if (hour > 6 && hour <= 12) {
			timeInterval = 1;
		} else if (hour > 12 && hour <= 18) {
			timeInterval = 2;
		} else {
			timeInterval = 3;
		}

		for (int i = 0; i < placesArray.size(); i++) {
			double rank = rankPlaces.get(placesArray.get(i).getId()).rankPoint;
			if (timeInterval == 0) {
				rankPlaces.put(placesArray.get(i).getId(), new PlaceRank(placesArray.get(i).getId(),
						rank + (wtime * placesArray.get(i).getTime_category_1())));
			} else if (timeInterval == 1) {
				rankPlaces.put(placesArray.get(i).getId(), new PlaceRank(placesArray.get(i).getId(),
						rank + (wtime * placesArray.get(i).getTime_category_2())));
			} else if (timeInterval == 2) {
				rankPlaces.put(placesArray.get(i).getId(), new PlaceRank(placesArray.get(i).getId(),
						rank + (wtime * placesArray.get(i).getTime_category_3())));
			} else if (timeInterval == 3) {
				rankPlaces.put(placesArray.get(i).getId(), new PlaceRank(placesArray.get(i).getId(),
						rank + (wtime * placesArray.get(i).getTime_category_4())));
			}
		}

		ArrayList<PlaceRank> rankedPlaces = new ArrayList<PlaceRank>();
		for (PlaceRank pr : rankPlaces.values()) {
			rankedPlaces.add(pr);
		}
		Collections.sort(rankedPlaces);
		// if(rankedPlaces.get(0).rankPoint>6){
		// System.out.println(rankedPlaces.get(0).rankPoint);
		// }
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

	public static boolean categoryPrediction(Checkin predictedCheckin, int num) {
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
				return true;
			}
		}
		return false;
	}

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
		for (User user : users.values()) {
			// user.calculateHomeLocationByAvg();
			user.calculateHomeLocationByFreq();
			// System.out.println(user.getHomeLocation());
		}
	}

	public static void calculateUsersPlaceDistances(int num) {
		int i = 0;
		for (User user : users.values()) {
			user.calculatePlaceDistances(num);
			i++;
			System.out.println(i);
		}
	}

	// Most Popular Places Are Setted
	@SuppressWarnings("unchecked")
	public static void setMostPopularPlaces() {
		Collections.sort(placesArray);
		for (int i = 0; i < mostPopularPlaces.length; i++) {
			mostPopularPlaces[i] = placesArray.get(i);
		}
	}

	@SuppressWarnings("unchecked")
	public static void setMostPopularNPlaces(int size) {
		Collections.sort(placesArray);
		mostPopularNPlaces = new Place[size];
		for (int i = 0; i < mostPopularNPlaces.length; i++) {
			mostPopularNPlaces[i] = placesArray.get(i);
		}
	}

	public static Place[] getMostPopularNPlaces(Place[] place, int size) {
		Arrays.sort(place);
		mostPopularNPlaces = new Place[size];
		for (int i = 0; i < mostPopularNPlaces.length; i++) {
			mostPopularNPlaces[i] = place[i];
		}
		return mostPopularNPlaces;
	}

	// First find closest than popular
	public static boolean closePopularPrediction(Checkin predictedCheckin, int closeNum, int popularNum) {
		User user = users.get(predictedCheckin.getUser_id());
		Place[] closestPlaces = getClosestPlaces(user, closeNum);
		Place[] closePopularPlaces = getMostPopularNPlaces(closestPlaces, popularNum);
		for (int i = 0; i < popularNum; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(closePopularPlaces[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == closePopularPlaces[i].getId()) {
				return true;
			}
		}
		return false;
	}

	// First find populars than closest
	public static boolean popularClosePrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		Place[] closestPlaces = getClosestNPlaces(user, num);
		for (int i = 0; i < num; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(closestPlaces[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == closestPlaces[i].getId()) {
				return true;
			}
		}
		return false;
	}

	// Popular Prediction Method
	public static boolean popularPrediction(Checkin predictedCheckin) {
		for (int j = 0; j < mostPopularPlaces.length; j++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(mostPopularPlaces[j].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == mostPopularPlaces[j].getId()) {
				return true;
			}
		}
		return false;
	}

	// Close Prediction Method
	// Id equals control with equals not ==
	public static boolean closestPrediction(Checkin predictedCheckin, int num) {
		User user = users.get(predictedCheckin.getUser_id());
		Place[] closestPlaces = getClosestPlaces(user, num);
		for (int i = 0; i < num; i++) {
			// if
			// (predictedCheckin.getPlace().getId().equals(closestPlaces[i].getId()))
			// {
			if (predictedCheckin.getPlace().getId() == closestPlaces[i].getId()) {
				return true;
			}
		}
		return false;
	}

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

	// Checkins which is going to be predicted
	public static void getPredictedCheckins(int startMonth) {
		predictedCheckins.clear();
		Timestamp ts = new Timestamp(111, startMonth, 20, 0, 0, 0, 0);
		LocalDateTime startDate = new LocalDateTime(ts.getTime(), jodaTzUTC);
		Timestamp ts2 = new Timestamp(111, startMonth + 1, 20, 0, 0, 0, 0);
		LocalDateTime endDate = new LocalDateTime(ts2.getTime(), jodaTzUTC);
		logResults("Start Date : " + startDate.toString() + " End Date:" + endDate.toString() + "\n");
		int count = 0;

		for (int i = 0; i < checkins.size(); i++) {
			if (checkins.get(i).getTimestamp().isAfter(startDate) && checkins.get(i).getTimestamp().isBefore(endDate)) {
				predictedCheckins.add(checkins.get(i));
				// deleteForPrediction(checkins.get(i));
				count++;
			}
			if (count == 5000) {
				break;
			}
		}
	}

	public static void deleteForPrediction(Checkin checkin) {
		User user = users.get(checkin.getUser_id());
		user.setNum_checkins(user.getNum_checkins() - 1);
		user.deleteCheckin(checkin);
		places.get(checkin.getPlace_id()).setNum_checkins(places.get(checkin.getPlace_id()).getNum_checkins() - 1);
	}

	public static void giveBackCheckin(Checkin checkin) {
		User user = users.get(checkin.getUser_id());
		user.setNum_checkins(user.getNum_checkins() - 1);
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
				String name = rs.getString("name"); // for gowalla : name ,
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
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM places;"); // foursquare
																		// :
																		// place
																		// ,
																		// gowalla
																		// :
																		// places
			while (rs.next()) {
				int id = rs.getInt("id");
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
				Place place = new Place(id + "", category_id, lat, lon, name, num_checkins, new Location(lat, lon),
						time_category_1, time_category_2, time_category_3, time_category_4);
				place.setCategory(categories.get(category_id));
				places.put(id + "", place);
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
			ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id < 1000001;");
			while (rs.next()) {
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
//				String place_id = rs.getString("place_id");
				int place_id_i = rs.getInt("place_id");
				String place_id = place_id_i + "";
				// int num_checkins = rs.getInt("num_checkins");
				Timestamp ts = rs.getTimestamp("timestamp");
				LocalDateTime ldt = new LocalDateTime(ts.getTime(), jodaTzUTC);
				// Date timestamp = rs.getDate("timestamp");
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Checkin checkin = new Checkin(id, user_id, place_id, 0, ldt);
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

		for (int i = 1; i < 8; i++) {
			try {
				Class.forName("org.postgresql.Driver");
				c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
						"02741903");
				c.setAutoCommit(false);
				System.out.println("Opened database successfully");

				stmt = c.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id >" + ((i * 1000000)) + " and id < "
						+ (((i + 1) * 1000000) + 1) + ";");
				while (rs.next()) {
					int id = rs.getInt("id");
					int user_id = rs.getInt("user_id");
					int place_id_i = rs.getInt("place_id");
					String place_id = place_id_i + "";
					int num_checkins = rs.getInt("num_checkins");
					Timestamp ts = rs.getTimestamp("timestamp");
					LocalDateTime ldt = new LocalDateTime(ts.getTime(), jodaTzUTC);
					// System.out.println("ID = " + num_checkins);
					// System.out.println("user_id = " + id);
					//
					// System.out.println();
					Checkin checkin = new Checkin(id, user_id, place_id, num_checkins, ldt);
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
			System.out.println(i * 1000000);
		}

		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + database_name, "postgres",
					"02741903");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM checkins where id > 8000000;");
			while (rs.next()) {
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
				int place_id_i = rs.getInt("place_id");
				String place_id = place_id_i + "";
				int num_checkins = rs.getInt("num_checkins");
				Timestamp ts = rs.getTimestamp("timestamp");
				LocalDateTime ldt = new LocalDateTime(ts.getTime(), jodaTzUTC);
				// System.out.println("ID = " + num_checkins);
				// System.out.println("user_id = " + id);
				//
				// System.out.println();
				Checkin checkin = new Checkin(id, user_id, place_id, num_checkins, ldt);
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

//		System.out.println(users.get(14462276).getCheckins().get(0).getPlace().getCategory().getName());
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
