package project.classes;

import project.interfaces.RoutingCommandInterface;

import com.vividsolutions.jts.geom.Coordinate;
/**
 * Initializes the programm.
 */
public class RoutingCommand implements RoutingCommandInterface {
	private DBConnection db = DBConnection.getInstance();
	private RouteComplete route = new RouteComplete();
	
	/**
	 * Constructor.
	 */
	public RoutingCommand() {}
	
	@Override
	public void exit() {
		this.db.close();
	}
	
	@Override
	public String[] routingCommands(int[] links) throws Exception {
		this.db.connect();
		this.route = this.db.queryLinks(links);
		this.route.assembleRouteSections();
		this.route.calculateAnglesToNorth();
		this.route.calculateAnglesToNext();
		this.route.generateCommandList();
		this.route.buildCompleteStreetGeometry();
		if(Config.printCoordsForDorenda) {
			this.printCoordsForDorenda();
		}
		return this.route.printCommandList();
	}
	@Override
	public CurrentCommand routingCommand(double lat, double lon) throws Exception {
		return this.route.generateCommandFromLatLong(lat, lon);
	}
	
	/** 
	 * Prints coordinates to console formated for use in dorenda
	 */
	private void printCoordsForDorenda() {
		for (RouteSection item : this.route.routeSections) {
			Coordinate[] coord = item.geometry.getCoordinates();
			for(int i = 0; i < coord.length; i++) {
				System.out.println(coord[i].x + "," + coord[i].y);
			}
		}
	}
}
