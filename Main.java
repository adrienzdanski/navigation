import java.util.List;

import project.classes.Config;
import project.classes.CurrentCommand;
import project.classes.FileLoader;
import project.classes.LatLon;
import project.classes.RoutingCommand;

public class Main {
	/**
	 * Main Initializes route generation and live routing.
	 * Reads links and positions from file.
	 * Starts printing the route and live routing.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		FileLoader loader = new FileLoader();
		RoutingCommand routingCommand = new RoutingCommand();
		
		/* Display Routing List */
		int[] links = loader.readLinks(Config.pathToLinks);
		printRoutingCommandsOverview(routingCommand.routingCommands(links));
		
		/* Display Navigation Commands */
		List<LatLon> positions = loader.readPositions(Config.pathToPositions);
		
		int startID = 0;
        int endID = positions.size();
        double distanceUntilCommand = 0.0;
        String lastCommandString = "";
		for(int i = startID; i < endID; i++) {
			double lat = positions.get(i).lat;
			double lon = positions.get(i).lon;
			CurrentCommand command = routingCommand.routingCommand(lat, lon);

			distanceUntilCommand += command.meters;
			String currentCommandString = command.command;
			
			boolean nextCommand = false;
			boolean commandIsNull = false;
			if(i < endID - 1) {
				if(currentCommandString == null) {
					commandIsNull = true;
				} else {
					String[] temp = currentCommandString.split("In\\s+\\d+\\.\\d+\\s+m|km\\s+");
					if(temp.length == 2) {
						currentCommandString = temp[1];
					} else {
						currentCommandString = "";
					}
					if(!currentCommandString.equals(lastCommandString)) {
						nextCommand = true;
					}
				}
			}
			
			if(!commandIsNull) {
				printCurrentCommand(lat, lon, command);
			}

			/*
			if(nextCommand && !commandIsNull) {
				printCurrentCommand(lat, lon, command);
			} else {
				if(distanceUntilCommand >= Config.distanceToCommand) {
					distanceUntilCommand = distanceUntilCommand % Config.distanceToCommand;
					printCurrentCommand(lat, lon, command);
				} else {
					command.command = "";
					printCurrentCommand(lat, lon, command);
				}
			}
			*/
			
			lastCommandString = currentCommandString;
		}
		
		/* Close DB Connection */
		routingCommand.exit();
	}
	
	/**
	 * Prints the routing list to the console.
	 * @param commands
	 */
	private static void printRoutingCommandsOverview(String[] commands) {
		if(commands != null && commands.length > 0) {
			for(int i = 0; i < commands.length; i++) {
				System.out.println(commands[i]);
			}
		} else {
			System.out.println("Empty command list!");
		}
		System.out.println("====================");
	}
	
	/**
	 * Prints the live routing command to the console
	 * @param lat
	 * @param lon
	 * @param command
	 */
	private static void printCurrentCommand(double lat, double lon, CurrentCommand command) {
		if(command.command != null)
			System.out.println(lat + "," + lon + "," + command.projectedLat + "," + command.projectedLon + "," + (int)command.meters + "," + command.command);
	}
}
