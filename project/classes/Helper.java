package project.classes;

import java.util.ArrayList;
import java.util.List;
/**
 * Helper class with common methods.
 */
public class Helper {
	
	/**
	 * Clones list of integers
	 * @param list
	 * @return cloned list of integers
	 */
	public static List<Integer> cloneListInt(List<Integer> list) {
	    List<Integer> clonedList = new ArrayList<Integer>(list.size());
	    for (Integer item : list) {
	        clonedList.add(new Integer(item));
	    }
	    return clonedList;
	}
	
	/**
	 * Clones list of doubles
	 * @param list
	 * @return cloned list of doubles
	 */
	public static List<Double> cloneListDouble(List<Double> list) {
	    List<Double> clonedList = new ArrayList<Double>(list.size());
	    for (Double item : list) {
	        clonedList.add(new Double(item));
	    }
	    return clonedList;
	}
}
