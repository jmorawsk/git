package helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;

public class MapHelper {
	public static HashMap<String, Float> getTopItems(HashMap<String, Float> items, int n){
		HashMap<String, Float> top = new HashMap<String, Float>();
		ArrayList<String> itemArray = new ArrayList<String>();
		for(String item: items.keySet()){
			itemArray.add(item);
		}
		Collections.sort(itemArray, new Comparator<String>(){

			@Override
			public int compare(String left, String right) {
				// Intentionally reversed
				return Float.compare(items.get(right),items.get(left));
			}
		});
		for(int i =0; i < n && i < itemArray.size(); i++){
			top.put(itemArray.get(i), items.get(itemArray.get(i)));
		}
		return top;
	}
}
