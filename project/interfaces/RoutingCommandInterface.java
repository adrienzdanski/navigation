package project.interfaces;

import project.classes.CurrentCommand;

public interface RoutingCommandInterface {

	// Berechnen aller Fahr- und Abbiegekommandos f�r eine vorgegebene Route
	public String[] routingCommands(int[] linkIDs) throws Exception;
	
	// Berechnen eines Abbiegekommandos f�r die oben gegebene Route zu einer bestimmten Position
	public CurrentCommand routingCommand(double lat, double lon) throws Exception;
	
	// Aufr�umen aller internen Strukturen
	public void exit();
}