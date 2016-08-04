package weka;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import data.Location;
import data.Pair;
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
	private int samplingTimeWindow;
	private boolean novel;
	private Date endOfTraining;
//	private double PREDICTION_WEIGHT_THRESHOLD = 2854.3; //Maybe 11.5 for average checkins
//	private double PREDICTION_WEIGHT_FACTOR = 0.01; //Maybe 10 for average checkins
	private double PREDICTION_WEIGHT_THRESHOLD = 11.5; //
	private double PREDICTION_WEIGHT_FACTOR = 10; //

	public WekaManager(Logger logger, Recommender recommender){
		this(recommender.getDataset(), recommender.getSocialWeight(), recommender.getGaussScale(), 
				recommender.isNovel(), recommender.getSamplingChoice());
		this.logger = logger;
		this.endOfTraining = recommender.getEndOfTraining();
		this.assignMappings(recommender.getTrainingUsers(), recommender.getTrainingLocations(),
				recommender.getEvalUsers(), recommender.getEvalLocations());
	}
	
	private WekaManager(Dataset dataset, float socialWeight,Double gaussScale, boolean novel, int samplingChoice){
		this.dataset = dataset;
		this.socialWeight = socialWeight;
		this.gaussScale = gaussScale;
		this.novel = novel;
		this.samplingTimeWindow = samplingChoice;
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
//		Instances instances = wekaReader.getWekaUndersample(trainUsers, trainLocations, socialWeight, gaussScale, endOfTraining, 1);
		
		long startTime = System.currentTimeMillis();
		logger.log("Start weka instances at:" + startTime);
		Instances instances = wekaReader.getWekaInstances(trainUsers, trainLocations,
				socialWeight, gaussScale, endOfTraining, samplingTimeWindow);
		long duration = (System.currentTimeMillis() - startTime);
		logger.log("Weka instances took:" + duration);
		
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
		float utility = 0.0f;
		
		float meanAveragePrecision = 0.0f;
		int totalValid = 0;
		int validUsers = 0;
		int totalFound = 0;

		double status = trainUsers.size(); //Maybe could change to locations/user
		status = trainLocations.size()/trainUsers.size();
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

//				topItems = PredictionHelper.topNpredictions(current, 3*outputSize, novel);
				topBaseItems = PredictionHelper.topNpredictions(current, 3*outputSize, novel);
				topFriendItems = PredictionHelper.topNFriendPredictions(current, 3*outputSize, novel);
				
				startItems.putAll(topFriendItems);
				startItems.putAll(topBaseItems);
//				topItems.putAll(topFriendItems);
				
				for(String locationId: startItems.keySet()){
//				for(String locationId: topItems.keySet()){
					
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

				Vector<Pair> topPairs = new Vector<Pair>();
				for(String item : topItems.keySet()) {
					topPairs.add(new Pair(item, topItems.get(item)));
				}
				topPairs.sort(new Comparator<Pair>(){
					@Override
					public int compare(Pair arg0, Pair arg1) {
						// TODO Auto-generated method stub
						return Float.compare(arg1.value, arg0.value);
					}
					
				});
				int validVisits = 0;
				int found = 0;
				Set<String> oldKeys = current.getCheckIns().keySet();
				float avgPrecision = 0.0f;
				for(String item : evalUsers.get(id).getCheckIns().keySet()){
					if(novel && oldKeys.contains(item)){
						continue;
					}
					validVisits++;
					totalValid++;
					
					for(int position = 0; position < topPairs.size(); position++){
						String recommendation = topPairs.get(position).id;
						if(recommendation.equals(item)){
							totalFound++;
							found++;
							
							avgPrecision += new Float(found) / new Float(position+1);
						}
					}
				}
				if(found==0)
					avgPrecision = 0;
				else 
					avgPrecision /= new Float(found);

				meanAveragePrecision += avgPrecision;
				
				if(validVisits != 0){
					meanPrecision += new Float(found) / (outputSize);
					validUsers++;
					if(!topItems.isEmpty() ){
						userCoverage++;
					}
					if(found != 0){
						utility++;
					}
				}
				
			}
		}
		meanAveragePrecision /= new Float(validUsers);
		meanPrecision /= new Float(validUsers);
		double meanRecall = new Float(totalFound)/totalValid;
		userCoverage /= new Float(validUsers);
		utility /= new Float(validUsers);

		logger.log("Weka Results");
		logger.log("Valid User count: " + validUsers);
		logger.log("Total valid items occurred: " + totalValid);
		logger.log("Total relevant items recommended: " + totalFound);
		logger.log("Precision: " + meanPrecision);
		logger.log("Recall: " + meanRecall);
		logger.log("User Coverage: " + userCoverage);
		logger.log("Utility: " + utility);
		logger.log("MAP: " + meanAveragePrecision);
	}
	
	public Float adjustPrediction(double basePrediction, double wekaPrediction, double status){
		double weight = 1/(1 + Math.exp(-PREDICTION_WEIGHT_FACTOR*(status-PREDICTION_WEIGHT_THRESHOLD)));
		weight = 0;
		return (float) (weight*basePrediction + (1-weight)*wekaPrediction);
	}
}
