/***********************************************
 * created on 		08.12.2004 (structure)
 * last modified: 	17.12.2004 (final)
 * 					19.05.2005 (change: check minPoints for segments and Lines)
 *					21.05.2005 (split long lines)
 *					21.07.2005 (random split position)					
 * 
 * author:			sstein
 * 
 * description:
 *  Smoothes a given line using a snakes algorithm. 
 *  This can be done segmentwise if break points will be given.
 * 	The class uses class snakesSmoothingSegment
 * 
 * TODO: interpolation of equidistant vertices with distance of maximal point displacement
 ***********************************************/
package ch.unizh.geo.algorithms.snakes;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @description:
 *  Smoothes a given line using a snakes algorithm. 
 *  This can be done segmentwise if break points will be given.
 * 	The class uses class snakesSmoothingSegment
 * 
 * @author sstein
 * @author matthias (bug fixes)
 * 
 */
public class SnakesSmoothingLine {

	private final boolean doSegmentation;
	private double alpha = 1;
	private double beta = 1; 
	private int iterations = 1;
	private int[] segmentationIndizes = null;
	private LineString inputLine = null;
	private LineString smoothedLine = null;
	private LineString[] segmentList = null;
	private int minPoints = 5;		//--necessary points to calculate a smoothing (at least 5 points are necessary
	//  for snakes, but since points are mirrored it can be also only 3)
	private int maxPoints = 100;	//used for segmentation, which is necessary to avoid too big matrices
	private double maxDistTreshold = 0;
	private boolean couldNotSmooth = false;
//	private int segmentPointIdx = 0; 

//	// -------------- constructors : begin ---------------------- 
//	/**
//	 * Smoothes a line with the given parameters. Either as complete line
//	 * or segmentwise if a list of cut point indizes is given.
//	 * @param line to smooth
//	 * @param iterations
//	 * @param alpha snakes parameter (should be one)
//	 * @param beta snakes parameter (should be one)
//	 * @param doSegmentation boolean value
//	 * @param segmentationIndizes list of point indizes there the line has to be cutted
//	 *                            without first and last line point
//	 */
//	public SnakesSmoothingLine(LineString line, int iterations, 
//			double alpha, double beta, 
//			boolean doSegmentation, int[] segmentationIndizes){
//
//		this.inputLine = line;
//		this.iterations = iterations;
//		this.alpha = alpha;
//		this.beta = beta;        
//		this.doSegmentation = doSegmentation;
//		this.segmentationIndizes = segmentationIndizes;
//		if ((segmentationIndizes == null) || (segmentationIndizes.length == 0)){
//			this.doSegmentation = false;
//		}        
//		this.solveIter();        
//	}

//	/**
//	 * Smoothes a line with the given parameters.    
//	 * @param line to smooth
//	 * @param iterations
//	 * @param alpha snakes parameter (should be one)
//	 * @param beta snakes parameter (should be one)
//	 */
//	public SnakesSmoothingLine(LineString line, int iterations, 
//			double alpha, double beta){
//
//		this.inputLine = line;
//		this.iterations = iterations;
//		this.alpha = alpha;
//		this.beta = beta;        
//		this.doSegmentation = false;
//
//		this.solveIter();
//	}

	/**
	 * Smoothes a line with the given parameters. Either as complete line
	 * or segmentwise if a list of cut point indizes is given.
	 * @param line to smooth
	 * @param maximum distance of point displacement
	 * @param doSegmentation boolean value
	 * @param segmentationIndizes list of point indizes there the line has to be cutted
	 *                            without first and last line point
	 */
	public SnakesSmoothingLine(LineString line, double maxDist, 
			double alpha, double beta, int[] segmentationIndizes){

		this.inputLine = line;
		this.maxDistTreshold = maxDist;
		this.iterations = 1;
		this.alpha = alpha;
		this.beta = beta;
		this.segmentationIndizes = segmentationIndizes;
		
		this.doSegmentation = (segmentationIndizes != null) && (segmentationIndizes.length != 0);
		
		this.solveMaxDisp();
	}

	/**
	 * Smoothes a line with the given parameters.    
	 * @param line to smooth
	 * @param maximum distance of point displacement
	 */
	public SnakesSmoothingLine(LineString line, double maxDist,double alpha, double beta){

		this.inputLine = line;
		this.maxDistTreshold = maxDist;        
		this.alpha = alpha;
		this.beta = beta;        
		this.doSegmentation = false;

		this.solveMaxDisp();
	}

