package recommender;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import data.Location;
import data.User;
import helpers.Logger;
import helpers.PredictionHelper;
import helpers.ReferenceHelper;
import helpers.UserAnalyzer;
import helpers.Dataset;
import helpers.Dataset.COMPETITOR;
import helpers.DatasetReader;

public class Recommender {
	private Logger logger;
	private Dataset dataset;
	private DatasetReader datasetReader;
	private boolean novel;
	private boolean crossval = false;

	private final int THREAD_COUNT = 4;
	private int neighborhoodSize;
//	private int outputSize =10;

	//	public Boolean timeWeight = false;
	private Float socialWeight;
	private Double gaussScale;
	private int samplingChoice;

	private Date endOfTraining;
	private Date endOfTesting;
	private Date endOfSkipping = null;
	private Float testPercentage =null;



	private HashMap<Integer, User> users;
	private HashMap<String, Location> locations;
	private HashMap<Integer, HashMap<Integer, Boolean>> friendshipMap;
	private HashMap<Integer, User> trainingUsers;
	private HashMap<String, Location> trainingLocations;
	private HashMap<Integer, User> evalUsers;
	private HashMap<String, Location> evalLocations;

	public Recommender(Logger logger, Dataset dataset, int neighborhoodSize, Float socialWeight,
			Double gaussScale, boolean novel, int samplingChoice){
		this.logger = logger;
		this.setDataset(dataset);
		datasetReader = new DatasetReader(dataset);
		this.neighborhoodSize = neighborhoodSize;
		this.setSocialWeight(socialWeight);
		this.setGaussScale(gaussScale);
		this.setNovel(novel);
		this.setSamplingChoice(samplingChoice);

		users = new HashMap<Integer, User>();
		locations = new HashMap<String, Location>();
		friendshipMap = new HashMap<Integer, HashMap<Integer, Boolean>>();
		setTrainingUsers(new HashMap<Integer, User>());
		setTrainingLocations(new HashMap<String, Location>());
		setEvalUsers(new HashMap<Integer, User>());
		setEvalLocations(new HashMap<String, Location>());
	}

	public void runByDay(int endTrainDay, int testDays, int actualTrainDays){
		startRunLog();
		Calendar cutoff = Calendar.getInstance();
		cutoff.setTime(getDataset().getStartDate());
		cutoff.add(Calendar.DAY_OF_YEAR, endTrainDay);
		setEndOfTraining(cutoff.getTime());
		cutoff.add(Calendar.DAY_OF_YEAR, testDays);
		endOfTesting = cutoff.getTime();
		cutoff.add(Calendar.DAY_OF_YEAR, -testDays);
		cutoff.add(Calendar.DAY_OF_YEAR, -actualTrainDays);
		endOfSkipping = cutoff.getTime();
		log("Training days: " + endTrainDay);
		log("Testing days: " + testDays);
		log("Train Window: " + actualTrainDays);
		runTraining();
	}

	public void runByMonth(int endTrainMonth, int testMonths, int actualTrainMonth){
		startRunLog();
		Calendar cutoff = Calendar.getInstance();
		cutoff.setTime(getDataset().getStartDate());
		cutoff.add(Calendar.MONTH, endTrainMonth);
		setEndOfTraining(cutoff.getTime());
		cutoff.add(Calendar.MONTH, testMonths);
		endOfTesting = cutoff.getTime();
		cutoff.add(Calendar.MONTH, -testMonths);
		cutoff.add(Calendar.MONTH, -actualTrainMonth);
		endOfSkipping = cutoff.getTime();
		log("Training months: " + endTrainMonth);
		log("Testing months: " + testMonths);
		log("Train Window: " + actualTrainMonth);
		runTraining();
	}

	public void runByLocationPercentage(float testPercentage) {
		startRunLog();
		this.testPercentage = testPercentage;
		setEndOfTraining(getDataset().getEndDate());
		endOfTesting = getDataset().getEndDate();
		log("Testing Fraction: " + testPercentage);
		runTraining();
	}
	

	public void runByPercentage(float testPercentage) {
		// TODO Auto-generated method stub
		startRunLog();
		this.testPercentage = testPercentage;
		setEndOfTraining(getDataset().getEndDate());
		endOfTesting = getDataset().getEndDate();
		log("Testing Fraction: " + testPercentage);
		runTraining();
	}

	private void startRunLog(){
		log("Dataset: " + getDataset().toString() + 
				//				" timeWeight: " + timeWeight.toString() + 
				" socialWeight: " +getSocialWeight().toString() +
				//				" distRestrict: " + distRestrict.toString() +
				" gaussScale: " + getGaussScale().toString());

		log("Neighborhood: " + new Integer(neighborhoodSize).toString());
	}
	

