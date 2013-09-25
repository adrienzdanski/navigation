package project.classes;

/**
 * Class that stores a latitude longitude pair
 */
public class LatLon {
	public double lat;
	public double lon;
	
	/**
	 * Constructor - stores latitude and longitude
	 * @param lat
	 * @param lon
	 */
	public LatLon(double lat, double lon) {
		this.lat = lat; 
		this.lon = lon;
	}
}
