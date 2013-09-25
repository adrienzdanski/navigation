package project.classes;

import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * One route part represents a link and its details.
 */
public class RoutePart {
	public int id;
	public int d_id;
	public int crossing_id_from;
	public int crossing_id_to;
	public double long_from;
	public double lat_from;
	public double long_to;
	public double lat_to;
	public int geomPosFrom;
	public int geomPosTo;
	public int meters;
	public int lsiclass;
	public String realname;
	public String lsidescription;
	public Geometry geom;
	public boolean isTCrossing;
	
	public Geometry roiGeom;
	
	/**
	 * Constructor
	 * @param id
	 * @param d_id
	 * @param crossing_id_from
	 * @param crossing_id_to
	 * @param long_from
	 * @param lat_from
	 * @param long_to
	 * @param lat_to
	 * @param geomPosFrom
	 * @param geomPosTo
	 * @param meters
	 * @param lsiclass
	 * @param realname
	 * @param lsidescription
	 * @param geom
	 * @param isTCrossing
	 */
	public RoutePart(int id, 
					 int d_id,
					 int crossing_id_from, 
					 int crossing_id_to, 
					 double long_from, 
					 double lat_from, 
					 double long_to,
					 double lat_to,
					 int geomPosFrom,
					 int geomPosTo,
					 int meters,
					 int lsiclass, 
					 String realname, 
					 String lsidescription, 
					 Geometry geom, 
					 boolean isTCrossing) {
		this.id = id;
		this.d_id = d_id;
		this.crossing_id_from = crossing_id_from;
		this.crossing_id_to = crossing_id_to;
		this.long_from = long_from;
		this.lat_from = lat_from;
		this.long_to = long_to;
		this.lat_to = lat_to;
		this.geomPosFrom = geomPosFrom;
		this.geomPosTo = geomPosTo;
		this.meters = meters;
		this.lsiclass = lsiclass;
		this.realname = realname;
		this.lsidescription = lsidescription;
		this.geom = geom;
		this.isTCrossing = isTCrossing;
	}
	
	/**
	 * Copy constructor.
	 * @param part
	 */
	public RoutePart(RoutePart part) {
		this.id = part.id;
		this.d_id = part.d_id;
		this.crossing_id_from = part.crossing_id_from;
		this.crossing_id_to = part.crossing_id_to;
		this.long_from = part.long_from;
		this.lat_from = part.lat_from;
		this.long_to = part.long_to;
		this.lat_to = part.lat_to;
		this.geomPosFrom = part.geomPosFrom;
		this.geomPosTo = part.geomPosTo;
		this.meters = part.meters;
		this.lsiclass = part.lsiclass;
		this.realname = part.realname;
		this.lsidescription = part.lsidescription;
		this.geom = (Geometry)part.geom.clone();
		this.cutGeometry();
		this.isTCrossing = part.isTCrossing;
	}
	
	/**
	 * Creates geometry for current part
	 */
	private void cutGeometry() {		
		Coordinate[] coords = this.geom.getCoordinates();
		int size = Math.abs(this.geomPosTo - this.geomPosFrom);
		Coordinate[] roiCoords = new Coordinate[size + 1];
		if(this.geomPosFrom < this.geomPosTo) {
			for(int y = this.geomPosFrom, z = 0; y <= this.geomPosTo; y++) {
				roiCoords[z++] = coords[y];
			}
		} else {
			for(int y = this.geomPosFrom, z = 0; y >= this.geomPosTo; y--) {
				roiCoords[z++] = coords[y];
			}
		}
		Geometry newGeom = new GeometryFactory().createLineString(roiCoords);
		this.roiGeom = (Geometry)newGeom.clone();
	}
	
	/**
	 * Returns index of given coordinate.
	 * @param lat
	 * @param lon
	 * @param coords
	 * @return int
	 */
	public int getIndexOfElement(double lat, double lon, Coordinate[] coords) {
		List<Coordinate> coordList = Arrays.asList(coords);
		Coordinate coord = new Coordinate(lon, lat);
		return coordList.indexOf(coord);
	}
	
	/**
	 * Prints details to console.
	 */
	public void printDetails() {
		System.out.println(this.long_from + ", " + this.lat_from);
		System.out.println("ID:               " + this.id);
		System.out.println("DID:              " + this.d_id);
		System.out.println("Realname:         " + this.realname);
		System.out.println("Crossing ID From: " + this.crossing_id_from);
		System.out.println("Crossing ID To:   " + this.crossing_id_to);
		System.out.println("Long From:        " + this.long_from);
		System.out.println("Lat From:         " + this.lat_from);
		System.out.println("Meters:           " + this.meters + " m");
		System.out.println("LSI-Class:        " + this.lsiclass);
		System.out.println("LSI-Description:  " + this.lsidescription);
		System.out.println("===============================================");
		
	}
}
