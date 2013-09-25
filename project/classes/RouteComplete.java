package project.classes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import fu.gps.Spherical;
/**
 * The RouteComplete class represents the complete route assembled from route parts and route sections
 */
public class RouteComplete {
	List<RoutePart> allRouteParts = new ArrayList<RoutePart>();
	List<RouteSection> routeSections = new ArrayList<RouteSection>();
	Geometry completeRoute;
	double oldDistance = 0.0;
	double newDistance = 0.0;
	Coordinate lastProjectedPoint = null;
	double distanceLeft = 0.0;	
	boolean command800Handled = false;
	boolean command300Handled = false;
	boolean command100Handled = false;
	boolean commandNOWHandled = false;
	
	/**
	 * Prints route details of route part.
	 */
	public void printRouteDetails() {
		for (RoutePart part : this.allRouteParts) {
			part.printDetails();
		}
	}
	
	/**
	 * Adds part to route.
	 * @param part
	 */
	public void addRoutePart(RoutePart part) {
		this.allRouteParts.add(part);
	}
	
	/**
	 * Assembles route parts to route sections.
	 */
	public void assembleRouteSections() {
		List<RoutePart> parts = new ArrayList<RoutePart>();
		
		for (int i = 0; i < this.allRouteParts.size(); i++) {
			if(i < this.allRouteParts.size() - 1) {
				RoutePart prevPart = null;
				if(i > 0) {
					prevPart = this.allRouteParts.get(i - 1);
				}
				RoutePart currentPart = this.allRouteParts.get(i);
				RoutePart nextPart = this.allRouteParts.get(i + 1);
				RoutePart nextOfNextPart = null;
				if((i + 2) < this.allRouteParts.size()) {
					nextOfNextPart = this.allRouteParts.get(i + 2);
				}
				parts.add(currentPart);
				
				/* Bridges */
				boolean nextIsBridge = false;
				if(nextPart.realname.matches("\\w*(b|B)rücke\\w*")) {
					nextIsBridge = true;
				}

				if((i + 2) < this.allRouteParts.size()) {
					boolean curAndNextAreEqual = nextOfNextPart.realname.equals(currentPart.realname);
					boolean lsiCurNextNextOfNextAreEqual =
							(currentPart.lsiclass == nextPart.lsiclass && nextPart.lsiclass == nextOfNextPart.lsiclass) ? true : false;
					if(nextIsBridge && curAndNextAreEqual && lsiCurNextNextOfNextAreEqual) {
						this.allRouteParts.get(i + 1).realname = currentPart.realname;
					}
				}
				
				//Assemble Streets
				boolean prevAndNextHaveSameName = false;
				boolean lsiPrevCurrentNextAreEqual = false;
				if(i > 0) {
					prevAndNextHaveSameName = prevPart.realname.equals(nextPart.realname);
					lsiPrevCurrentNextAreEqual = (prevPart.lsiclass == currentPart.lsiclass && currentPart.lsiclass == nextPart.lsiclass) ? true : false;
				}
				boolean partsAreConnections = ((currentPart.lsiclass == 35161 || currentPart.lsiclass == 35162 || currentPart.lsiclass == 35163)
						               			&& (nextPart.lsiclass == 35161 || nextPart.lsiclass == 35162 || nextPart.lsiclass == 35163)
											  ) ? true : false;
				//boolean currentNextAreConnectionsByRegEx = (currentPart.realname.matches("\\bautobahnanschlussstelle_\\d*\\b") == nextPart.realname.matches("\\bautobahnanschlussstelle_\\d*\\b")) ? true : false;
				boolean curHighwayRegEx = currentPart.realname.matches("\\bautobahnanschlussstelle_\\d*\\b");
				boolean nextHighwayRegEx = nextPart.realname.matches("\\bautobahnanschlussstelle_\\d*\\b");
				boolean currentNextAreConnectionsByRegEx = (curHighwayRegEx == true && nextHighwayRegEx == true) ? true : false;
				boolean lsiClassesAreEqual = (currentPart.lsiclass == nextPart.lsiclass) ? true : false;
				boolean partsAreHighways = (currentPart.lsiclass == 35110 && nextPart.lsiclass == 35110) ? true : false;
				boolean partsHaveSameName = currentPart.realname.equals(nextPart.realname);

				/* Bindestriche entfernen und vergleichen */
				String temp1 = currentPart.realname.replaceAll("-", "").toLowerCase();
				String temp2 = nextPart.realname.replaceAll("-", "").toLowerCase();
				boolean stringsAreEqual = temp1.equals(temp2);
				
				boolean assembleNewStreet = true;
				if(partsHaveSameName && lsiClassesAreEqual) {
					assembleNewStreet = false;
				} else
				if(partsHaveSameName && partsAreHighways) {
					assembleNewStreet = false;
				} else
				if(currentNextAreConnectionsByRegEx && partsAreConnections) {
					assembleNewStreet = false;
				} else
				if(partsHaveSameName && partsAreConnections) {
					assembleNewStreet = false;
				} else
				if(stringsAreEqual) {
					assembleNewStreet = false;
				} else
				if(partsHaveSameName) {
					assembleNewStreet = false;
				}
				
				if(assembleNewStreet) {
					this.routeSections.add(new RouteSection(parts));
					parts.clear();
				}
			} else {
				//Add last two parts
				parts.add(this.allRouteParts.get(i));
				this.routeSections.add(new RouteSection(parts));
				parts.clear();
			}
		}
	}
	
