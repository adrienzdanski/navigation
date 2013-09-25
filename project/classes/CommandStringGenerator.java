package project.classes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
/**
 * The CommandStringGenerator class manages the output of the routing commands.
 * It determines the structure of the sentence by the given parameters.
 */
public class CommandStringGenerator {
	public double currentAccumulatedDistance = 0.0;
	
	/**
	 * Generates one navigation command
	 * @param current
	 * @param next
	 * @param isCommand
	 * @return formated navigation command
	 */
	public String generateNavigationListEntry(List<RouteSection> list, int index, boolean isCommand) {
		RouteSection current = list.get(index);
		RouteSection next = list.get(index + 1);
		RouteSection nextOfNext = null;
		if(index + 2 < list.size()) {
			nextOfNext = list.get(index + 2);
		}
		double sectionDistance = 0.0;
		for (RoutePart part : current.parts) {
			if(!isCommand) this.currentAccumulatedDistance += part.meters;
			sectionDistance += part.meters;
		}
		String drive = this.getDistanceStringWithStreet(sectionDistance, current, next);
		String start = this.getStartingPhrase(current, next, false);
		String crossing = this.getCrossingType(current, next);
		String direction = this.getDirection(current, next);
		String street = this.getStreetName(next, nextOfNext);
		
		if(isCommand) {
			String startCommand = this.getStartingPhrase(current, next, true);
			return startCommand + crossing + direction + street;
		} else {
			return drive + start + crossing + direction + street;
		}
	}
	
	/**
	 * Formats goal string and total distance
	 * @param current
	 * @return formated goal string and total distance
	 */
	public String generateNavigationListEntryGoal(RouteSection current) {
		return "Sie haben Ihr Ziel " + current.getLastPart().realname + " erreicht. Distanz: " + this.getDistanceString(this.currentAccumulatedDistance);
	}
	
	/**
	 * Formats starting phrase depending on street type and street angles
	 * @param current
	 * @param next
	 * @param isCommand
	 * @return formated starting phrase
	 */
	private String getStartingPhrase(RouteSection current, RouteSection next, boolean isCommand) {
		/* Autobahnkreuz */
		if(current.getFirstPart().lsiclass == 35110 && next.getFirstPart().lsiclass == 35110) {
			return (isCommand) ? "am nächsten " : "Am nächsten ";
		}
		/* Auffahrt */
		if(current.getFirstPart().lsiclass != 35110 && next.getFirstPart().lsiclass == 35110) {
			return (isCommand) ? "an der " : "An der ";
		}
		/* Abfahrt */
		if(current.getFirstPart().lsiclass == 35110 && next.getFirstPart().lsiclass != 35110) {
			return (isCommand) ? "an der " : "An der ";
		}
		if(next.getFirstPart().lsiclass == 35161 
		   || next.getFirstPart().lsiclass == 35162 
		   || next.getFirstPart().lsiclass == 35163 ) {
			return (isCommand) ? "an der " : "An der ";
		}
		if(current.angleToNext < 5.0 && current.angleToNext >= 0.01) { 
			return (isCommand) ? "fahren Sie " : "Fahren Sie ";
		}
		if(current.angleToNext > -5.0 && current.angleToNext <= -0.01) {
			return (isCommand) ? "fahren Sie " : "Fahren Sie ";
		}
		return (isCommand) ? "biegen Sie " : "Biegen Sie ";
	}
	
