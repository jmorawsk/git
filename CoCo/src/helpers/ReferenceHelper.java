package helpers;

import java.util.HashMap;

import data.Location;
import data.User;

public class ReferenceHelper {
	public static void setLocationReferences(HashMap<String, Location> locations, HashMap<Integer, User> users) {
		for(String locationId : locations.keySet()) {
			locations.get(locationId).setLocations(locations);
			locations.get(locationId).setUsers(users);
		}
	}

	public static void setUserReferences(HashMap<String, Location> locations, HashMap<Integer, User> users,
			HashMap<Integer, HashMap<Integer, Boolean>> friendshipMap) {
		for(Integer id : users.keySet()) {
			users.get(id).setLocations(locations);
			users.get(id).setUsers(users);
			HashMap<Integer, Boolean> userFriends = friendshipMap.get(id);
			if(userFriends == null){
				userFriends = new HashMap<Integer, Boolean>();
			}
			users.get(id).setFriendshipMap(userFriends);
		}
	}
}
