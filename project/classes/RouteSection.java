package project.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import fu.gps.Spherical;

/**
 * A route section is assembled from many route parts
 */
public class RouteSection {
	public List<RoutePart> parts = new ArrayList<RoutePart>();
	public Geometry geometry;
	public double angleToNorthFrom;
	public double angleToNorthTo;
	public double angleToNext;
	public String commandNaviList = null;
	public String commandPosList = null;
	public int crossingCount;
	
	/**
	 * Constructor.
	 */
	public RouteSection() {}
	
	/**
	 * Constructor.
	 * @param parts
	 */
	public RouteSection(List<RoutePart> parts) {		
		for (RoutePart part : parts) {
			this.parts.add(new RoutePart(part));
		}
		List<Coordinate> coords = new ArrayList<Coordinate>();
		for (RoutePart part : this.parts) {
			Coordinate[] coordsPart = part.roiGeom.getCoordinates();
			List<Coordinate> coordList = Arrays.asList(coordsPart);
			coords.addAll(coordList);
		}
		this.geometry = new GeometryFactory().createLineString((Coordinate[])coords.toArray(new Coordinate[0]));
	}
	
	/**
	 * Returns first route part of section.
	 * @return RoutePart
	 */
	public RoutePart getFirstPart() {
		return this.parts.get(0);
	}
	
	/**
	 * Returns last route part of section.
	 * @return RoutePart
	 */
	public RoutePart getLastPart() {
		return this.parts.get(this.parts.size() - 1);
	}
	
	/**
	 * Sets the angles to north for start and end of section.
	 * @param angleFrom
	 * @param angleTo
	 */
	public void setAnglesToNorth(double angleFrom, double angleTo) {
		this.angleToNorthFrom = angleFrom;
		this.angleToNorthTo = angleTo;
	}
	
	/**
	 * Projects point on geometry.
	 * @param gpsCoord
	 * @return Coordinate
	 */
	public Coordinate getProjectedPoint(Geometry gpsCoord) {
		LocationIndexedLine line = new LocationIndexedLine(this.geometry);
    	LinearLocation linearLocation = line.project(gpsCoord.getCoordinate());
    	return linearLocation.getCoordinate(this.geometry);
	}
	
	/**
	 * Calculates distance to point.
	 * @param gpsCoord
	 * @return distance to gpsCoord point
	 */
	public double getDistanceToPoint(Geometry gpsCoord) {
    	Coordinate projectedPoint = this.getProjectedPoint(gpsCoord);
    	Coordinate[] coords = gpsCoord.getCoordinates();
    	return Spherical.greatCircleMeters(coords[0].x, coords[0].y, projectedPoint.x, projectedPoint.y);
	}
}