	// -------------- constructors : end ----------------------

//	/**
//	 * used for iterative solution by a given maximum number of iteration<p> 
//	 * decides between the cases of segmentation and no segmentation. <p>
//	 * calls smoothLine() .. which itself calls class SnakesSmoothingSegment 
//	 */
//	private void solveIter(){
//		System.out.println("SnakesSmoothingsLine.solveIter: border correction in SnakesSmoothSegment() only for end points");
//		//------------------
//		// with segmentation 
//		//------------------ 
//		if(this.doSegmentation == true){
//			this.segmentList = SplitLineString.splitInSegments(this.inputLine,this.segmentationIndizes);            
//			int n = this.segmentationIndizes.length +1;
//			LineString[] smoothedSegments = new LineString[n];
//			//-- smooth the single segments
//			for (int i = 0; i < n; i++) {             
//				LineString lineSegment = this.segmentList[i];
//				//--check nr of points first (min. is 3 according to SnakesSmoothingSegment)
//				//  and not bigger than maxPoints to avoid big matrices
//				if ((lineSegment.getNumPoints() >= this.minPoints)&& 
//						(lineSegment.getNumPoints() < this.maxPoints)){
//					LineString newSegment = this.smoothLine(lineSegment);
//					smoothedSegments[i] = (LineString)newSegment.clone();
//				}
//				else if (lineSegment.getNumPoints() < this.minPoints){ 
//					//--if to short, use orginal points
//					smoothedSegments[i] = (LineString)lineSegment.clone();
//				}
//				else if(lineSegment.getNumPoints() >= this.maxPoints){
//					System.out.println("SnakesSmoothingLine.maxDisp: Do further segmentation since segment has to much vertices!!!");
//					//-- get segmentation points
//					int[] pointidx = getSegmentationPointsForLongLines(this.inputLine, this.maxPoints);
//					//-- do smoothing by use of the class itself .. use one iteration instaed maxDist threshold 
//					SnakesSmoothingLine ssl = new SnakesSmoothingLine(lineSegment, 1, 
//							this.alpha, this.beta, pointidx);
//					smoothedSegments[i] = ssl.getSmoothedLine();
//				}
//				else{
//					System.out.println("SnakesSmoothingLine.maxDisp: Unhandled case for this number of vertices!!!");
//				}
//			}
//			this.smoothedLine = SplitLineString.concatSegements(smoothedSegments);
//		}
//		//------------------
//		// no segmentation 
//		//------------------        	
//		else{
//			System.out.println("no of points: " + this.inputLine.getNumPoints());
//			if((this.inputLine.getNumPoints() >= this.minPoints)  &&
//					(this.inputLine.getNumPoints() < this.maxPoints)){
//				this.smoothedLine = this.smoothLine(this.inputLine);
//			}
//			else if (this.inputLine.getNumPoints() < this.minPoints){ 
//				//-- if to short, use orginal points
//				this.smoothedLine = this.inputLine;
//				System.out.println("SnakesSmoothingLine.solveMaxDisp: line to short to smooth!");
//			}
//			else if(this.inputLine.getNumPoints() >= this.maxPoints){
//				System.out.println("SnakesSmoothingLine.maxDisp: Do segmentation since line is has to much vertices!!!");
//				//-- get segmentation points
//				int[] pointidx = getSegmentationPointsForLongLines(this.inputLine, this.maxPoints);
//				//-- do smoothing by use of the class itself
//				SnakesSmoothingLine ssl = new SnakesSmoothingLine(this.inputLine, 1, this.alpha, this.beta, pointidx);
//				this.smoothedLine = ssl.getSmoothedLine();
//			}
//			else{
//				System.out.println("SnakesSmoothingLine.maxDisp: Unhandled case for this number of vertices!!!");
//			}
//		}
//	}

