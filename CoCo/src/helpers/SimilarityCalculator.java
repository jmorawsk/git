package helpers;

import java.util.HashMap;

import data.User;

public class SimilarityCalculator {

	public static Float getSimilarity(User user1, User user2, float socialWeight) {

		float dist = 0f;
		float thisOffset = 0f;
		float otherOffset = 0f;
		
		float overlap = 0f;

		for(String locationId : user1.getCheckIns().keySet()) {
			if(user2.getCheckIns().get(locationId) != null) {
				overlap += user1.getCheckInWeight(locationId) * user2.getCheckInWeight(locationId);
//				overlap++;
			}
		}
		float scale = 1.0f;

		scale = (float) overlap / (float) (user1.getTotalCheckInWeight() + user2.getTotalCheckInWeight() - overlap);
//		scale = (float) overlap / (float) (user1.getCheckIns().keySet().size() + user2.getCheckIns().keySet().size() - overlap);

		//negative social weight means don't use
		if(socialWeight >= 0){
			if(user1.getFriendshipWeightMap().containsKey(user2.getId()))
				scale *= socialWeight + (1f - socialWeight) * user1.getFriendshipWeightMap().get(user2.getId());
			else
				scale *= socialWeight;
		}
		return scale;
	}
	
	public static void generateNeighborhood(User user, int neighborhoodSize, float socialWeight) {

		HashMap<User, Float> similarities = new HashMap<User, Float>();

		//for each location the user visits check the similarity of all other users
		for(String locationId : user.getCheckIns().keySet()) {
			for(Integer otherId : user.getLocations().get(locationId).getCheckIns().keySet()) {
				User other = user.getUsers().get(otherId);
				if(!otherId.equals(user.getId()) && similarities.get(other) == null && other != null) {
					float sim = getSimilarity(user,other,socialWeight);
					if(sim > 0) {
						//System.out.println("Similarity between " + this.id + " and " + otherId + " is " + sim);
						similarities.put(other, sim);
					}
				}
			}
		}
//		System.out.println("User"+user.getId()+"checks"+user.getCheckIns().size()+
//				"sim"+similarities.size());
//		System.exit(0);
		//get the top N out of similarities
		user.setNeighborhood(getTop(similarities, neighborhoodSize));
	}
	
	private static HashMap<Integer, Float> getTop(HashMap<User, Float> sim, int count) {

		HashMap<Integer, Float> top = new HashMap<Integer, Float>();
		Integer lowestUser = null;
		float lowestValue = Float.MAX_VALUE;

		for(User user : sim.keySet()) {
//			System.out.println("User" +user.getId()+ " Value" + sim.get(user) + "Size" +top.keySet().size());
			if(top.keySet().size() < count) {
				top.put(user.getId(), sim.get(user));
			}
			else {
//				System.out.println(top.keySet().size() + " " + count);
				if(lowestValue == Float.MAX_VALUE) {
					lowestUser = getLowestUser(top);
					lowestValue = top.get(lowestUser);
				}				
				if(lowestValue < sim.get(user)) {
					top.put(user.getId(), sim.get(user));
					top.remove(lowestUser);
					lowestUser = getLowestUser(top);
					lowestValue = top.get(lowestUser);
				}
			}
		}
		return top;
	}

	private static Integer getLowestUser(HashMap<Integer, Float> data) {
		Integer lowestUser = null;
		float lowestValue = Float.MAX_VALUE;
		for(Integer id : data.keySet()) {
			if(data.get(id) < lowestValue) {
				lowestValue = data.get(id);
				lowestUser = id;
			}
		}
		return lowestUser;
	}
}
