package de.tudresden.gis.json;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * 
 * @author matthias
 *
 */
public class JtsToGeoJsonCoordinates {
	private static String ARRAY_START = "[";
	private static String ARRAY_END = "]";
	private static String ELEMENT_SEP = ",";
	
	public static final String create(LineString ls){
		StringBuffer sb = new StringBuffer();
		sb.append(ARRAY_START);
		
		for (Coordinate coord : ls.getCoordinates()){
			// start coordinate
			sb.append(ARRAY_START);
			sb.append(coord.x);
			sb.append(ELEMENT_SEP);
			sb.append(coord.y);
			
			if (!Double.isNaN(coord.z)){
				sb.append(ELEMENT_SEP);
				sb.append(coord.z);
			}
			
			// end coordinate and separator for next coodinate array
			sb.append(ARRAY_END);
			sb.append(ELEMENT_SEP);
		}
		// remove last ELEMENT_SEP
		sb.setLength(sb.length()-1);
		
		sb.append(ARRAY_END);
		return sb.toString();
	}
}
