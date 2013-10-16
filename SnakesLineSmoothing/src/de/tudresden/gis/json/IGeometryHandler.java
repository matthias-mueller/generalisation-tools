package de.tudresden.gis.json;

import com.vividsolutions.jts.geom.Geometry;

public interface IGeometryHandler {
	public Geometry handleGeometry(Geometry geom);
}
