package de.tudresden.gis.json;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public interface IGeometryMaker {
	
	static final GeometryFactory gf = new GeometryFactory();
	
	public void startArray();
	public void endArray();
	public void addDecimal(double decimal);
	public Geometry getGeometry();
	public boolean isComplete();
	
}