	private void runTraining(){
		System.out.println("Loading Dataset");
		loadDataset();

		System.out.println("Analyzing Users");
		analyzeUsers();

		System.out.println("Generating Neighborhoods");
		generateNeighborhoods();
	}
	
	private void loadDataset(){
		datasetReader.getCheckIns(users, locations, getTrainingUsers(), getTrainingLocations(),
				getEvalUsers(), getEvalLocations(), endOfSkipping, getEndOfTraining(), endOfTesting, testPercentage);
		friendshipMap = datasetReader.getFriendshipMap();
		ReferenceHelper.setLocationReferences(locations, users);
		ReferenceHelper.setUserReferences(locations, users, friendshipMap);
		ReferenceHelper.setLocationReferences(getTrainingLocations(), getTrainingUsers());
		ReferenceHelper.setUserReferences(getTrainingLocations(), getTrainingUsers(), friendshipMap);
		ReferenceHelper.setLocationReferences(getEvalLocations(), getEvalUsers());
		ReferenceHelper.setUserReferences(getEvalLocations(), getEvalUsers(), friendshipMap);
	}
	
	private void analyzeUsers(){
		double close = 0;
		double far = 0;
		int closeFails = 0;
		int farFails = 0;
		for(User user : getTrainingUsers().values()) {
			UserAnalyzer.analyzeDistances(user);
			UserAnalyzer.analyzeFriendshipCovisits(user);
			UserAnalyzer.calculateCheckInWeights(user, getDataset().getStartDate(), getEndOfTraining());

			Double closeValue = user.getThresholdDistanceMean();
			if(closeValue.isNaN()){
				closeFails++;
			} else {
				close += closeValue;
			}
			Double farValue = user.getFarThresholdDistanceMean();
			if(farValue.isNaN()){
				farFails++;
			}
			else {
				far += farValue;
			}
		}
		close /= (getTrainingUsers().size() - closeFails);
		far /= (getTrainingUsers().size() - farFails);
		
		for(User user : getTrainingUsers().values()) {
			if(user.getThresholdDistanceMean().isNaN())
				user.setThresholdDistanceMean(close);
			if(user.getFarThresholdDistanceMean().isNaN())
				user.setFarThresholdDistanceMean(far);
			if(user.getThresholdDistanceMedian().isNaN())
				user.setThresholdDistanceMedian(close);
			if(user.getFarThresholdDistanceMedian().isNaN())
				user.setFarThresholdDistanceMedian(far);
		}
	}
	
