package project.classes;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
 * The FileLoader reads the link.txt and pos.txt.
 */
public class FileLoader {
	/**
	 * Read link list file from file system
	 * @param path to link.txt file
	 * @return array of links
	 * @throws IOException
	 */
	public int[] readLinks(String path) throws IOException {
		BufferedReader fileIn = new BufferedReader(new FileReader(path));
		List<String> links = new ArrayList<String>();
		String line;
		while((line = fileIn.readLine()) != null)
			links.add(line);
		fileIn.close();
		return this.copyListToArray(links);
	}
	
	/**
	 * Read position list file from file system
	 * @param path to pos.txt
	 * @return list of positions
	 * @throws IOException
	 */
	public List<LatLon> readPositions(String path) throws IOException {
		BufferedReader fileIn = new BufferedReader(new FileReader(path));
		List<LatLon> positions = new ArrayList<LatLon>();
		String line;
		while((line = fileIn.readLine()) != null) {
			String[] temp = line.split(",");
			positions.add(new LatLon(Double.parseDouble(temp[1]), Double.parseDouble(temp[0])));
		}
		fileIn.close();
		return positions;
	}
	
	/**
	 * Copies lists to arrays
	 * @param list
	 * @return integer array of list items
	 */
	private int[] copyListToArray(List<String> list) {
		int size = list.size();
		int[] links = new int[size];
		for(int i = 0; i < size; i++) {
			links[i] = Integer.parseInt(list.get(i));
		}
		return links;
	}
}
