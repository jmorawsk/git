package weka;

import java.util.ArrayList;
import java.util.Date;

import data.Location;
import data.User;
import helpers.PredictionHelper;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class WekaInstanceHelper {
	public static int NUMBER_OF_ATTRIBUTES = 11;
	
	public static Instance createInstance(User user, Location location,
			Date startDate, Date date, boolean visited,
			float socialWeight, Double gaussScale){
		
		Instance instance = new DenseInstance(NUMBER_OF_ATTRIBUTES);
		
		int userId = user.getId();
		int friends = user.getFriendCount();
		int differentLocations = user.getCheckIns().size();
	    double distance = location.getDistance(user.getLocations().get(user.getPreviousCheckInLocation()));
		int visitors = location.getCheckIns().keySet().size();
		float socialPrediction = PredictionHelper.socialPrediction(user, location, socialWeight);
		float spatialNearPrediction = PredictionHelper.spatialPredictionNear(user, location, gaussScale);
		float spatialFarPrediction = PredictionHelper.spatialPredictionFar(user, location, gaussScale);
		float temporalPrediction = PredictionHelper.temporalPredictionGeneral(user, location, startDate, date);
		float combinedPrediction = PredictionHelper.predict(user, location);
		float actual = visited ? 1f : 0f;
		
		int i = 0;
		instance.setValue(i++, userId);
		instance.setValue(i++, friends);
		instance.setValue(i++, differentLocations);
		instance.setValue(i++, distance);
		instance.setValue(i++, visitors);
		instance.setValue(i++, socialPrediction);
		instance.setValue(i++, spatialNearPrediction);
		instance.setValue(i++, spatialFarPrediction);
		instance.setValue(i++, temporalPrediction);
		instance.setValue(i++, combinedPrediction);
		instance.setValue(i++, actual);
		
		return instance;
	}

	public static ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("userId"));
		attributes.add(new Attribute("friends"));
		attributes.add(new Attribute("differentLocations"));
		attributes.add(new Attribute("distance"));
		attributes.add(new Attribute("visitors"));
		attributes.add(new Attribute("socialPred"));
		attributes.add(new Attribute("spatialNear"));
		attributes.add(new Attribute("spatialFar"));
		attributes.add(new Attribute("temporalPred"));
		attributes.add(new Attribute("comboPred"));
		attributes.add(new Attribute("actual"));
		return attributes;
	}
	
}
