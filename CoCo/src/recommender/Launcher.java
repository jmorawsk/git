package recommender;

import helpers.Dataset;
import helpers.Dataset.COMPETITOR;
import helpers.Dataset.DATASETS;
import helpers.Logger;
import weka.WekaManager;

public class Launcher {

	//First arg is setup (competitor), second is complexity (0 = base, 1 = param explore)
	public static void main(String[] args) {
		//use negative for specific debug
		//arg 1, test set: 0 lfbcaGowalla, 1 Lura foursquare, 2 Lura gowalla, 3 LRT, 4 Brightkite
		//arg 2, testing type: 0 default (full), 1 explore param, 2 fast explore param, case 3 fast/snapshot
		//arg 3, weka time window in days



		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.LFBCA);
		//		Logger logger = new Logger("NAME");
		int neighborhoodSize = 30;
		int outputSize = 10;
		int samplingChoice = 5;
		//See wekaDatasetReader,
		/*Choices (old) - Now just filters number of days, 0 is all
		 * 0 full matrix (Base) //way too long
		 * 1 full recent (1 month) //way too long
		 * 2 undersample full //too long
		 * 3 undersample recent (1 month) //too long
		 * 4 undersample majority, all
		 * 5 undersample majority, recent (1 month)
		 * 
		 *
		 * 
		 */
		boolean novel = true;
		float socialWeight = 0.25f;
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
		int thirdArg = -1;
		if (args.length > 2) {
			try {
				thirdArg = Integer.parseInt(args[2]);
				samplingChoice = thirdArg;
			} catch (NumberFormatException e) {
				System.err.println("Argument" + args[2] + " must be an integer.");
				System.exit(1);
			}
		}
		switch(secondArg){
		case 2:
			for(socialWeight = 0f; socialWeight <= 1f; socialWeight += 0.05f){
				switch(firstArg){
				case 0:
					fullLFBCAtestGowalla(socialWeight, gaussScale, samplingChoice);
					break;
				case 1:
					fastLURAtestFoursquare(socialWeight, gaussScale, samplingChoice);
					break;
				case 2:
					fastLURAtestGowalla(socialWeight, gaussScale, samplingChoice);
					break;
				case 3:
					float testPercentage = 0.4f;
					fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,5, samplingChoice);
					break;
				case 4:
					fullLFBCAtestBrightkite(socialWeight, gaussScale, samplingChoice);
					break;
				case 5:
					CEPRtestGowalla(socialWeight, gaussScale, samplingChoice);
					break;
				}
			}
			break;
		case 1:
			for(socialWeight = 0f; socialWeight <= 1f; socialWeight += 0.05f){
				switch(firstArg){
				case 0:
					fullLFBCAtestGowalla(socialWeight, gaussScale, samplingChoice);
					break;
				case 1:
					fullLURAtestFoursquare(socialWeight, gaussScale, samplingChoice);
					break;
				case 2:
					fullLURAtestGowalla(socialWeight, gaussScale, samplingChoice);
					break;
				case 3:
					float testPercentage = 0.4f;
					fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,10, samplingChoice);
					break;
				case 4:
					fullLFBCAtestBrightkite(socialWeight, gaussScale, samplingChoice);
					break;
				case 5:
					CEPRtestGowalla(socialWeight, gaussScale, samplingChoice);
					break;
				}
			}
			break;
		case 0:
			switch(firstArg){
			case 0:
				fullLFBCAtestGowalla(socialWeight, gaussScale, samplingChoice);
				break;
			case 1:
				fullLURAtestFoursquare(socialWeight, gaussScale, samplingChoice);
				break;
			case 2:
				fullLURAtestGowalla(socialWeight, gaussScale, samplingChoice);
				break;
			case 3:
				float testPercentage = 0.2f;
				outputSize = 5;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,outputSize, samplingChoice);
				outputSize = 10;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,outputSize, samplingChoice);
				testPercentage = 0.4f;
				outputSize = 5;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,outputSize, samplingChoice);
				outputSize = 10;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,outputSize, samplingChoice);
				break;
			case 4:
				fullLFBCAtestBrightkite(socialWeight, gaussScale, samplingChoice);
				break;
			case 5:
				CEPRtestGowalla(socialWeight, gaussScale, samplingChoice);
				break;
			}
		case 3:
			switch(firstArg){
			case 0:
				fullLFBCAtestGowalla(socialWeight, gaussScale, samplingChoice);
				break;
			case 1:
				fastLURAtestFoursquare(socialWeight, gaussScale, samplingChoice);
				break;
			case 2:
				fullLURAtestGowalla(socialWeight, gaussScale, samplingChoice);
				break;
			case 3:
				float testPercentage = 0.4f;
				outputSize = 10;
				fullLRTtestFoursquare(socialWeight, gaussScale,testPercentage,outputSize, samplingChoice);
				break;
			case 4:
				BrightkiteSnapshot(socialWeight, gaussScale, samplingChoice);
				break;
			case 5:
				CEPRtestGowalla(socialWeight, gaussScale, samplingChoice);
				break;
			}

		case -1:
			switch(firstArg){
			case 4:
				BrightkiteSnapshotMonth(socialWeight, gaussScale, 11, samplingChoice);
				break;
			}
		}



	}

	private static void fullLFBCAtestGowalla(Float socialWeight, Double gaussScale, int samplingChoice){
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
					gaussScale, novel, samplingChoice);
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

	private static void fullLURAtestFoursquare(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("LURATestFourSquare");
		Dataset dataset = new Dataset(DATASETS.fourSquare, COMPETITOR.LURA);
		int neighborhoodSize = 30;
		boolean novel = true;
		int testDays = 60;
		int trainingDays = 300;

		//		int outputSize = 5;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		recommender.runByDay(trainingDays, testDays, trainingDays);
		for(int outputSize: new int[] {5,10,15,20,25}){
			recommender.evaluate(outputSize);
			wekaTest(logger, recommender, outputSize);
		}
	}

	private static void fastLURAtestFoursquare(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("LURATestFourSquare");
		Dataset dataset = new Dataset(DATASETS.fourSquare, COMPETITOR.LURA);
		int neighborhoodSize = 30;
		boolean novel = true;
		int testDays = 60;
		int trainingDays = 300;

		//		int outputSize = 5;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		recommender.runByDay(trainingDays, testDays, trainingDays);
		for(int outputSize: new int[] {10}){
			recommender.evaluate(outputSize);
			wekaTest(logger, recommender, outputSize);
		}
	}

	private static void fullLURAtestGowalla(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("LURATestGowalla");
		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.LURA);
		int neighborhoodSize = 30;
		boolean novel = true;
		int testDays = 60;
		int trainingDays = 420;

		//		int outputSize = 5;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		recommender.runByDay(trainingDays, testDays, trainingDays);
		for(int outputSize: new int[] {5,10,15,20,25}){
			recommender.evaluate(outputSize);
			wekaTest(logger, recommender, outputSize);
		}
	}

	private static void fastLURAtestGowalla(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("LURATestGowalla");
		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.LURA);
		int neighborhoodSize = 30;
		boolean novel = true;
		int testDays = 60;
		int trainingDays = 420;

		//		int outputSize = 5;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		recommender.runByDay(trainingDays, testDays, trainingDays);
		for(int outputSize: new int[] {10}){
			recommender.evaluate(outputSize);
		}
	}

	private static void fullLRTtestFoursquare(Float socialWeight, Double gaussScale, float testPercentage,
			int outputSize, int samplingChoice){
		Logger logger = new Logger("LRTTestFoursquare");
		Dataset dataset = new Dataset(DATASETS.fourSquare, COMPETITOR.LRT);
		int neighborhoodSize = 30;
		boolean novel = true;
		for(int i = 0; i < 5; i++){
			Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
					gaussScale, novel, samplingChoice);
			recommender.runByLocationPercentage(testPercentage);
			recommender.evaluate(outputSize);
			wekaTest(logger, recommender, outputSize);
		}
	}

	private static void fullLFBCAtestBrightkite(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("LFBCATestBrightkite");
		Dataset dataset = new Dataset(DATASETS.brightkite, COMPETITOR.LFBCA);
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		int testMonths = 2;
		int trainingWindow = 1;
		int firstMonth = 3;
		//		firstMonth = 12;

		for(int month =firstMonth; month<=29; month++){
			//		for(int month =9; month<=17; month++){
			Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
					gaussScale, novel, samplingChoice);
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

	private static void BrightkiteSnapshot(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("LFBCATestBrightkite");
		Dataset dataset = new Dataset(DATASETS.brightkite, COMPETITOR.LFBCA);
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		int testMonths = 2;

		int month = 20;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		logTime(logger);
		recommender.runByMonth(month, testMonths, month);
		logTime(logger);
		//			recommender.runByMonth(month, testMonths, trainingWindow);
		recommender.evaluate(outputSize);
		logTime(logger);
		wekaTest(logger, recommender, outputSize);
		logTime(logger);
	}
	
	private static void BrightkiteSnapshotMonth(Float socialWeight, Double gaussScale, int month, int samplingChoice){
		Logger logger = new Logger("LFBCATestBrightkite");
		Dataset dataset = new Dataset(DATASETS.brightkite, COMPETITOR.LFBCA);
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		int testMonths = 2;

		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		logTime(logger);
		recommender.runByMonth(month, testMonths, month);
		logTime(logger);
		//			recommender.runByMonth(month, testMonths, trainingWindow);
		recommender.evaluate(outputSize);
		logTime(logger);
		wekaTest(logger, recommender, outputSize);
		logTime(logger);
	}

	private static void CEPRtestGowalla(Float socialWeight, Double gaussScale, int samplingChoice){
		Logger logger = new Logger("newTest");
		Dataset dataset = new Dataset(DATASETS.gowalla, COMPETITOR.CEPR);
		int neighborhoodSize = 30;
		int outputSize = 10;
		boolean novel = true;
		float testPercentage = 0.3f;
		Recommender recommender = new Recommender(logger, dataset, neighborhoodSize, socialWeight, 
				gaussScale, novel, samplingChoice);
		recommender.runByPercentage(testPercentage);
		recommender.evaluate(outputSize);
		wekaTest(logger, recommender, outputSize);
	}

	private static void wekaTest(Logger logger, Recommender recommender, int outputSize){
		WekaManager wekaMan = new WekaManager(logger, recommender);
		wekaMan.run(outputSize);
	}

	private static void logTime(Logger logger){
		logger.log("" + System.currentTimeMillis());
	}
}