	/**
	 * Calculates angle to north.
	 */
	public void calculateAnglesToNorth() {
		for(int i = 0; i < this.routeSections.size(); i++) {
			Coordinate[] curCoords = this.routeSections.get(i).geometry.getCoordinates();
			if(curCoords.length >= 2) {
				Coordinate curCoordFrom1 = curCoords[0];
				Coordinate curCoordFrom2 = curCoords[1];
				Coordinate curCoordTo1 = curCoords[curCoords.length - 2];
				Coordinate curCoordTo2 = curCoords[curCoords.length - 1];
				double angleToNorthFrom = Spherical.northAngle(curCoordFrom1.y, curCoordFrom1.x, curCoordFrom2.y, curCoordFrom2.x);
				double angleToNorthTo = Spherical.northAngle(curCoordTo1.y, curCoordTo1.x, curCoordTo2.y, curCoordTo2.x);
				this.routeSections.get(i).setAnglesToNorth(angleToNorthFrom, angleToNorthTo);
			} else {
				System.out.println("RouteComplete - to low args " + curCoords.length + this.routeSections.get(i).getLastPart().realname);
			}
		}
	}
	
	/** 
	 * Calculates angle to next route section.
	 */
	public void calculateAnglesToNext() {
		for (int i = 0; i < this.routeSections.size() - 1; i++) {
			double currentAngle = this.routeSections.get(i).angleToNorthTo;
			double nextAngle = this.routeSections.get(i + 1).angleToNorthFrom;
			this.routeSections.get(i).angleToNext = nextAngle - currentAngle;
		}
	}
	
	/**
	 * Generates list of commands.
	 */
	public void generateCommandList() {
		CommandStringGenerator generator = new CommandStringGenerator();
		for (int i = 0; i < this.routeSections.size(); i++) {
			if(i < this.routeSections.size() - 1) {
				this.routeSections.get(i).commandNaviList = generator.generateNavigationListEntry(this.routeSections, i, false);
			} else {
				this.routeSections.get(i).commandNaviList = generator.generateNavigationListEntryGoal(this.routeSections.get(i));
			}
		}
	}
	
	/**
	 * Generates the complete route geometry.
	 */
	public void buildCompleteStreetGeometry() {
		List<Coordinate> coords = new ArrayList<Coordinate>();
		for(RouteSection section : this.routeSections) {
			Coordinate[] sectionCoords = section.geometry.getCoordinates();
			for(int i = 0; i < sectionCoords.length; i++) {
				coords.add(sectionCoords[i]);
			}
		}
		this.completeRoute = new GeometryFactory().createLineString((Coordinate[])coords.toArray(new Coordinate[0]));
	}
	
	/**
	 * Generates command list.
	 * @return list of commands
	 */
	public String[] printCommandList() {
		String[] commandList = new String[this.routeSections.size()];
		for (int i = 0; i < this.routeSections.size(); i++) {
			commandList[i] = this.routeSections.get(i).commandNaviList;
		}
		return commandList;
	}
	
