package project.classes;
/**
 * Represents one command for a projected point.
 */
public class CurrentCommand {
    public String command;       // Abbiegekommando oder null
    public double projectedLat;  // Projektion der mitgelieferten Position auf die Stra�engeometrie (latitude)
    public double projectedLon;  // Projektion der mitgelieferten Position auf die Stra�engeometrie (longitude)
    public double meters;        // Gefahrene Meter seit der letzten (projizierten) Position
}