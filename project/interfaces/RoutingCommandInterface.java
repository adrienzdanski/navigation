package project.interfaces;

import project.classes.CurrentCommand;

public interface RoutingCommandInterface {

	// Berechnen aller Fahr- und Abbiegekommandos für eine vorgegebene Route
	public String[] routingCommands(int[] linkIDs) throws Exception;
	
	// Berechnen eines Abbiegekommandos für die oben gegebene Route zu einer bestimmten Position
	public CurrentCommand routingCommand(double lat, double lon) throws Exception;
	
	// Aufräumen aller internen Strukturen
	public void exit();
}