	private void generateNeighborhoods(){
		Vector<Vector<Integer>> allTargets = new Vector<Vector<Integer>>();
		Vector<NeighborhoodGenerator> nGens = new Vector<NeighborhoodGenerator>();
		
		int i =0;
		for(i=0; i<THREAD_COUNT; i++){
			allTargets.add(new Vector<Integer>());   
		}
		
		i = 0;
		for(Integer id : getTrainingUsers().keySet()) {
			allTargets.get(i).add(id);
			i++;
			i%=THREAD_COUNT;
		}
		
		for(i=0; i<THREAD_COUNT; i++) {
			NeighborhoodGenerator nGen;
			nGen = new NeighborhoodGenerator(allTargets.get(i), getTrainingUsers(), i,
					neighborhoodSize, getSocialWeight());
			nGens.add(nGen);
			nGen.start();
		}
		
		for(NeighborhoodGenerator nGen : nGens) {
			try {
				nGen.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void evaluate(int outputSize){

		//Evaluate needs to show results for each type of recommendation
		//Types: TOTAL Spatial Social Temporal combinations
		//Note need: to compare spatial options in combination with others 
		/*
		 * Source types:
		 * 0 - Fully Combined
		 * 1 - Spatial
		 * 2 - Temporal
		 * 3 - Social
		 */
		
		int compCount = 4;
		
		log("Output N: " + new Integer(outputSize).toString());
		System.out.println("Evaluating Performance");
		
		float[] meanPrecision = new float[compCount];
		float[] meanRecall = new float[compCount];
		float[] userCoverage = new float[compCount];
		int totalValid = 0;
		int validUsers = 0;

		int[] totalFound = new int[compCount];
		
		for(Integer id : getEvalUsers().keySet()) {
			if(getTrainingUsers().get(id) != null) {
				
				User current = getTrainingUsers().get(id);
				if(getDataset().getCompetitor() == COMPETITOR.LURA){
					if(!satisfiesLURAactivity(current,getEndOfTraining())){
						continue;
					}
				}
				
				List<HashMap<String, Float>> topItems = new ArrayList<HashMap<String, Float>>();
				
				topItems.add(PredictionHelper.topNpredictions(current, outputSize, isNovel()));
				topItems.add( PredictionHelper.topNspatialPredictions(
						current, outputSize, isNovel(), gaussScale));
				topItems.add(PredictionHelper.topNTemporalPredictionsGeneral(current, outputSize, isNovel(),
						getDataset().getStartDate(),endOfTesting));
				topItems.add(PredictionHelper.topNFriendPredictions(current, outputSize, isNovel()));
				
				int validVisits = 0;
				int[] found = new int[compCount];
				Set<String> oldKeys = current.getCheckIns().keySet();
				for(String item : getEvalUsers().get(id).getCheckIns().keySet()){
					if(isNovel() && oldKeys.contains(item)){
						continue;
					}
					validVisits++;
					totalValid++;
					
					for(int index = 0; index < compCount; index++){
						if(topItems.get(index).containsKey(item)){
							totalFound[index]++;
							found[index]++;
						}
					}
				}
				
				if(validVisits != 0){
					validUsers++;
					for(int index = 0; index < compCount; index++){
						meanPrecision[index] += new Float(found[index]) / (outputSize);
						if(!topItems.get(index).isEmpty() ){
							userCoverage[index]++;
						}
					}
					
					
				}
			}
		}
		

		log("Valid User count: " + validUsers);
		log("Total valid items occurred: " + totalValid);

		for(int index = 0; index < compCount; index++){
			meanPrecision[index] /= new Float(validUsers);
			meanRecall[index] = new Float(totalFound[index])/totalValid;
			userCoverage[index] /= new Float(validUsers);
			
			log("Results of source type: " + index);
			log("Total relevant items recommended: " + totalFound[index]);
			log("Precision: " + meanPrecision[index]);
			log("Recall: " + meanRecall[index]);
			log("User Coverage: " + userCoverage[index]);
		}
		

	}

	private void log(String message){
		logger.log(message);
	}
	
	public static boolean satisfiesLURAactivity(User user, Date endDate){
		int activityThreshold = 5;
		if(user.getCheckIns().keySet().size() >= activityThreshold){
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			int days_delta_t = 60;
			cal.add(Calendar.DATE, -days_delta_t);
			Date cutoff  = cal.getTime();
			int recentCount = 0;
			for(ArrayList<Date> dates : user.getCheckIns().values()){
				Date firstVisit = dates.get(0);
				Date lastVisit = dates.get(dates.size() -1);
				if(lastVisit.before(firstVisit))
					firstVisit = lastVisit;
				if(firstVisit.after(cutoff) && firstVisit.before(endDate)){
					recentCount++;
					if(recentCount >= activityThreshold){
						return true;
					}
				}
			}
			
		}
		return false;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Float getSocialWeight() {
		return socialWeight;
	}

	public void setSocialWeight(Float socialWeight) {
		this.socialWeight = socialWeight;
	}

	public Double getGaussScale() {
		return gaussScale;
	}

	public void setGaussScale(Double gaussScale) {
		this.gaussScale = gaussScale;
	}

	public boolean isNovel() {
		return novel;
	}

	public void setNovel(boolean novel) {
		this.novel = novel;
	}

	public HashMap<Integer, User> getTrainingUsers() {
		return trainingUsers;
	}

	public void setTrainingUsers(HashMap<Integer, User> trainingUsers) {
		this.trainingUsers = trainingUsers;
	}

	public HashMap<String, Location> getTrainingLocations() {
		return trainingLocations;
	}

	public void setTrainingLocations(HashMap<String, Location> trainingLocations) {
		this.trainingLocations = trainingLocations;
	}

	public HashMap<Integer, User> getEvalUsers() {
		return evalUsers;
	}

	public void setEvalUsers(HashMap<Integer, User> evalUsers) {
		this.evalUsers = evalUsers;
	}

	public HashMap<String, Location> getEvalLocations() {
		return evalLocations;
	}

	public void setEvalLocations(HashMap<String, Location> evalLocations) {
		this.evalLocations = evalLocations;
	}

	public Date getEndOfTraining() {
		return endOfTraining;
	}

	public void setEndOfTraining(Date endOfTraining) {
		this.endOfTraining = endOfTraining;
	}

	public int getSamplingChoice() {
		return this.samplingChoice;
	}
	
	public void setSamplingChoice(int choice) {
		this.samplingChoice = choice;
	}


	
}
