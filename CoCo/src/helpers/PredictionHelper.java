package helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import data.Location;
import data.User;

public class PredictionHelper {
	public static Float predict(User user, Location location) {

		float total = 0f;
		boolean found = false;

			for(Integer id : user.getNeighborhood().keySet()) {
				User other = user.getUsers().get(id);
				if(other.getCheckinsForLocation(location.getId()) != null) {
					found = true;
					total += user.getNeighborhood().get(id);
				}
			}
				/*
				 * TODO determine if using everything below here, remember to search gaussian locations
				 * spatial stuff
				 */
				
				
//				double distance = book.getDistance(getPreviousCheckIn().getEvent());
//
////				if( getPreviousCheckIn().getTime().getDay() == day) {
////					total *= Runner.gaussian(thresholdDistance, distance);
////				} else {
//				double weight = Runner.gaussian(farThresholdDistance, distance);
//				if(Double.isNaN(weight)){
//					weight = 0.5;
//				}
//				total *= weight;
//				}
				
				/*
				 * End of spatial stuff
				 */
			
		if(!found) {
			return -1f;
		}
		return total;
	}
	
	public static HashMap<String, Float> topNpredictions(User user, int n, boolean novel) {
		HashMap<String, Float> top = new HashMap<String, Float>();
		String lowest = "";

		for(Integer id : user.getNeighborhood().keySet()) {
			for(String item : user.getUsers().get(id).getCheckIns().keySet()) {
				if(top.containsKey(item))
					continue;
				if(novel && user.getCheckIns().containsKey(item)){
					continue;
				}
				Float value = predict(user, user.getLocations().get(item));
				if(top.size() < n) {
					top.put(item, value);
					lowest = getLowest(top);
				}else {
					lowest = getLowest(top);
					if (value > top.get(lowest)) {
						top.remove(lowest);
						top.put(item, value);
						lowest = getLowest(top);
					}
				}
			}
		}
//		System.out.println("User " + user.getId() + "Top size" + top.size()
//		+ "Neighbors" +user.getNeighborhood().size());
//		System.exit(0);
//		if(!top.isEmpty()){
//			System.out.println(top.size());
//		}
		return top;
	}
	
	public static HashMap<String, Float> topNPredictionsForUserSet(User user, int n, Set<Integer> userSet, boolean novel) {
		HashMap<String, Float> top = new HashMap<String, Float>();
		String lowest = "";

		for(Integer id : userSet) {
			if(user.getUsers().get(id) == null){
				continue;
			}
			for(String item : user.getUsers().get(id).getCheckIns().keySet()) {
				if(top.containsKey(item))
					continue;
				if(novel && user.getCheckIns().containsKey(item)){
					continue;
				}
				Float value = predict(user, user.getLocations().get(item));
				if(top.size() < n) {
					top.put(item, value);
					lowest = getLowest(top);
				}else {
					lowest = getLowest(top);
					if (value > top.get(lowest)) {
						top.remove(lowest);
						top.put(item, value);
						lowest = getLowest(top);
					}
				}
			}
		}
//		System.out.println("User " + user.getId() + "Top size" + top.size()
//		+ "Neighbors" +user.getNeighborhood().size());
//		System.exit(0);
//		if(!top.isEmpty()){
//			System.out.println(top.size());
//		}
		return top;
	}
	
	public static HashMap<String, Float> topNFriendPredictions(User user, int n, boolean novel) {

		return topNPredictionsForUserSet(user, n, user.getFriendshipMap().keySet(), novel);
	}

	//TODO: could refactor this method to merge with getLowestUser
	private static String getLowest(HashMap<String, Float> data) {
		String lowest = null;
		float lowestValue = Float.MAX_VALUE;
		for(String id : data.keySet()) {
			if(data.get(id) < lowestValue) {
				lowestValue = data.get(id);
				lowest = id;
			}
		}
		return lowest;
	}
	
	public static Float spatialPredictionFar(User user, Location location, Double gaussScale){
		if(user.getLocations() == null){
			return 1f;
		}
		Location previous = user.getLocations().get(user.getPreviousCheckInLocation());
		if(previous == null){
			return 1f;
		}
		double distance = location.getDistance(previous);

		return (float) MathHelper.gaussian(user.getFarThresholdDistance(), distance, gaussScale);
	}
	
	public static Float spatialPredictionNear(User user, Location location, Double gaussScale){
		if(user.getLocations() == null){
			return 1f;
		}
		Location previous = user.getLocations().get(user.getPreviousCheckInLocation());
		if(previous == null){
			return 1f;
		}
		double distance = location.getDistance(previous);

		return (float) MathHelper.gaussian(user.getThresholdDistance(), distance, gaussScale);
	}
	
	public static Float socialPrediction(User user, Location location, float socialWeight){
		float total = 0f;
		float totalFriendship = 0f;
		if(user.getFriendshipWeightMap() == null){
			return 0f;
		}
		for(int friendId : user.getFriendshipWeightMap().keySet()){
			float friendship = user.getFriendshipWeightMap().get(friendId);
			if(location.getCheckIns() != null && location.getCheckIns().containsKey(friendId)){
				total += user.getFriendshipWeightMap().get(friendId)*friendship;
			}
			totalFriendship += friendship;
		}
		return total/totalFriendship;
	}
	
	public static Float temporalPredictionLatest(User user, Location location, Date startDate, Date currentDate){
		Date lastDate = null;
		if(location.getCheckIns()== null){
			return 0f;
		}
		for(ArrayList<Date> userVisits : location.getCheckIns().values()){
			Date firstInArray = userVisits.get(0);
			Date lastInArray = userVisits.get(userVisits.size() - 1);
			Date max = DateHelper.maxDate(firstInArray, lastInArray);
			lastDate = DateHelper.maxDate(lastDate,max);
		}
		return (float) (lastDate.getTime() - startDate.getTime()) / (float) (currentDate.getTime() - startDate.getTime());
	}
	
	public static Float temporalPredictionGeneral(User user, Location location, Date startDate, Date currentDate){
		float total = 0f;
		if(location.getCheckIns()== null){
			return 0f;
		}
		for(int visitorId : location.getCheckIns().keySet()){
			User visitor = location.getUsers().get(visitorId);
			total += visitor.getCheckInWeight(location.getId())/visitor.getTotalCheckInWeight();
		}
		return total/ (location.getCheckIns().size());
	}
	
}
