package helpers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.Location;
import data.User;
import helpers.Dataset.DATASETS;
import helpers.Dataset.PERCENTAGE_ITEM;


public class DatasetReader {

	private Dataset dataset;

	
	public DatasetReader(Dataset dataset){
		this.dataset = dataset;
	}

	public FileReader getCheckinFileReader(){
		FileReader reader = null;
		try {
			reader = new FileReader(dataset.getCheckinFilename());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reader;
	}

	private FileReader getFriendshipFileReader(){
		FileReader reader = null;
		try {
			reader = new FileReader(dataset.getFriendshipFilename());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reader;
	}

	public HashMap<Integer, HashMap<Integer, Boolean>> getFriendshipMap(){

		HashMap<Integer, HashMap<Integer, Boolean>> friendshipMap = new HashMap<Integer, HashMap<Integer, Boolean>>();

		BufferedReader fReader = new BufferedReader(getFriendshipFileReader());
		Pattern pattern = dataset.getFriendshipPattern();
		String line;

		try {
			if(dataset.getBaseDataset() == DATASETS.fourSquare){
				//skipping header line
				String headerLine = fReader.readLine();
			}

			while ((line = fReader.readLine()) != null) {
				Matcher m = pattern.matcher(line);
				m.find();
				Integer id1 = new Integer(m.group(1));
				Integer id2 = new Integer(m.group(2));

				if(!friendshipMap.containsKey(id1))
					friendshipMap.put(id1, new HashMap<Integer, Boolean>());
				if(!friendshipMap.containsKey(id2))
					friendshipMap.put(id2, new HashMap<Integer, Boolean>());

				friendshipMap.get(id1).put(id2, true);
				friendshipMap.get(id2).put(id1, true);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return friendshipMap;
	}

	//Use either dates or test percentage
	public void getCheckIns(HashMap<Integer, User> users, HashMap<String, Location> locations, 
			HashMap<Integer, User> trainUsers, HashMap<String, Location> trainLocations,
			HashMap<Integer, User> evalUsers, HashMap<String, Location> evalLocations,
			Date startOfTraining, Date endOfTraining, Date endOfTesting, Float testPercentage){

		Random random = new Random();
		BufferedReader fReader = new BufferedReader(getCheckinFileReader());
		Pattern pattern = dataset.getCheckinPattern();
		String line;
		try {
			if(dataset.getBaseDataset() == DATASETS.fourSquare){
				//skipping header line
				String headerLine = fReader.readLine();
			}

			while ((line = fReader.readLine()) != null) {

				int count = 0;
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
				date = parseDate(timeString);

				addUserAndLocation(id, users, locationId, locations, longitude, latitude, date);

				if(testPercentage == null){
					if(startOfTraining != null && date.after(startOfTraining)){

						if(date.before(endOfTraining)){
							addUserAndLocation(id, trainUsers, locationId, trainLocations, latitude, longitude, date);
						} else if(date.before(endOfTesting)){
							addUserAndLocation(id, evalUsers, locationId, evalLocations, latitude, longitude, date);
						}
					}
				} else {
					
					if (dataset.getPercentItem().equals(PERCENTAGE_ITEM.location)){
						//This handles splitting dataset by random percentage, testing on percent of locations per user
						//Training gets all checkins if location is in training for user
						if(trainUsers.get(id) != null && trainUsers.get(id).getCheckinsForLocation(locationId)!=null){
							addUserAndLocation(id, trainUsers, locationId, trainLocations, latitude, longitude, date);
						}
						//Testing gets all checkins if location is in testing for user
						else if(evalUsers.get(id) != null && evalUsers.get(id).getCheckinsForLocation(locationId)!=null){
							addUserAndLocation(id, evalUsers, locationId, evalLocations, latitude, longitude, date);
						}
						//Random assignment to train/test
						else if(random.nextFloat()>testPercentage){
							addUserAndLocation(id, trainUsers, locationId, trainLocations, latitude, longitude, date);
						} else{
							addUserAndLocation(id, evalUsers, locationId, evalLocations, latitude, longitude, date);
						}
					}
					if (dataset.getPercentItem().equals(PERCENTAGE_ITEM.time)){
						float totalCheckins = 5310346f;
						//only correct for CEPR gowalla dataset
						count++;
						float trainPercent = 1 - testPercentage;
						if(count/totalCheckins < trainPercent){
							addUserAndLocation(id, trainUsers, locationId, trainLocations, latitude, longitude, date);
						} else{
							addUserAndLocation(id, evalUsers, locationId, evalLocations, latitude, longitude, date);
						}
					}
					

				}


			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addUserAndLocation(Integer id, HashMap<Integer, User> users,
			String locationId, HashMap<String, Location> locations, Float latitude, Float longitude, Date date){
		User user = getUserInMap(id,users);
		user.addCheckIn(locationId, date);

		Location location = getLocationInMap(locationId, locations, latitude, longitude);
		location.addCheckIn(id, date);
	}

	private User getUserInMap(Integer id, HashMap<Integer, User> userMap){
		User user = userMap.get(id);
		if(user == null) {
			user = new User(id);
			userMap.put(id, user);
		}
		return user;
	}

	private Location getLocationInMap(String id, HashMap<String, Location> locationMap, Float latitude, Float longitude){
		Location location = locationMap.get(id);
		if(location == null) {
			location = new Location(id);
			location.setCoords(latitude, longitude);
			locationMap.put(id, location);
		}
		return location;
	}

	public Date parseDate(String timeString){
		String gap = "T";
		if(dataset.getBaseDataset() == DATASETS.fourSquare){
			gap = " ";
		}
		String[] dateParse = timeString.split(gap)[0].split("-");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, new Integer(dateParse[0]));
		Integer month = new Integer(dateParse[1]) - 1;
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, new Integer(dateParse[2]));
		String[] timeParse = timeString.split(gap)[1].split(":");
		Integer hour = new Integer(timeParse[0]);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		Integer minute = new Integer(timeParse[1]);
		cal.set(Calendar.MINUTE, minute);
		if(dataset.getBaseDataset() != DATASETS.fourSquare){
			timeParse[2] = timeParse[2].substring(0, timeParse[2].length()-1);
		}
		Integer second = new Integer(timeParse[2]);
		cal.set(Calendar.SECOND, second);
		Date date = cal.getTime();
		return date;
	}
}
