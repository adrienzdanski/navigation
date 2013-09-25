package project.classes;

/**
 * Configuration Class
 * Specifies the database properties and the paths to the link and position files.
 */
public class Config {
    public static String dbhost = "geo.example.net";
    public static int dbport = 5432;
    public static String dbuser = "xxx";
    public static String dbpasswd = "xxx";
    public static String dbname = "xxx";
    
    public static String userDirectory = System.getProperty("user.dir");
//    public static String fileID = "8";
//    public static String pathToLinks = userDirectory + "/bin/data/links" + fileID + ".txt";
//    public static String pathToPositions = userDirectory + "/bin/data/pos" + fileID + ".txt";
    
    public static String pathToLinks = userDirectory + "/links.txt";
    public static String pathToPositions = userDirectory + "/pos.txt";
    
    public static boolean printCoordsForDorenda = false;
    public static double distanceToCommand = 1.0;
}
