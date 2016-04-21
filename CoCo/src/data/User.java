package data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class User {

	private int id;
	private HashMap<String, ArrayList<Date>> checkIns;
	private HashMap<String, Float> checkInWeights;
	private float totalCheckInWeight;
	private HashMap<Integer, Boolean> friendshipMap;
	private HashMap<Integer, Float> friendshipWeightMap;
	private HashMap<Integer, Float> neighborhood;
	private HashMap<Integer, User> users;
	private HashMap<String, Location> locations;
	private String previousCheckInLocation;
	private Date previousCheckInTime;
	private Double thresholdDistance, farThresholdDistance;

	public User(int id) {
		this.setId(id);
		checkIns = new HashMap<String, ArrayList<Date>>();
		checkInWeights = new HashMap<String, Float>();
		this.friendshipWeightMap = new HashMap<Integer, Float>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashMap<String, ArrayList<Date>> getCheckIns() {
		return checkIns;
	}

	public void addCheckIn(String locationId, Date date) {

		if(!this.checkIns.containsKey(locationId))
			this.checkIns.put(locationId, new ArrayList<Date>());
		this.checkIns.get(locationId).add(date);
		if(this.previousCheckInTime == null || this.previousCheckInTime.before(date)){
			this.previousCheckInTime = date;
			this.previousCheckInLocation = locationId;
		}
	}
	
	public ArrayList<Date> getCheckinsForLocation(String locationId){
		return this.checkIns.get(locationId);
	}

	public HashMap<String, Float> getCheckInWeights() {
		return checkInWeights;
	}

	public void setCheckInWeight(String locationId, float weight) {
		this.checkInWeights.put(locationId, weight);
	}

	public float getCheckInWeight(String locationId) {
		return this.checkInWeights.get(locationId);
	}

	public float getTotalCheckInWeight() {
		return this.totalCheckInWeight;
	}
	
	public void setTotalCheckInWeight(float total) {
		this.totalCheckInWeight = total;
	}

	public HashMap<Integer, Float> getFriendshipWeightMap() {
		return friendshipWeightMap;
	}

	public void setFriendshipWeightMap(HashMap<Integer, Float> friendshipWeightMap) {
		this.friendshipWeightMap = friendshipWeightMap;
	}

	public HashMap<Integer, Float> getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(HashMap<Integer, Float> neighborhood) {
		this.neighborhood = neighborhood;
	}

	public HashMap<String, Location> getLocations() {
		return locations;
	}

	public void setLocations(HashMap<String, Location> locations) {
		this.locations = locations;
	}

	public HashMap<Integer, User> getUsers() {
		return users;
	}

	public void setUsers(HashMap<Integer, User> users) {
		this.users = users;
	}

	public HashMap<Integer, Boolean> getFriendshipMap() {
		return friendshipMap;
	}

	public void setFriendshipMap(HashMap<Integer, Boolean> friendshipMap) {
		this.friendshipMap = friendshipMap;
	}

	public Double getThresholdDistance() {
		return thresholdDistance;
	}

	public void setThresholdDistance(Double thresholdDistance) {
		this.thresholdDistance = thresholdDistance;
	}

	public Double getFarThresholdDistance() {
		return farThresholdDistance;
	}

	public void setFarThresholdDistance(Double farThresholdDistance) {
		this.farThresholdDistance = farThresholdDistance;
	}

	public String getPreviousCheckInLocation() {
		// TODO Auto-generated method stub
		return previousCheckInLocation;
	}

	public Date getPreviousCheckInTime() {
		return previousCheckInTime;
	}
	
	public int getFriendCount(){
		return this.friendshipMap.size();
	}
	
}
