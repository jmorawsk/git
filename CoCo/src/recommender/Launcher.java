package recommender;

import helpers.Dataset;
import helpers.Dataset.COMPETITOR;
import helpers.Dataset.DATASETS;
import helpers.Logger;
import weka.WekaManager;

public class Launcher {

	//First arg is setup (competitor), second is complexity (0 = base, 1 = param explore)
	public static void main(String[] args) {


		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.LFBCA);
//		Logger logger = new Logger("NAME");
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		float socialWeight = 0.75f;
		Double gaussScale = 0.5;
		int testMonths = 2;
		//		recommender = new Recommender(logger, dataset, neighborhoodSize, outputSize, 
		//				socialWeight, gaussScale, novel);
		//		recommender.runByMonth(9, testMonths);

		int firstArg = -1;
		if (args.length > 0) {
			try {
				firstArg = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Argument" + args[0] + " must be an integer.");
				System.exit(1);
			}
		}
		int secondArg = -1;
		if (args.length > 1) {
			try {
				secondArg = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.err.println("Argument" + args[1] + " must be an integer.");
				System.exit(1);
			}
		}
		switch(secondArg){
		case 1:
			for(socialWeight = 0f; socialWeight <= 1f; socialWeight += 0.05f){
				switch(firstArg){
				case 0:
					fullLFBCAtest(socialWeight, gaussScale);
					break;
				case 1:
					fullLURAtestFoursquare(socialWeight, gaussScale);
					break;
				case 2:
					fullLURAtestGowalla(socialWeight, gaussScale);
					break;
				case 3:
					float testPercentage = 0.4f;
					fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage);
					break;
				}
			}
			break;
		case 0:
			switch(firstArg){
			case 0:
				fullLFBCAtest(socialWeight, gaussScale);
				break;
			case 1:
				fullLURAtestFoursquare(socialWeight, gaussScale);
				break;
			case 2:
				fullLURAtestGowalla(socialWeight, gaussScale);
				break;
			case 3:
				float testPercentage = 0.2f;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage);
				testPercentage = 0.4f;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage);
				break;
			}
		}

	}

	private static void fullLFBCAtest(Float socialWeight, Double gaussScale){
		Logger logger = new Logger("LFBCATestGowalla");
		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.LFBCA);
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		int testMonths = 2;
		int trainingWindow = 1;
		int firstMonth = 3;
//		firstMonth = 12;

		for(int month =firstMonth; month<=17; month++){
//		for(int month =9; month<=17; month++){
			Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
					gaussScale, novel);
			logTime(logger);
			recommender.runByMonth(month, testMonths, month);
			logTime(logger);
//			recommender.runByMonth(month, testMonths, trainingWindow);
			recommender.evaluate(outputSize);
			logTime(logger);
			wekaTest(logger, recommender, outputSize);
			logTime(logger);
		}
	}

	private static void fullLURAtestFoursquare(Float socialWeight, Double gaussScale){
		Logger logger = new Logger("LURATestFourSquare");
		Dataset dataset = new Dataset(DATASETS.fourSquare, COMPETITOR.LURA);
		int neighborhoodSize = 30;
		boolean novel = true;
		int testDays = 60;
		int trainingDays = 300;

		//		int outputSize = 5;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel);
		recommender.runByDay(trainingDays, testDays, trainingDays);
		for(int outputSize: new int[] {5,10,15,20,25}){
			recommender.evaluate(outputSize);
			wekaTest(logger, recommender, outputSize);
		}
	}

	private static void fullLURAtestGowalla(Float socialWeight, Double gaussScale){
		Logger logger = new Logger("LURATestGowalla");
		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.LURA);
		int neighborhoodSize = 30;
		boolean novel = true;
		int testDays = 60;
		int trainingDays = 420;

		//		int outputSize = 5;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel);
		recommender.runByDay(trainingDays, testDays, trainingDays);
		for(int outputSize: new int[] {5,10,15,20,25}){
			recommender.evaluate(outputSize);
			wekaTest(logger, recommender, outputSize);
		}
	}
	
	private static void fullLRTtestFoursquare(Float socialWeight, Double gaussScale, float testPercentage){
		Logger logger = new Logger("LRTTestFoursquare");
		Dataset dataset = new Dataset(DATASETS.fourSquare, COMPETITOR.LRT);
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		for(int i = 0; i < 5; i++){
			Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel);
			recommender.runByLocationPercentage(testPercentage);
			recommender.evaluate(outputSize);
		}
	}

	private static void wekaTest(Logger logger, Recommender recommender, int outputSize){
		WekaManager wekaMan = new WekaManager(logger, recommender);
		wekaMan.run(outputSize);
	}
	
	private static void logTime(Logger logger){
		logger.log("" + System.currentTimeMillis());
	}
}