	/**
	 * Formats crossing type depending on street type and crossing type
	 * @param current
	 * @param next
	 * @return formated crossing type
	 */
	private String getCrossingType(RouteSection current, RouteSection next) {
		boolean isTCrossing = current.getLastPart().isTCrossing;
		/* Autobahnkreuz */
		if(current.getFirstPart().lsiclass == 35110 && next.getFirstPart().lsiclass == 35110) {
			return "Autobahnkreuz fahren Sie ";
		}
		/* Auffahrt */
		if(current.getFirstPart().lsiclass != 35110 && next.getFirstPart().lsiclass == 35110) {
			return "nächsten Auffahrt fahren Sie ";
		}
		if(current.getFirstPart().lsiclass != 35110 && (next.getFirstPart().lsiclass == 35161
				   || next.getFirstPart().lsiclass == 35162
				   || next.getFirstPart().lsiclass == 35163)) {
			return "nächsten Auffahrt fahren Sie ";
		}
		/* Abfahrt */
		if(current.getFirstPart().lsiclass == 35110 && next.getFirstPart().lsiclass != 35110) {
			return "nächsten Abfahrt fahren Sie ";
		}
		if(current.getFirstPart().lsiclass == 35110 && (next.getFirstPart().lsiclass == 35161
													   || next.getFirstPart().lsiclass == 35162
													   || next.getFirstPart().lsiclass == 35163)) {
			return "nächsten Abfahrt fahren Sie ";
		}
		
		
		
		if(isTCrossing) {
			return "an der nächsten T-Kreuzung ";
		} else {
			return "an der nächsten Kreuzung ";
		}
	}
	
	/**
	 * Formats turning direction and weight with the help of the calculated street angles 
	 * @param current
	 * @param next
	 * @return formated direction
	 */
	private String getDirection(RouteSection current, RouteSection next) {
		String dir = "";
		double angle = current.angleToNext;

		if(next.getFirstPart().lsiclass == 35161 ||
		   next.getFirstPart().lsiclass == 35162 ||
		   next.getFirstPart().lsiclass == 35163) { 
			if(angle > 5.0) {
				dir = "links weiter ";
			} else if(angle < -5.0) {
				dir = "rechts weiter ";
			} else {
				dir = "gerade aus weiter ";
			}
		}

		if(angle > 5.0) {
			dir = "links ab ";
		} else if(angle < -5.0) {
			dir = "rechts ab ";
		} else {
			dir = "gerade aus weiter ";
		}
		
		String weight = "";
		if(next.getFirstPart().lsiclass != 35110 || next.getFirstPart().lsiclass != 35161
												 || next.getFirstPart().lsiclass == 35162
												 || next.getFirstPart().lsiclass == 35163) {
			double normalizedAngle = Math.abs(angle);
			if(normalizedAngle > 5.0 && normalizedAngle <= 45.0) {
				weight = "halb ";
			} else if(normalizedAngle > 45.0 && normalizedAngle <= 135.0) {
				weight = "";
			} else if(normalizedAngle > 135 && normalizedAngle <= 180) {
				weight = "scharf ";
			}
		}
		
		return weight + dir;
	}
	
	/**
	 * Formats street name
	 * @param next
	 * @return formated street name
	 */
	private String getStreetName(RouteSection next, RouteSection nextOfNext) {
		String nextOfNextStreet = "";
		if(nextOfNext != null) {
			nextOfNextStreet = nextOfNext.getFirstPart().realname;
		}
		if(next.getFirstPart().realname.matches("\\b(\\w+)_(\\d+)\\b")) {
			if(next.getFirstPart().lsiclass == 35141) return "auf die Innerortstrasse. ";
			if(next.getFirstPart().lsiclass == 35134 
			   || next.getFirstPart().lsiclass == 35133 
			   || next.getFirstPart().lsiclass == 35132
			   || next.getFirstPart().lsiclass == 35131
			   || next.getFirstPart().lsiclass == 35130) return "auf die Landstrasse. ";
			if(next.getFirstPart().lsiclass == 35163
			   || next.getFirstPart().lsiclass == 35162
			   || next.getFirstPart().lsiclass == 35161) {
				if(next.getFirstPart().realname.matches("\\b(\\w+)_(\\d+)\\b")) {
					String temp = next.getFirstPart().realname.split("_")[0];
					temp = temp.substring(0,1).toUpperCase() + temp.substring(1);
					return "auf die " + temp + ". ";
				} else {
					return "auf die Anschlussstelle " + nextOfNextStreet + "."; // + " (" + next.getFirstPart().realname + "/"  + next.getFirstPart().lsiclass + "/" + next.getFirstPart().lsidescription + ")";
				}
			}
			if(next.getFirstPart().lsiclass == 35110) return "auf die Autobahn. ";
			if(next.getFirstPart().lsiclass == 35165) return "auf den Kreisverkehr. ";
			if(next.getFirstPart().lsiclass == 35120) return "auf die Kraftfahrstrasse. ";
			if(next.getFirstPart().lsiclass == 35131) return "auf die Bundesstrasse. ";
		}
		if(next.getFirstPart().realname.matches("\\bautobahnanschlussstelle_\\d*\\b")) {
			return "auf die Autobahn. "; // + " (" + next.getFirstPart().realname + "/"  + next.getFirstPart().lsiclass + "/" + next.getFirstPart().lsidescription + ")";
		}
		return "auf " + next.getFirstPart().realname + ". "; // + "(" + next.getFirstPart().lsiclass + "/" + next.getFirstPart().lsidescription + ")";
	}
	
