package helpers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class Dataset {

	public static enum DATASETS {fourSquare, brightkite, gowalla};
	public static enum COMPETITOR {LFBCA, LURA, LRT, NONE};

	private DATASETS baseDataset;
	private COMPETITOR competitor;
	private Date startDate;
	private Date endDate;

	public Dataset(DATASETS dataset, COMPETITOR competitor){
		this.baseDataset = dataset;
		this.competitor = competitor;
		this.setDates();
	}

	public DATASETS getBaseDataset() {
		return baseDataset;
	}
	public void setBaseDataset(DATASETS baseDataset) {
		this.baseDataset = baseDataset;
	}

	public COMPETITOR getCompetitor() {
		return competitor;
	}

	public void setCompetitor(COMPETITOR competitor) {
		this.competitor = competitor;
	}

	public String getCheckinFilename(){
		String name = "";

		switch(this.baseDataset) {
		case fourSquare:
			name =  "";
			if(this.competitor == COMPETITOR.LRT){
				name = "gScorrData/FoursquareCheckins_LRT.csv";
			}
			if(this.competitor == COMPETITOR.LURA){
				name = "gScorrData/FoursquareCheckins_LURA.csv";
			}
			break;
		case brightkite:
			name = "";
			if(this.competitor == COMPETITOR.LFBCA){
				name = "brightkite_gowalla/Brightkite_totalCheckins_LFBCA.txt";
			}
			break;
		case gowalla:
			name = "";
			if(this.competitor == COMPETITOR.LFBCA){
				name = "brightkite_gowalla/Gowalla_totalCheckins_LFBCA.txt";
			}
			if(this.competitor == COMPETITOR.LURA){
				name = "brightkite_gowalla/Gowalla_totalCheckins_LURA.txt";
			}
			break;
		}

		return name;
	}

	public String getFriendshipFilename(){
		String name = "";
		switch(this.baseDataset) {
		case fourSquare:
			name =  "gScorrData/FoursquareFriendship.csv";
			break;
		case brightkite:
			name = "brightkite_gowalla/Brightkite_edges.txt";
			break;
		case gowalla:
			name = "brightkite_gowalla/Gowalla_edges.txt";
			break;
		}
		return name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	private void setDates(){
		GregorianCalendar calendar = new GregorianCalendar();
		switch(this.baseDataset) {
		case fourSquare:
			calendar.set(2011, Calendar.JANUARY, 1, 0, 0 ,0);
			startDate = calendar.getTime();
			calendar.set(2011, Calendar.AUGUST, 1, 0, 0 ,0);
			endDate = calendar.getTime();
			if(this.competitor == COMPETITOR.LRT){
				calendar.set(2011, Calendar.AUGUST, 1, 0, 0 ,0);
				endDate = calendar.getTime();
			}
			if(this.competitor == COMPETITOR.LURA){
				calendar.set(2012, Calendar.JANUARY, 1, 0, 0 ,0);
				endDate = calendar.getTime();
			}
			break;
		case brightkite:
			calendar.set(2008, Calendar.MARCH, 1, 0, 0 ,0);
			startDate = calendar.getTime();
			calendar.set(2010, Calendar.NOVEMBER, 1, 0, 0 ,0);
			endDate = calendar.getTime();
			break;
		case gowalla:
			calendar.set(2009, Calendar.FEBRUARY, 1, 0, 0 ,0);
			startDate = calendar.getTime();
			calendar.set(2010, Calendar.NOVEMBER, 1, 0, 0 ,0);
			endDate = calendar.getTime();
			break;
		}
	}

	public Pattern getFriendshipPattern(){
		Pattern p;
		if(baseDataset.equals(DATASETS.fourSquare)) {
			p = Pattern.compile("(.+),(.+)");
		} else {
			p = Pattern.compile("(.+)\\s+(.+)");
		}
		return p;
	}
	
	public Pattern getCheckinPattern(){
		Pattern p;
		if(baseDataset.equals(DATASETS.fourSquare)) {
			p = Pattern.compile("(.+),(.+),(.+),(.+),(.+)");
		}
		else{
			p = Pattern.compile("(.+)\\s+(.+)\\s+(.+)\\s+(.+)\\s+(.+)");
		}
		return p;
	}
	
	@Override
	public String toString(){
		return baseDataset.name() + competitor.name();
	}
}
