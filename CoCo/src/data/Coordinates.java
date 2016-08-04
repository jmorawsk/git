package data;

public class Coordinates {

	private double latitude;
	private double longitude;
	
	public Coordinates(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
		toHaversine();
	}
	
	public double getDistance(Coordinates other){
		return Math.sqrt(Math.pow(latitude - other.latitude, 2.0) 
				+ Math.pow(longitude - other.longitude, 2.0));
	}
	
	private void toHaversine() {
		double newLat = latitude * Math.PI / 180.0;
		double newLong = longitude * Math.PI / 180.0;

		double r = 6378.1f;

		latitude = 2.0 * r * Math.asin(Math.sqrt(Math.cos(newLat) * Math.pow(Math.sin(newLong / 2.0), 2.0)));
		longitude = 2.0 * r * Math.asin(Math.abs(Math.sin(newLat / 2.0)));

		latitude *= Math.signum(newLat);
		longitude *= Math.signum(newLong);
		
	}
	
	public String toString(){
		return (latitude+","+longitude);
	}
}
