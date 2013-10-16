package de.tudresden.gis.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.unizh.geo.algorithms.snakes.SnakesSmoothingLine;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.vividsolutions.jts.geom.LineString;


public class JsonProcessor {
	
	private static final String TYPE_CODE = "type";
	private static final String LINE_STRING_CODE = "LineString";
	private static final String COORDINATES_CODE = "coordinates";
	
	public static LineString SmoothLine(LineString line, double maxVertexDisplacement){
		SnakesSmoothingLine ssl = new SnakesSmoothingLine(line, maxVertexDisplacement, 1.0, 1.0);
		return ssl.getSmoothedLine();
	}

	public static void processGeoJSON(InputStream is, OutputStream os, IGeometryHandler gh) throws IOException{
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser parser = jsonFactory.createParser(is);
		JsonGenerator generator = jsonFactory.createGenerator(os);
		
		
		JsonToken token;
		boolean typePresent = false;
		boolean lineStringPresent = false;
		LineStringMaker geometryMaker = null;
		
		String currentFieldName = "";
		while ( (token = parser.nextToken()) != null){
			
			// if geometry maker is complete, process contents and
			// write result before continuing to parse next token
			if (geometryMaker != null && geometryMaker.isComplete()){
				LineString resultGeom = (LineString) gh.handleGeometry(geometryMaker.getGeometry());
				String resultString = JtsToGeoJsonCoordinates.create(resultGeom);
				generator.writeRawValue(resultString);
				
				// reset all GeoJson related elements
				typePresent = false;
				lineStringPresent = false;
				geometryMaker = null;
			}
			
			switch (token){
				case START_ARRAY:
					if (geometryMaker != null){
						geometryMaker.startArray();
					} else {
						generator.writeStartArray();
					}
//					System.out.println("Read object: " + parser.getText());
					break;
				case START_OBJECT:
//					System.out.println("Read object: " + parser.getText());
					generator.writeStartObject();
					break;
				case FIELD_NAME: // Field name = key
					// Field names indicate special element in GeoJson
					currentFieldName = parser.getText();
					if (currentFieldName.equalsIgnoreCase(TYPE_CODE)){
						typePresent = true;
					}
					if (typePresent && lineStringPresent && currentFieldName.equalsIgnoreCase(COORDINATES_CODE)){
						// create new LineString maker
						geometryMaker = new LineStringMaker();
					}
					
//					System.out.println("Read object: " + parser.getText());
					generator.writeFieldName(parser.getText());
					break;
				case VALUE_NULL:
//					System.out.println("Read object: " + parser.getText());
					generator.writeNull();
					break;
				case VALUE_TRUE:
//					System.out.println("Read object: " + parser.getText());
					generator.writeBoolean(true);
					break;
				case VALUE_FALSE:
//					System.out.println("Read object: " + parser.getText());
					generator.writeBoolean(false);
					break;
				case VALUE_NUMBER_INT:
					// INT could be a coordinate part without a decimal point
//					System.out.println("Read object: " + parser.getText());
					if (geometryMaker != null){
						geometryMaker.addDecimal(Double.parseDouble(parser.getText()));;
					} else {
						generator.writeRawValue(parser.getValueAsString());
					}
					break;
				case VALUE_NUMBER_FLOAT:
					// FLOAT could be a coordinate part
//					System.out.println("Read object: " + parser.getText());
					if (geometryMaker != null){
						geometryMaker.addDecimal(Double.parseDouble(parser.getText()));;
					} else {
						generator.writeRawValue(parser.getValueAsString());
					}
					break;
				case VALUE_STRING:
					if (typePresent && parser.getText().equalsIgnoreCase(LINE_STRING_CODE)){
						lineStringPresent = true;
					}
//					System.out.println("Read object: " + parser.getText());
					generator.writeString(parser.getValueAsString());
					break;
				case END_OBJECT:	
//					System.out.println("Read object: " + parser.getText());
					generator.writeEndObject();
					break;
				case END_ARRAY:
					if (geometryMaker != null){
						geometryMaker.endArray();
					} else {
						generator.writeEndArray();
					}
//					System.out.println("Read object: " + parser.getText());
					break;
				
				case VALUE_EMBEDDED_OBJECT:
//					System.out.println("Read object: " + parser.getText());
					generator.writeObject(parser.getEmbeddedObject());
					break;
					
				case NOT_AVAILABLE:
//					System.out.println("Read object: " + parser.getText());
					continue;
			}			
		}
		
		generator.close();
		parser.close();
		
	}

//	public static void main(String[] args) throws IOException {
////		String inFileName = args[1];
////		String outFileName = args[2];
////		double tolerance = Double.parseDouble(args[3]);
//		String inFileName = "data/in.json";
//		String outFileName = "data/out.json";
//		double tolerance = 0.05;
//
//		File inFile = new File(inFileName);
//		File outFile = new File(outFileName);
//		
//		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inFile));
//		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
//		
////		PrintStream k = System.out;
//		
//		IGeometryHandler snakeHandler = new SnakeLineSmoothing(tolerance);
//		
//		processGeoJSON(bis, bos, snakeHandler);
//		
//		bos.close();
//		bis.close();
//		
//	}
}
