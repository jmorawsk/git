package recommender;

import java.util.HashMap;
import java.util.Vector;

import data.Predictable;
import data.User;
import helpers.SimilarityCalculator;

class NeighborhoodGenerator extends Thread {

	private Vector<Integer> targets;
	private HashMap<Integer, User> data;
	private int id;
	private int neighborhoodSize;
	private float socialWeight;
	
	public NeighborhoodGenerator(Vector<Integer> targets, HashMap<Integer, User> data, int id,
			int neighborhoodSize, float socialWeight) {
		this.targets = targets;
		this.data = data;
		this.id = id;
		this.neighborhoodSize = neighborhoodSize;
		this.socialWeight = socialWeight;
	}
	
	public void run() {
		for(Object target : targets) {
			SimilarityCalculator.generateNeighborhood(data.get(target), neighborhoodSize, socialWeight);
		}
	}
	
	
	
}
