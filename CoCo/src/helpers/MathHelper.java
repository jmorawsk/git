package helpers;

public class MathHelper {
	public static double gaussian(double stddev, double dist, double gaussScale) {
		return Math.pow(Math.E, -(dist / (2 * Math.pow(gaussScale * stddev, 2))));
	}
}
