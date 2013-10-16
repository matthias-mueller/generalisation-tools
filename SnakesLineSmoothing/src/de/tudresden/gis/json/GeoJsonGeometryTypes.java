package de.tudresden.gis.json;

public enum GeoJsonGeometryTypes {
	POINT("Point"),
	MULTIPOINT("MultiPoint"),
	LINESTRING("LineString"),
	MULTILINESTRING("MultiLineString"),
	POLYGON("Polygon"),
	MULTIPOLYGON("MultiPolygon");
	
	private final String code;
	
	private GeoJsonGeometryTypes (String code) {
		this.code = code;
	}
	
	public String code(){
		return code;
	}
}