	/**
	 * Generates current command for given longitude and latitude.
	 * @param lon
	 * @param lat
	 * @return CurrentCommand
	 */
	public CurrentCommand generateCommandFromLatLong(double lon, double lat) {
		CurrentCommand command = new CurrentCommand();
		
		//Create Geometry for projection
		Geometry gpsCoord = new GeometryFactory().createPoint(new Coordinate(lat, lon));
		double currentDistanceToProjectedPoint = this.routeSections.get(0).getDistanceToPoint(gpsCoord);
		RouteSection currentSection = new RouteSection();
		for(RouteSection section : this.routeSections) {
			double distance = section.getDistanceToPoint(gpsCoord);
			if(currentDistanceToProjectedPoint >= distance) {
				currentSection = section;
				currentDistanceToProjectedPoint = distance;
			}
		}
		
		//Fill Command with projected Point
		Coordinate projectedPoint = currentSection.getProjectedPoint(gpsCoord);
		command.projectedLon = projectedPoint.x;
		command.projectedLat = projectedPoint.y;
		
		//Calculate driven distance
		this.oldDistance = this.newDistance;
		double distance = 0.0;
		Coordinate[] coords = this.completeRoute.getCoordinates();
		for(int i = 0; i < coords.length - 1; i++) {
			double meters = Spherical.greatCircleMeters(coords[i].y, coords[i].x, coords[i + 1].y, coords[i + 1].x);
			double[] distanceCircle = Spherical.precomputeDistanceCircle(coords[i].y, coords[i].x, meters);
			if(Spherical.isInsideDistanceCircle(distanceCircle, projectedPoint.y, projectedPoint.x)) {
				distance += Spherical.greatCircleMeters(coords[i].y, coords[i].x, projectedPoint.y, projectedPoint.x);
				break;
			} else {
				distance += meters;
			}
		}
		this.newDistance = distance;
		double metersSinceLastCommand = this.newDistance - this.oldDistance;
		command.meters = Math.round(metersSinceLastCommand);
		
		//Get DistanceString for Command
		String distanceString = "In ";
		Coordinate[] currentCoords = currentSection.geometry.getCoordinates();
		double distanceToProjPoint = 0.0;
		//Get distance to projected point
		for(int j = 0; j < currentCoords.length - 1; j++) {
			double meters = Spherical.greatCircleMeters(currentCoords[j].y, currentCoords[j].x, currentCoords[j + 1].y, currentCoords[j + 1].x);
			double[] distanceCircle = Spherical.precomputeDistanceCircle(currentCoords[j].y, currentCoords[j].x, meters);
			
			if(Spherical.isInsideDistanceCircle(distanceCircle, projectedPoint.y, projectedPoint.x)) {
				distanceToProjPoint += Spherical.greatCircleMeters(currentCoords[j].y, currentCoords[j].x, projectedPoint.y, projectedPoint.x);
				break;
			} else {
				distanceToProjPoint += meters;
			}
		}
		//Get distance of section
		double totalDistanceSection = 0.0;
		for(int x = 0; x < currentCoords.length - 1; x++) {
			totalDistanceSection += Spherical.greatCircleMeters(currentCoords[x].y, currentCoords[x].x, currentCoords[x + 1].y, currentCoords[x + 1].x);
		}
		double distanceLeft = totalDistanceSection - distanceToProjPoint;
		this.distanceLeft = distanceLeft;
		
		/* Staffelung */
		boolean newCommand = false;
		if(this.distanceLeft <= 850 && this.distanceLeft > 350 && !command800Handled) {
			distanceString += "800 m ";
			newCommand = true;
			commandNOWHandled = false;
			command300Handled = false;
			command100Handled = false;
			command800Handled = true;
		}
		if(this.distanceLeft <= 350 && this.distanceLeft > 150 && !command300Handled) {
			distanceString += "300 m ";
			newCommand = true;
			command800Handled = false;
			command100Handled = false;
			commandNOWHandled = false;
			command300Handled = true;
		}
		if(this.distanceLeft <= 150 && this.distanceLeft > 50 && !command100Handled) {
			distanceString += "100 m ";
			newCommand = true;
			command800Handled = false;
			command300Handled = false;
			commandNOWHandled = false;
			command100Handled = true;
		}
		if(this.distanceLeft <= 30 && !commandNOWHandled) {
			distanceString = "JETZT ";
			newCommand = true;
			command800Handled = false;
			command300Handled = false;
			command100Handled = false;
			commandNOWHandled = true;
		}
		//distanceString += this.getDistanceString(distanceLeft);
		int currentSectionID = this.routeSections.indexOf(currentSection);
		CommandStringGenerator generator = new CommandStringGenerator();
		String sectionCommand = "";
		if(currentSectionID < this.routeSections.size() - 1) {
			sectionCommand = generator.generateNavigationListEntry(this.routeSections, currentSectionID, true);
		} else {
			sectionCommand = generator.generateNavigationListEntryGoal(currentSection);
		}
		//Fill Command with Commandstring
		if(currentDistanceToProjectedPoint < 50.0) {
			if(currentSectionID < this.routeSections.size() - 1) {
				if(newCommand) {
					command.command = distanceString + sectionCommand;
				} else {
					command.command = "";
				}
				
			} else {
				command.command = sectionCommand;
			}
			this.lastProjectedPoint = projectedPoint;
		} else {
			command.command = null;
		}
		return command;
	}

	/**
	 * Generates and formats the distance string.
	 * @param distance
	 * @return String
	 */
	private String getDistanceString(double distance) {
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#0.00", dfs);
		if(distance >= 1000.0) {
			return String.valueOf(df.format(distance / 1000.0)) + " km ";
		}
		return String.valueOf(df.format(distance)) + " m ";
	}
}
