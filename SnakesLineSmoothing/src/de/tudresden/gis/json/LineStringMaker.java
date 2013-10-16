package de.tudresden.gis.json;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author matthias
 *
 */
public class LineStringMaker implements IGeometryMaker{
	private double[] coordBuffer = new double[3];
	private int coordIdx = 0;
	private boolean lineArrayStarted = false;
	private boolean coordsArrayStarted = false;
	private ArrayList<Coordinate> vertices = new ArrayList<Coordinate>();
	private boolean completed = false;
	
	public LineStringMaker(){
		super();
	}
	
	public void startArray(){
		if (lineArrayStarted){
			coordsArrayStarted = true;
		} else {
			lineArrayStarted = true;
		}
	}
	
	public void endArray(){
		if (coordsArrayStarted){
			coordsArrayStarted = false;
			// make coordinate 2D case
			if (coordIdx == 2){
				vertices.add(new Coordinate(coordBuffer[0], coordBuffer[1]));
			}
			// make coordinate 3D case
			if (coordIdx == 3){
				vertices.add(new Coordinate(coordBuffer[0], coordBuffer[1], coordBuffer[2]));
			}
			// Reset coord idx
			coordIdx = 0;
		} else {
			lineArrayStarted = false;
			// this would be the last action
			completed = true;
		}
	}
	
	public void addDecimal(double decimal){
		coordBuffer[coordIdx] = decimal;
		coordIdx++;
	}
	
	public Geometry getGeometry() {
		return IGeometryMaker.gf.createLineString(vertices.toArray(new Coordinate[vertices.size()]));
	}
	
	public boolean isComplete() {
		return completed;
	}
}