	/**
	 * Formats distance string + street name
	 * @param distance
	 * @param cur
	 * @return formated distance string + street name
	 */
	private String getDistanceStringWithStreet(double distance, RouteSection cur, RouteSection nextOfNext) {
		String nextOfNextStreet = "";
		if(nextOfNext != null) {
			nextOfNextStreet = nextOfNext.getFirstPart().realname;
		}
		String str = "Fahren Sie ";
		str += this.getDistanceString(distance);
		if(cur.getFirstPart().realname.matches("\\b(\\w+)_(\\d+)\\b")) {
			if(cur.getFirstPart().lsiclass == 35141) str += "auf der Innerortstrasse. \n";
			if(cur.getFirstPart().lsiclass == 35134 
			   || cur.getFirstPart().lsiclass == 35133 
			   || cur.getFirstPart().lsiclass == 35132
			   || cur.getFirstPart().lsiclass == 35131
			   || cur.getFirstPart().lsiclass == 35130) str += "auf der Landstrasse. \n";
			if(cur.getFirstPart().lsiclass == 35163
			   || cur.getFirstPart().lsiclass == 35162
			   || cur.getFirstPart().lsiclass == 35161) {
				if(cur.getFirstPart().realname.matches("\\b(\\w+)_(\\d+)\\b")) {
					String temp = cur.getFirstPart().realname.split("_")[0];
					temp = temp.substring(0,1).toUpperCase() + temp.substring(1);
					str += "auf die " + temp + " \n";
				} else {
					str += "auf der Anschlussstelle " + nextOfNextStreet + " \n";
				}
				
			}
			if(cur.getFirstPart().lsiclass == 35110) str += "auf der Autobahn. \n";
			if(cur.getFirstPart().lsiclass == 35165) str += "auf dem Kreisverkehr. \n";
			if(cur.getFirstPart().lsiclass == 35120) str += "auf der Kraftfahrstrasse. \n";
			if(cur.getFirstPart().lsiclass == 35131) str += "auf der Bundesstrasse. \n";
		} else {
			if(cur.getLastPart().realname.matches("\\bautobahnanschlussstelle_\\d*\\b")) {
				str += "auf der Autobahn.\n";
			} else {
				str += "auf der " + cur.getLastPart().realname + ".\n";
			}
		}

		return str;
	}
	
	/**
	 * Formats distance string
	 * @param distance
	 * @return formated distance string
	 */
	private String getDistanceString(double distance) {
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#0.0", dfs);
		if(distance >= 1000.0) {
			return String.valueOf(df.format(distance / 1000.0)) + " km ";
		}
		DecimalFormat df2 = new DecimalFormat("#0", dfs);
		return String.valueOf(df2.format(distance)) + " m ";
	}
}