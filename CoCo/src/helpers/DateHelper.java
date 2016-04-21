package helpers;

import java.util.Calendar;
import java.util.Date;

public class DateHelper {
	public static boolean areSameDay(Date date1, Date date2){
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
		                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
	}
	
	public static Date leastDate(Date date1, Date date2){
		return date1 == null ? date2 : (date2 == null ? date1 : (date1.before(date2) ? date1 : date2));
	}
	
	public static Date maxDate(Date date1, Date date2){
		return date1 == null ? date2 : (date2 == null ? date1 : (date1.after(date2) ? date1 : date2));
	}
}