	/**
	 * used for smoothing with maximum displacement of points..<p>
	 * checks if max displacement value has been exceed<p> 
	 * decides between the cases of segmentation and no segmentation. <p>
	 * calls smoothLine() .. if segments are smoothed<p>
	 * long lines are segmentated to avoid big matrices (see field maxPoints) 
	 */
	private void solveMaxDisp(){
		System.out.println("SnakesSmoothingsLine.solvemaxDisp: border correction in SnakesSmoothSegment() only for end points");
		LineString prevLine = null;
		double prevmaxDisp = 0; double deltamaxDisp =0; int u = 0;            
		if(this.doSegmentation == true){
			//---------
			// with segmentation
			//--------
			assert(this.segmentationIndizes[this.segmentationIndizes.length-1] < this.inputLine.getNumPoints());
			System.out.println(this.segmentationIndizes[this.segmentationIndizes.length-1]);
			System.out.println(this.inputLine.getNumPoints());
			this.segmentList = SplitLineString.splitInSegments(this.inputLine,this.segmentationIndizes);            
			int n = this.segmentationIndizes.length +1;
			LineString[] smoothedSegments = new LineString[n];
			boolean proceed = true; int j=1;            
			LineString[] segments = this.segmentList;
			prevLine=inputLine;
			while(proceed){            
				//-- smooth the single segments
				if (j >1){
					segments = (LineString[])smoothedSegments.clone();
				}
				for (int i = 0; i < n; i++) {             
					LineString lineSegment = segments[i];
					int noOfVertices = lineSegment.getNumPoints(); 
					//--check nr of points first (min. is 3 according to SnakesSmoothingSegment)
					//  and not bigger than maxPoints to avoid big matrices
					if ((noOfVertices >= this.minPoints)&& 
							(lineSegment.getNumPoints() <= this.maxPoints)){
						LineString newSegment = this.smoothLine(lineSegment);
						smoothedSegments[i] = (LineString)newSegment.clone();
					}
					else if (noOfVertices < this.minPoints){ 
						//--if to short, use orginal points
						smoothedSegments[i] = (LineString)lineSegment.clone();
					}
					else if(noOfVertices > this.maxPoints){
						System.out.println("SnakesSmoothingLine.maxDisp: Do further segmentation since segment has to much vertices!!!");
						//-- get segmentation points
						int[] pointidx = getSegmentationPointsForLongLines(this.inputLine, this.maxPoints);
						//-- do smoothing by use of the class itself .. use one iteration instaed maxDist threshold 
						SnakesSmoothingLine ssl = new SnakesSmoothingLine(lineSegment, 1, this.alpha, this.beta, pointidx);
						smoothedSegments[i] = ssl.getSmoothedLine();
					}
					else{
						System.out.println("SnakesSmoothingLine.maxDisp: Unhandled case for this number of vertices!!!");
					}

				}
				//-- concat segments and check if smoothed too much
				this.smoothedLine = SplitLineString.concatSegements(smoothedSegments);
				double dist = this.getMaxPointDisplacement(this.inputLine, this.smoothedLine);
				System.out.println("SnakesSmoothingLine.solveMaxDisp - Loop: " + j + " maxDisp = " 
						+ dist + " treshold = " + this.maxDistTreshold );               
				if(dist > this.maxDistTreshold){
					if (j == 1){
						u=u+1;
						System.out.println("SnakesSmoothingLine.solveMaxDisp: reset alpha, beta, segments");
						this.alpha = this.alpha /2;
						this.beta = this.beta/2;
						System.out.println("alpha new: " + this.alpha + " beta new: " + this.beta);
						segments = this.segmentList;
						j = 0;   
						if (u==10){proceed = false; this.couldNotSmooth = true;} //notbremse
					}
					else{
						proceed = false;
						this.smoothedLine = prevLine;
					}
				}
				else{
					prevLine = this.smoothedLine;
					deltamaxDisp = (dist - prevmaxDisp)/dist;
					if (deltamaxDisp < 0.01){ 
						//--stop if changes are smaller than 1 percent
						proceed = false;
						this.smoothedLine = prevLine;
					}
					prevmaxDisp = dist; 
				}
				j++;
			}
		}
		//---------
		// no segmentation
		//--------        
		else{
			//check nr of points first (min. is 3 according to SnakesSmoothingSegment)
			System.out.println("no of points: " + this.inputLine.getNumPoints());
			if ((this.inputLine.getNumPoints() >= this.minPoints) &&
					(this.inputLine.getNumPoints() < this.maxPoints)){                
				//--initialize
				SnakesSmoothingSegment mySnake = new SnakesSmoothingSegment();
				mySnake.initialise(this.inputLine,this.alpha,this.beta);
				//mySnake.setCorrectBorderPoints(false);
				//--smooth
				boolean proceed = true; int j=1;
				prevLine = this.inputLine;
				while(proceed== true){
					mySnake.solve();
					double dist = this.getMaxPointDisplacement(this.inputLine, mySnake.getSmoothedSegment());
					System.out.println("SnakesSmoothingLine.solveMaxDisp - Loop: " + j + " maxDisp = " 
							+ dist + " treshold = " + this.maxDistTreshold );               
					if(dist > this.maxDistTreshold){
						if (j == 1){
							u=u+1;
							System.out.println("SnakesSmoothingLine.solveMaxDisp: reset alpha, beta, line");
							this.alpha = this.alpha /2;
							this.beta = this.beta/2;
							System.out.println("alpha new: " + this.alpha + " beta new: " + this.beta);
							mySnake.initialise(this.inputLine,this.alpha,this.beta);                        
							j = 0;
							if (u==10){proceed = false; this.couldNotSmooth = true;} //notbremse
						}
						else{
							proceed = false;
							this.smoothedLine = prevLine;
						}
					}
					else{
						prevLine = mySnake.getSmoothedSegment();
						deltamaxDisp = (dist - prevmaxDisp)/dist;
						if (deltamaxDisp < 0.01){ 
							//stop if changes smaller than 1 percent
							proceed = false;
							this.smoothedLine = prevLine;
						}
						prevmaxDisp = dist;                     
					}
					j++;
				} //end while
			}
			else if (this.inputLine.getNumPoints() < this.minPoints){ 
				//-- if to short, use orginal points
				this.smoothedLine = this.inputLine;
				System.out.println("SnakesSmoothingLine.solveMaxDisp: line to short to smooth!");
			}
			// do segmentation if line contains more than maxPoints
			else if(this.inputLine.getNumPoints() >= this.maxPoints){
				System.out.println("SnakesSmoothingLine.maxDisp: Do segmentation since line is has to much vertices!!!");
				//-- get segmentation points
				int[] pointidx = getSegmentationPointsForLongLines(inputLine, this.maxPoints);
				//-- do smoothing by use of the class itself
				SnakesSmoothingLine ssl = new SnakesSmoothingLine(this.inputLine, this.maxDistTreshold, this.alpha, this.beta, pointidx);
				this.smoothedLine = ssl.getSmoothedLine();
			}
			else{
				System.out.println("SnakesSmoothingLine.maxDisp: Unhandled case for this number of vertices!!!");
			}
		}//end else segmentation
	}

