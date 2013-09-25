package project.classes;

import fu.gps.Spherical;
import gao.tools.SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * DBConnection Class.
 * Establishes connection and retrieves Data from DB.
 */
public class DBConnection {
	public static DBConnection instance = null;
	private Connection connection = null;
	private Geometry tempGeometry;
	
	/**
	 * Private constructor for singleton.
	 */
    private DBConnection() {}
    
    /**
     * Get or generate new DBConnection instance.
     * @return DBConnection instance
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }
    
    /**
     * Connect to database.
     * The database informations are stored in the Config class.
     */
    public void connect() {
    	try {
            String connectionString="jdbc:postgresql://" + Config.dbhost + ":" + Config.dbport + "/" + Config.dbname;
            this.connection = DriverManager.getConnection(connectionString, Config.dbuser, Config.dbpasswd);
            this.connection.setAutoCommit(false);
        } catch (Exception e) {
            System.out.println("Error init DB: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Close the database connection.
     */
    public void close() {
    	try {
			this.connection.close();
		} catch (SQLException e) {
			System.out.println("Error closing DB: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
    }
    
    /**
     * Query Database for current route.
     * @param links
     * @return complete route
     * @throws Exception
     */
    public RouteComplete queryLinks(int[] links) throws Exception {
    	Statement statement;
    	statement = this.connection.createStatement();
        statement.setFetchSize(1000);
        Statement statementFrom;
        Statement statementTo;
        Statement statementDomain;
        Statement statementCrossing;
        RouteComplete route = new RouteComplete();
        
        int startID = 0;
        int endID = links.length;
        for(int i = startID; i < endID; i++) {	
        	ResultSet resultSet;
        	
        	String query = "SELECT l.id, " +
        				          "l.d_id, " +
		        				  "l.crossing_id_from, " +
								  "l.crossing_id_to, " +
								  "l.meters, " +
								  "l.lsiclass, " +
								  "lsi.description " +
						   "FROM link l, lsiclasses lsi " +
						   "WHERE l.lsiclass = lsi.id " +
						   "AND l.id = " + links[i] + " ";
						   
        	resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				int id = (int)resultSet.getLong(1);
				int d_id = (int)resultSet.getLong(2);
				int crossing_id_from = (int)resultSet.getLong(3);
				int crossing_id_to = (int)resultSet.getLong(4);
				int meters = (int)resultSet.getLong(5);
				int lsiclass = (int)resultSet.getLong(6);
				String lsidescription = resultSet.getString(7);
							
				query = "SELECT c.posnr, c.lat, c.long FROM crossing c WHERE c.id = " + String.valueOf(crossing_id_from) + " AND c.d_id = " + String.valueOf(d_id);
		        statementFrom = this.connection.createStatement();
		        statementFrom.setFetchSize(10);
				ResultSet resultSetCrossingFrom = statementFrom.executeQuery(query);
				int geomPosFrom = 0;
				double latFrom = 0.0;
				double longFrom = 0.0;
				while(resultSetCrossingFrom.next()) {
					geomPosFrom = (int)resultSetCrossingFrom.getLong(1);
					latFrom = resultSetCrossingFrom.getDouble(2);
					longFrom = resultSetCrossingFrom.getDouble(3);
				}
				resultSetCrossingFrom.close();
				
				query = "SELECT c.posnr, c.lat, c.long FROM crossing c WHERE c.id = " + String.valueOf(crossing_id_to) + " AND c.d_id = " + String.valueOf(d_id);
				statementTo = this.connection.createStatement();
			    statementTo.setFetchSize(10);
				ResultSet resultSetCrossingTo = statementTo.executeQuery(query);
				int geomPosTo = 0;
				double latTo = 0.0;
				double longTo = 0.0;
				while(resultSetCrossingTo.next()) {
					geomPosTo = (int)resultSetCrossingTo.getLong(1);
					latTo = resultSetCrossingTo.getDouble(2);
					longTo = resultSetCrossingTo.getDouble(3);
				}
				resultSetCrossingTo.close();
				
				query = "SELECT d.realname, d.geodata_line FROM domain d " +
						"WHERE d.d_id = " + String.valueOf(d_id) + " AND (d.geometry='L') ";
		        statementDomain = this.connection.createStatement();
		        statementDomain.setFetchSize(10);
				ResultSet resultSetDomain = statementDomain.executeQuery(query);
				String realname = "";
				while(resultSetDomain.next()) {
					realname = resultSetDomain.getString(1);
					if(realname.matches(".+;.+")) {
						realname = realname.split(";")[0];
					}
					byte[] geodata_line = resultSetDomain.getBytes(2);
					this.tempGeometry = SQL.wkb2Geometry(geodata_line);
				}
				Geometry geometry = this.tempGeometry;
				resultSetCrossingTo.close();
				
				query = "SELECT d.geodata_line " +
						"FROM crossing c, domain d " +
						"WHERE c.d_id = d.d_id " +
						"AND c.id = " + String.valueOf(crossing_id_to);
				statementCrossing = this.connection.createStatement();
				statementCrossing.setFetchSize(10);
				ResultSet resultSetCrossing = statementCrossing.executeQuery(query);
				List<Geometry> geomList = new ArrayList<Geometry>();
				while(resultSetCrossing.next()) {
					byte[] geodata_line = resultSetCrossing.getBytes(1);
					geomList.add(SQL.wkb2Geometry(geodata_line));
				}
				boolean isTCrossing = this.checkTCrossing(geomList, longTo, latTo);
				resultSetCrossing.close();
				
				route.addRoutePart(new RoutePart(id,
												 d_id,
												 crossing_id_from,
												 crossing_id_to,
												 longFrom,
												 latFrom,
												 longTo,
												 latTo,
												 geomPosFrom,
												 geomPosTo,
												 meters,
												 lsiclass,
												 realname,
												 lsidescription,
												 geometry, 
												 isTCrossing));
			}
			resultSet.close();
        }
        return route;
    }
    
    /**
     * Check route parts for T-Crossings.
     * @param geomList
     * @param lon
     * @param lat
     * @return returns true if crossing is T-Crossing
     */
    private boolean checkTCrossing(List<Geometry> geomList, double lon, double lat) {
    	boolean isTCrossing = false;
    	Coordinate startCoord = new Coordinate(lon, lat);
    	if(geomList.size() == 3) {
    		List<Coordinate[]> coordArrList = new ArrayList<Coordinate[]>();
			for(Geometry geom : geomList) {
				coordArrList.add(geom.getCoordinates());
			}
			//Find Index of lon/lat in List
			int[] idx = new int[3];
			int counter = 0;
			for(Coordinate[] coordArr : coordArrList) {
				for(int i = 0; i < coordArr.length; i++) {
					if(coordArr[i].x == startCoord.x && coordArr[i].y == startCoord.y) {
						idx[counter] = i;
					}
				}
				counter++;
			}
			counter = 0;
			//Find neighbour of idx
			List<Coordinate[]> streetSegments = new ArrayList<Coordinate[]>();
			for(Coordinate[] coordArr : coordArrList) {
				Coordinate[] tempCoordArr = new Coordinate[2];
				tempCoordArr[0] = coordArr[idx[counter]];
				double curlon = coordArr[idx[counter]].x;
				double curlat = coordArr[idx[counter]].y;
				Coordinate before = new Coordinate();
				Coordinate after = new Coordinate();
				
				if(idx[counter] == 0) {
					tempCoordArr[1] = coordArr[1];
					streetSegments.add(tempCoordArr);
				} else if(idx[counter] == coordArr.length - 1) {
					tempCoordArr[1] = coordArr[coordArr.length - 2];
					streetSegments.add(tempCoordArr);
				} else {
					for(int i = 0; i < coordArr.length; i++) {
						if(coordArr[i].x == curlon && coordArr[i].y == curlat) {
							before = coordArr[i - 1];
							after = coordArr[i + 1];
						}
					}
					//Add Element if not in list
					tempCoordArr[1] = before;
					if(!streetSegments.contains(tempCoordArr)) {
						streetSegments.add(tempCoordArr);
					}
					tempCoordArr[1] = after;
					if(!streetSegments.contains(tempCoordArr)) {
						streetSegments.add(tempCoordArr);
					}
				}
				counter++;
			}
			
			//Calculate Angles
			Coordinate[] street1 = streetSegments.get(0);
			Coordinate[] street2 = streetSegments.get(1);
			Coordinate[] street3 = streetSegments.get(2);
			
			//Angle between 1 & 2
			double angle12 = this.angleBetweenStreets(street1, street2);
			//Angle between 2 & 3
			double angle23 = this.angleBetweenStreets(street2, street3);
			//Angle between 1 & 3
			double angle13 = this.angleBetweenStreets(street1, street3);
			
			//Check for TCrossing
			int is180 = 0;
			int is90 = 0;
			
			if(angle12 >= 80.0 && angle12 <= 100.0) {
				is90++;
			}
			if(angle12 >= 170.0 && angle12 <= 180.0) {
				is180++;
			}
			
			if(angle23 >= 80.0 && angle23 <= 100.0) {
				is90++;
			}
			if(angle23 >= 170.0 && angle23 <= 180.0) {
				is180++;
			}
			
			if(angle13 >= 80.0 && angle13 <= 100.0) {
				is90++;
			}
			if(angle13 >= 170.0 && angle13 <= 180.0) {
				is180++;
			}

			if(is180 == 1 && is90 == 2) {
				isTCrossing = true;
			}
    	}
    	return isTCrossing;
    }
    
    /**
     * Calculate angle between streets
     * @param street1 coordinates
     * @param street2 coordinates
     * @return angle between streets
     */
    private double angleBetweenStreets(Coordinate[] street1, Coordinate[] street2) {
    	return Spherical.surfaceAngle(street1[0].y, street1[0].x, street1[1].y, street1[1].x, street2[1].y, street2[1].x);
    }
}
