package weka;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import data.Location;
import data.User;
import helpers.Dataset;
import helpers.DatasetReader;
import helpers.Logger;
import helpers.PredictionHelper;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import helpers.Dataset.DATASETS;

public class WekaDataReader {
	private Dataset dataset;
	private DatasetReader datasetReader;
	private Logger logger;

	public WekaDataReader(Dataset dataset, Logger logger){
		this.dataset = dataset;
		this.datasetReader = new DatasetReader(dataset);
		this.logger = logger;
	}

	public void prepareWekaFile(){

	}

	public Instances getWekaCheckIns(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			float socialWeight, Double gaussScale, Date endOfTraining){

		
		logger.log("Train Users:" + trainUsers.size());
		logger.log("Train Locations:" + trainLocations.size());


		ArrayList<Attribute> attributes = WekaInstanceHelper.getAttributes();
		Instances isTrainingSet = new Instances("Relation", attributes, 10);
		isTrainingSet.setClassIndex(isTrainingSet.numAttributes() - 1);

		BufferedReader fReader = new BufferedReader(datasetReader.getCheckinFileReader());
		Pattern pattern = dataset.getCheckinPattern();
		String line;
		try {
			if(dataset.getBaseDataset() == DATASETS.fourSquare){
				//skipping header line
				String headerLine = fReader.readLine();
			}

			while ((line = fReader.readLine()) != null) {

				Matcher m = pattern.matcher(line);
				m.find();

				Integer id = 0;
				Float latitude = 0f, longitude = 0f;
				String timeString = "";
				Date date = new Date();
				String locationId = "";

				id = new Integer(m.group(1));
				locationId = m.group(5);

				if(dataset.getBaseDataset() == DATASETS.fourSquare) {
					latitude = new Float(m.group(2));
					longitude = new Float(m.group(3));
					timeString = m.group(4);
				} else {
					latitude = new Float(m.group(3));
					longitude = new Float(m.group(4));
					timeString = m.group(2);
				}
				date = datasetReader.parseDate(timeString);

				if(date.before(endOfTraining)){
					User trainUser = trainUsers.get(id);
					Location trainLocation = trainLocations.get(locationId);


					boolean visited = true;
					Instance instance = WekaInstanceHelper.createInstance(trainUser, trainLocation,
							dataset.getStartDate(), date, visited , socialWeight, gaussScale);
					isTrainingSet.add(instance);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isTrainingSet;

	}
	
	public Instances getWekaInstances(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			float socialWeight, Double gaussScale, Date endOfTraining, int choice){
		/*Choices (old)
		 * 0 full matrix (Base) //way too long
		 * 1 full recent (1 month) //way too long
		 * 2 undersample full //too long
		 * 3 undersample recent (1 month) //too long
		 * 4 undersample majority, all
		 * 5 undersample majority, recent (1 month)
		 * 
		 * now just filters by days
		 */
		
		
		return getWekaUndersample(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,choice);
		
//		switch(choice){
////		case 0: return getWekaFromMatrix(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,0,1.0f);
////		case 1: return getWekaFromMatrix(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,1,1.0f);
////		case 2: return getWekaFromMatrix(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,0,0.1f);
////		case 3: return getWekaFromMatrix(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,1,0.1f);
//		case 0: return getWekaUndersample(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,0);
//		case -1: return getWekaUndersample(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,0);
//		
//		
//		default: return getWekaUndersample(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,choice);
////		default: return getWekaFromMatrix(trainUsers, trainLocations,socialWeight, gaussScale, endOfTraining,0,1.0f);
//		}
	
		
	}
	
	
	public Instances getWekaFromMatrix(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			float socialWeight, Double gaussScale, Date endOfTraining, int trainWindow, float sampling){

		Random random = new Random(123);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(endOfTraining);
		calendar.add(Calendar.MONTH, -trainWindow);
		Date startOfTraining = calendar.getTime();
		
		logger.log("Train Users:" + trainUsers.size());
		logger.log("Train Locations:" + trainLocations.size());

		ArrayList<Attribute> attributes = WekaInstanceHelper.getAttributes();

		Instances isTrainingSet = new Instances("Relation", attributes, 10);
		isTrainingSet.setClassIndex(isTrainingSet.numAttributes() - 1);
		
		for(Integer userId: trainUsers.keySet()){

			User trainUser = trainUsers.get(userId);
			for(String locationId: trainLocations.keySet()){
				if(random.nextFloat() < sampling){
					continue;
				}
				
				Location trainLocation = trainLocations.get(locationId);
				
				ArrayList<Date> visits = trainUser.getCheckinsForLocation(locationId);
				
				boolean visited = (visits != null);
				if(visited){
					Date last = visits.get(visits.size() - 1);
					Date first  = visits.get(0);
					if(trainWindow>0 && last.before(startOfTraining) && first.before(startOfTraining)){
						visited = false;
					}
				}
				Date date = !visited ? endOfTraining : visits.get(0);

				Instance instance = WekaInstanceHelper.createInstance(trainUser, trainLocation,
						dataset.getStartDate(), date, visited, socialWeight, gaussScale);
				isTrainingSet.add(instance);
			}
		}
		
		return isTrainingSet;
	}
	
	public Instances getWekaFromPartialMatrix(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			float socialWeight, Double gaussScale, Date endOfTraining, int trainWindow){

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(endOfTraining);
		calendar.add(Calendar.MONTH, -trainWindow);
		Date startOfTraining = calendar.getTime();
		
		logger.log("Train Users:" + trainUsers.size());
		logger.log("Train Locations:" + trainLocations.size());


		ArrayList<Attribute> attributes = WekaInstanceHelper.getAttributes();
		Instances isTrainingSet = new Instances("Relation", attributes, 10);
		isTrainingSet.setClassIndex(isTrainingSet.numAttributes() - 1);
		
		String[] allLocations = trainLocations.keySet().toArray(new String[0]);
		Random random = new Random();
		
		for(Integer userId: trainUsers.keySet()){

			User trainUser = trainUsers.get(userId);
			ArrayList<String> locationIds = new ArrayList<String>();
			locationIds.addAll(trainUser.getCheckIns().keySet());
			
			int originalLocations = locationIds.size();
			for(int i = 0; i< originalLocations; i++){
				locationIds.add(allLocations[random.nextInt(allLocations.length)]);
			}
			
			for(String locationId: locationIds){
				Location trainLocation = trainLocations.get(locationId);
				
				ArrayList<Date> visits = trainUser.getCheckinsForLocation(locationId);
				
				boolean visited = (visits != null);
				if(visited){
					Date last = visits.get(visits.size() - 1);
					Date first  = visits.get(0);
					if(trainWindow>0 && last.before(startOfTraining) && first.before(startOfTraining)){
						visited = false;
					}
				}
				Date date = !visited ? endOfTraining : visits.get(0);

				Instance instance = WekaInstanceHelper.createInstance(trainUser, trainLocation,
						dataset.getStartDate(), date, visited, socialWeight, gaussScale);
				isTrainingSet.add(instance);
			}
		}
		return isTrainingSet;
	}
	
	public Instances getWekaUndersample(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			float socialWeight, Double gaussScale, Date endOfTraining, int trainWindow){

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(endOfTraining);
//		calendar.add(Calendar.MONTH, -trainWindow);
		calendar.add(Calendar.DAY_OF_YEAR, -trainWindow);
		Date startOfTraining = calendar.getTime();
		

		logger.log("Training window (days):" + trainWindow);
		logger.log("Train Users:" + trainUsers.size());
		logger.log("Train Locations:" + trainLocations.size());

		ArrayList<Attribute> attributes = WekaInstanceHelper.getAttributes();
		Instances isTrainingSet = new Instances("Relation", attributes, 10);
		isTrainingSet.setClassIndex(isTrainingSet.numAttributes() - 1);
		
		String[] allLocations = trainLocations.keySet().toArray(new String[0]);
		Random random = new Random();
		
		for(Integer userId: trainUsers.keySet()){

			User trainUser = trainUsers.get(userId);
			ArrayList<String> locationIds = new ArrayList<String>();
			locationIds.addAll(trainUser.getCheckIns().keySet());
			
			int originalLocations = locationIds.size();
			for(int i = 0; i< originalLocations; i++){
				locationIds.add(allLocations[random.nextInt(allLocations.length)]);
			}
			
			for(String locationId: locationIds){
				Location trainLocation = trainLocations.get(locationId);
				
				ArrayList<Date> visits = trainUser.getCheckinsForLocation(locationId);
				
				boolean visited = (visits != null);
				Date date = endOfTraining;
				if(visited){
					Date last = visits.get(visits.size() - 1);
					Date first  = visits.get(0);
					Date lastVisit = first.before(last) ? last : first;
//					if(trainWindow>0 && last.before(startOfTraining) && first.before(startOfTraining)){
					if(trainWindow>0 && lastVisit.before(startOfTraining)){
						continue;
					}
				}
//				Date date = !visited ? endOfTraining : visits.get(0);

				Instance instance = WekaInstanceHelper.createInstance(trainUser, trainLocation,
						dataset.getStartDate(), date, visited, socialWeight, gaussScale);
				isTrainingSet.add(instance);
			}
		}
		return isTrainingSet;
	}

	public void trainWeka(HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			HashMap<Integer, User> evalUsers, HashMap<String, Location> evalLocations){

	}
}
