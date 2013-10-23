package de.tudresden.gis.algorithm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;

import ch.unizh.geo.algorithms.snakes.SnakesSmoothingLineNew;

import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
//import com.vividsolutions.jts.io.ParseException;
//import com.vividsolutions.jts.io.WKTReader;


import de.tudresden.gis.json.IGeometryHandler;
import de.tudresden.gis.json.JsonProcessor;


/**
 * 
 * @author matthias
 *
 */
public class SnakeLineSmoothing implements IGeometryHandler{

//	private static GeometryFactory gf = new GeometryFactory();
	
	private final double maxDisplacement;
	
	public SnakeLineSmoothing(double maxDisplacement){
		this.maxDisplacement = maxDisplacement;
	}

	public static LineString SmoothLine(LineString line, double maxVertexDisplacement){
		SnakesSmoothingLineNew ssl = new SnakesSmoothingLineNew(line, maxVertexDisplacement, 1.0, 1.0);
		// return original line if smoothing could not be done
		if(ssl.isCouldNotSmooth()){
			return line;
		}
		return ssl.getSmoothedLine();
	}

	@Override
	public Geometry handleGeometry(Geometry geom) {
		return SmoothLine((LineString)geom, maxDisplacement);
	}
	
	public static void main(String[] args) throws IOException {
		String inFileName = args[0];
		String outFileName = args[1];
		double tolerance = Double.parseDouble(args[2]);
//		String inFileName = "data/route.json";
//		String outFileName = "data/route_out.json";
//		double tolerance = 0.05;

		File inFile = new File(inFileName);
		File outFile = new File(outFileName);
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inFile));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
		
//		PrintStream k = System.out;
		
		IGeometryHandler snakeHandler = new SnakeLineSmoothing(tolerance);
		
		JsonProcessor.processGeoJSON(bis, bos, snakeHandler);
		
		bos.close();
		bis.close();
		
	}
	
	
//	public static void processWKTFile(File inFile, File outFile, double tolerance) throws IOException{
//	BufferedReader br = new BufferedReader(new FileReader(inFile));
//	BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
//	WKTReader reader = new WKTReader(gf); 
//    String line;
//    LineString ls = null;
//    while((line = br.readLine()) != null){
//    	try{
//    		ls = (LineString) reader.read(line); 
//    		LineString smoothLS = SmoothLine(ls, tolerance);
//    		bw.write(smoothLS.toText());
//    		bw.newLine();
//    	} catch (ParseException e){
//    		System.err.println("Could not parse LineString geometry");
//    		continue;
//    	}
//    }
//    bw.close();
//    br.close();
//}

//public static void main(String[] args) throws IOException {
////	String inFileName = args[1];
////	String outFileName = args[2];
////	double tolerance = Double.parseDouble(args[3]);
//	double tolerance = 0.5;
//	String inFileName = "data/source.txt";
//	String outFileName = "data/smooth" + tolerance + ".txt";
//
//	File inFile = new File(inFileName);
//	File outFile = new File(outFileName);
//
//	processWKTFile(inFile, outFile, tolerance);
//
//}

}
