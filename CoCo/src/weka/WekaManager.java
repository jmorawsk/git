package weka;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import data.Location;
import data.User;
import helpers.Dataset;
import helpers.Logger;
import helpers.PredictionHelper;
import helpers.Dataset.COMPETITOR;
import helpers.MapHelper;
import recommender.Recommender;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class WekaManager {
	private Dataset dataset;
	private Logger logger;
	private HashMap<Integer, User> trainUsers;
	private HashMap<String, Location> trainLocations;
	private HashMap<Integer, User> evalUsers;
	private HashMap<String, Location> evalLocations;
	private Float socialWeight;
	private Double gaussScale;
	private boolean novel;
	private Date endOfTraining;
	private double PREDICTION_WEIGHT_THRESHOLD = 2854.3;
	private double PREDICTION_WEIGHT_FACTOR = 0.001;

	public WekaManager(Logger logger, Recommender recommender){
		this(recommender.getDataset(), recommender.getSocialWeight(), recommender.getGaussScale(), recommender.isNovel());
		this.logger = logger;
		this.endOfTraining = recommender.getEndOfTraining();
		this.assignMappings(recommender.getTrainingUsers(), recommender.getTrainingLocations(),
				recommender.getEvalUsers(), recommender.getEvalLocations());
	}
	
	private WekaManager(Dataset dataset, float socialWeight,Double gaussScale, boolean novel){
		this.dataset = dataset;
		this.socialWeight = socialWeight;
		this.gaussScale = gaussScale;
		this.novel = novel;
	}

	private void assignMappings(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			HashMap<Integer, User> evalUsers, HashMap<String, Location> evalLocations){
		this.trainUsers = trainUsers;
		this.trainLocations = trainLocations;
		this.evalUsers = evalUsers;
		this.evalLocations = evalLocations;
	}

	public void run(int outputSize){
		WekaDataReader wekaReader = new WekaDataReader(dataset,logger);
//		Instances instances = wekaReader.getWekaCheckIns(trainUsers, trainLocations, socialWeight, gaussScale, endOfTraining);
//		Instances instances = wekaReader.getWekaFromMatrix(trainUsers, trainLocations, socialWeight, gaussScale, endOfTraining);
//		Instances instances = wekaReader.getWekaFromPartialMatrix(trainUsers, trainLocations, socialWeight, gaussScale, endOfTraining);
		Instances instances = wekaReader.getWekaFromRecent(trainUsers, trainLocations, socialWeight, gaussScale, endOfTraining);
		RandomForest forestClassifier = new RandomForest();
		String[] options;
		Evaluation eTest = null;
		try {
//			String optionsString = "-I 100 -K 0 -S 1 -num-slots 1 -num-decimal-places 3";
//			String optionsString = "-I 100 -K 0 -S 1 -num-slots 1";
			int minTrees = 100;
			int desiredTrees = trainUsers.size();
			desiredTrees = trainLocations.size();
			int trees = Math.max(minTrees, desiredTrees);
			trees = 100;
			String optionsString = "-I " + trees + " -K 0 -S 1 -num-slots 1";
			options = Utils.splitOptions(optionsString);
			//1000 is too many -gc error
			forestClassifier.setOptions(options);
			forestClassifier.buildClassifier(instances);

			eTest = new Evaluation(instances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		evaluateWeka(eTest, forestClassifier, instances, outputSize);
	}

	private void evaluateWeka(Evaluation evaluator, Classifier classifier, Instances instances, int outputSize){
		float meanPrecision = 0.0f;
		float userCoverage = 0.0f;
		int totalValid = 0;
		int validUsers = 0;

		int totalFound = 0;

		double status = trainUsers.size();
		logger.log("Status Value:" + status);
		logger.log("Weight:" + 1/(1 + Math.exp(-PREDICTION_WEIGHT_FACTOR*(status-PREDICTION_WEIGHT_THRESHOLD))));
		
		for(Integer id : evalUsers.keySet()) {
			if(trainUsers.get(id) != null) {

				User current = trainUsers.get(id);
				if(dataset.getCompetitor() == COMPETITOR.LURA){
					if(!Recommender.satisfiesLURAactivity(current,endOfTraining)){
						continue;
					}
				}
				
				HashMap<String, Float> topItems = new HashMap<String, Float>();
				HashMap<String, Float> topBaseItems = new HashMap<String, Float>();
				HashMap<String, Float> topFriendItems = new HashMap<String, Float>();
				HashMap<String, Float> startItems = new HashMap<String, Float>();

				topItems = PredictionHelper.topNpredictions(current, 3*outputSize, novel);
				topFriendItems = PredictionHelper.topNFriendPredictions(current, 3*outputSize, novel);
				
				startItems.putAll(topFriendItems);
				startItems.putAll(topBaseItems);
				
				for(String locationId: startItems.keySet()){
					
					Location location = current.getLocations().get(locationId);
					boolean visited = (evalUsers.get(id).getCheckinsForLocation(locationId) != null);
					
					Instance instance = WekaInstanceHelper.createInstance(current, location,
							dataset.getStartDate(), endOfTraining, visited, socialWeight, gaussScale);
					
					instance.setDataset(instances);
					Float prediction = 0f;
					try {
						prediction = (float) evaluator.evaluateModelOnce(classifier, instance);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(0);
					}
					
					Float basePrediction = topBaseItems.get(locationId);
					if(basePrediction == null)
						basePrediction = 0f;
					
					prediction = adjustPrediction(basePrediction, prediction, status);
					
					//should be unnecessary
//					if(novel && !visited){
						topItems.put(locationId, prediction);
//					}
					
				}
				
				topItems = MapHelper.getTopItems(topItems, outputSize);
				
				int validVisits = 0;
				int found = 0;
				Set<String> oldKeys = current.getCheckIns().keySet();
				for(String item : evalUsers.get(id).getCheckIns().keySet()){
					if(novel && oldKeys.contains(item)){
						continue;
					}
					validVisits++;
					totalValid++;
					
					if(topItems.containsKey(item)){
						totalFound++;
						found++;
					}
				}
				
				if(validVisits != 0){
					meanPrecision += new Float(found) / (outputSize);
					validUsers++;
					if(!topItems.isEmpty() ){
						userCoverage++;
					}
				}
				
			}
		}
		meanPrecision /= new Float(validUsers);
		double meanRecall = new Float(totalFound)/totalValid;
		userCoverage /= new Float(validUsers);

		logger.log("Weka Results");
		logger.log("Valid User count: " + validUsers);
		logger.log("Total valid items occurred: " + totalValid);
		logger.log("Total relevant items recommended: " + totalFound);
		logger.log("Precision: " + meanPrecision);
		logger.log("Recall: " + meanRecall);
		logger.log("User Coverage: " + userCoverage);
	}
	
	public Float adjustPrediction(double basePrediction, double wekaPrediction, double status){
		double weight = 1/(1 + Math.exp(-PREDICTION_WEIGHT_FACTOR*(status-PREDICTION_WEIGHT_THRESHOLD)));
		return (float) (weight*basePrediction + (1-weight)*wekaPrediction);
	}
}
