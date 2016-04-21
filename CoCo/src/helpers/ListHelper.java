package helpers;

import java.util.ArrayList;

public class ListHelper {
	public static Double mean(ArrayList<Double> list) {
		
		Double sum = 0.0;

		for(Double value : list)
			sum += value;

		return sum / list.size();
	}
}
