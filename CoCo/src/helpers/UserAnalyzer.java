package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import data.Checkin;
import data.Location;
import data.User;


public class UserAnalyzer {

	public static void calculateCheckInWeights(User user, Date firstDate, Date lastDate){
		float totalCheckInWeight = 0;
		for(String event : user.getCheckIns().keySet()) {
			float value = 0f;
			Date current = null;
			for(Date date : user.getCheckIns().get(event)) {
				
				//Linear time based only on last visit
				if(current == null || date.after(current))
				{
					current = date;
					value = (float) (date.getTime() - firstDate.getTime()) / (float) (lastDate.getTime() - firstDate.getTime());
				}
			}
			user.setCheckInWeight(event, value);
			totalCheckInWeight += value;
		}
		user.setTotalCheckInWeight(totalCheckInWeight);
	}
	
	public static void analyzeDistances(User user){
		ArrayList<Checkin> checkInList = new ArrayList<Checkin>();

		for(String locationId : user.getCheckIns().keySet()) {
			Location location = user.getLocations().get(locationId);
			for(Date date : user.getCheckinsForLocation(locationId)) {
				checkInList.add(new Checkin(user, location, date));
			}			
		}
		Collections.sort(checkInList);

		//this.previousCheckIn = checkInList.get(checkInList.size()-1);
		ArrayList<Double> distances = new ArrayList<Double>();
		ArrayList<Double> sameDayDistances = new ArrayList<Double>();
		ArrayList<Double> diffDayDistances = new ArrayList<Double>();

		for(int i = 0; i < checkInList.size()-1; i++) {
			Double distance = checkInList.get(i).getLocation().getDistance(checkInList.get(i+1).getLocation());
			//filter out extreme outlier distances to preserve the quality of the mean
			if(distance < 1000.0 && distance >= 0) {
				distances.add(distance);
				
				if(DateHelper.areSameDay(checkInList.get(i).getTime(),
						checkInList.get(i+1).getTime()))
					sameDayDistances.add(distance);
				else
					diffDayDistances.add(distance);
			}
		}
		Collections.sort(sameDayDistances);
		Collections.sort(diffDayDistances);
		user.setThresholdDistanceMean(ListHelper.mean(sameDayDistances));
		user.setFarThresholdDistanceMean(ListHelper.mean(distances));
		user.setThresholdDistanceMedian(ListHelper.median(sameDayDistances));
		user.setFarThresholdDistanceMedian(ListHelper.median(distances));
	}
	
	public static void analyzeFriendshipCovisits(User user) {

		float eventsAttended = 0;
		for(ArrayList<Date> dates : user.getCheckIns().values()) {	
			eventsAttended += dates.size();
		}
//		System.out.println("User" + user.getId());
//		System.out.println("User" + user.getFriendshipMap().size());
		for(Integer id : user.getFriendshipMap().keySet()) {
			if(!user.getUsers().containsKey(id))
				continue;
			User friend = user.getUsers().get(id);
			float covisits = 0f;
			for(String locationId : user.getCheckIns().keySet()) {				
				if(friend.getCheckIns().containsKey(locationId)) {
					covisits += howManyMatch(friend.getCheckinsForLocation(locationId),
							user.getCheckIns().get(locationId));
				}
			}
			user.getFriendshipWeightMap().put(id, covisits/eventsAttended);
		}
	}
	
	private static float howManyMatch(ArrayList<Date> list1, ArrayList<Date> list2) {

		float match = 0f;
		for(Date date1 : list1) {
			for(Date date2 : list2) {
				if(DateHelper.areSameDay(date1, date2)) {
					match++;
					break;
				}
				//try to decrease window, didn't help
//				if(date1.getDay() == date2.getDay() && date1.getHours() == date2.getHours()) {
//					match++;
//				}
			}
		}
		return match;
	}
}