	/**
	 * calls SnakesSmoothingSegment class to smooth the line
	 * @param line
	 */
	private LineString smoothLine(LineString line){        
		//initialize
		SnakesSmoothingSegment mySnake = new SnakesSmoothingSegment();
		mySnake.initialise(line,this.alpha,this.beta);
		//mySnake.setCorrectBorderPoints(false);
		for (int i = 0; i < this.iterations; i++) {
			mySnake.solve();
		}        
		return mySnake.getSmoothedSegment();
	}

	private double getMaxPointDisplacement(LineString lineOriginal, LineString lineNew){
		double maxdist = 0;
		Coordinate[] coordsOrg = lineOriginal.getCoordinates();
		Coordinate[] coordsNew = lineNew.getCoordinates();
		double dx, dy, s;
		for(int i=0; i < coordsOrg.length; i++){
			/**
            //-- point-point distance is not so good, since for snakes lateral movements appear 
            //   (not perpendicular to line direction like for tafus)
            dx= coordsOrg[i].x - coordsNew[i].x;
            dy= coordsOrg[i].y - coordsNew[i].y;
            s = dx*dx + dy*dy;
			 **/
			Point pt = new GeometryFactory().createPoint(coordsNew[i]);
			s = pt.distance(lineOriginal);
			if (s > maxdist){
				maxdist = s;
			}
		}
		return maxdist;
	}

	/**
	 * calculates the segementation poinnts for too long lines
	 * @param line
	 * @return pointidx
	 */
	private static int[] getSegmentationPointsForLongLines(LineString line, int maxPoints){    	
		//-- calculate random split index = maxpoint +/- x	: x=<5;
//		this.segmentPointIdx = this.maxPoints + (int)Math.ceil((Math.random()-0.5)*10);
		int parts = 1 + (int)Math.ceil(line.getNumPoints()/maxPoints);
		int[] pointidx = new int[parts-1];
		int delta = (int)Math.ceil(line.getNumPoints() / parts);
		System.out.println("delta: "+ delta);
		System.out.print("Segmentation point indices for to long lines: ");
		for (int j = 0; j < pointidx.length; j++) {
			pointidx[j]=delta*(j+1);
			System.out.print(pointidx[j] + "  ");
		}
		System.out.println(" ");
		return pointidx;
	}

	/******************* getters and setters ************/

	public LineString getSmoothedLine() {
		return smoothedLine;
	}
	public boolean isCouldNotSmooth() {
		return couldNotSmooth;
	}
//	/**
//	 * @return Returns the maxPoints.
//	 * 		 maxPoints defines a line segmentation criterion
//	 * 		 to avoid too big matrices. 
//	 * 
//	 */
//	public int getMaxPoints() {
//		return maxPoints;
//	}
//	/**
//	 * @param maxPoints The maxPoints to set.
//	 * 		 maxPoints defines a line segmentation criterion
//	 * 		 to avoid too big matrices 
//	 */
//	public void setMaxPoints(int maxPoints) {
//		this.maxPoints = maxPoints;
//	}
//	/**
//	 * @return Returns the minPoints.
//	 * necessary number of line vertices to do a smoothing
//	 * with snakes technique 
//	 * 
//	 */
//	public int getMinPoints() {
//		return minPoints;
//	}
//	/**
//	 * necessary number of line vertices to do a smoothing
//	 * with snakes technique 
//	 * @param minPoints The minPoints to set.
//	 */
//	public void setMinPoints(int minPoints) {
//		this.minPoints = minPoints;
//	}
}
