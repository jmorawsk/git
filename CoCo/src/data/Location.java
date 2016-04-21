package data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Location {
	private String id;
	private Coordinates coords;
	private HashMap<Integer, ArrayList<Date>> checkIns;
	private HashMap<Integer, User> users;
	private HashMap<String, Location> locations;
	
	public Location(String locationId){
		this.setId(locationId);

		this.checkIns = new HashMap<Integer, ArrayList<Date>>();
	}
	
	public double getDistance(Location other){
		return coords.getDistance(other.getCoords());
	}

	public Coordinates getCoords() {
		return coords;
	}

	public void setCoords(float latitude, float longitude) {
		this.coords = new Coordinates(latitude, longitude);
	}
	
	public HashMap<Integer, ArrayList<Date>> getCheckIns() {
		return this.checkIns;
	}
	
	public void addCheckIn(Integer id, Date date) {
		if(!this.checkIns.containsKey(id))
			this.checkIns.put(id, new ArrayList<Date>());
		this.checkIns.get(id).add(date);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HashMap<Integer, User> getUsers() {
		return users;
	}

	public void setUsers(HashMap<Integer, User> users) {
		this.users = users;
	}

	public HashMap<String, Location> getLocations() {
		return locations;
	}

	public void setLocations(HashMap<String, Location> locations) {
		this.locations = locations;
	}
}
