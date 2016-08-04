package helpers;

import java.util.ArrayList;

public class ListHelper {
	public static Double mean(ArrayList<Double> list) {
		
		Double sum = 0.0;

		for(Double value : list)
			sum += value;

		return sum / list.size();
	}
	
public static Double median(ArrayList<Double> list) {
		
		Double sum = 0.0;

		if(list.size()%2 == 0){
			if(list.isEmpty()){
				return Double.NaN;
			}

			int index = list.size()/2;
			
			sum = (list.get(index)+list.get(index-1)) / 2;
		}
		else {
			sum = list.get((list.size()-1)/2);
		}

		return sum;
	}
}
