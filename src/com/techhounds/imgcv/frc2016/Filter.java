package com.techhounds.imgcv.frc2016;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.techhounds.imgcv.PolygonCv;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/*
 * This class is designed to be extended by several full featured child filters
 * It's purpose is to provide standard methods which, when given an image, can detect polygons
 * similar in shape and size to an ideal object
 * 
 * Color filtering should be done by the children filters
 * Polygon analysis should also be done by the children...ie, calculating things such as distance
 * or angle based on the location of the polygon in the image
 * 
 * This class will also provide static methods for drawing/rendering these polygons, bc
 * I'm just too lazy to make a different one...at this time, at least.
 */

public abstract class Filter {

	//Default Variables/Configs
	
	protected NetworkTable networkTable = null;
	protected double       frameCount   = 0; 
	
	//Child-instance settable configs
	
	protected double polygonEpsilon;

	protected double targetHeightMin;
	protected double targetWidthMin;
	protected double targetSidesMin;
	protected double targetRatioMin;
	protected double targetAreaMin; 
	
	protected double targetHeightMax;
	protected double targetWidthMax;
	protected double targetSidesMax;
	protected double targetRatioMax;
	protected double targetAreaMax;
	
	protected double targetHeightIdeal; protected double targetHeightWeight;
	protected double targetWidthIdeal;  protected double targetWidthWeight;
	protected double targetSidesIdeal;  protected double targetSidesWeight;
	protected double targetRatioIdeal;  protected double targetRatioWeight;
	protected double targetAreaIdeal;   protected double targetAreaWeight;
	
	protected boolean _Debug = true;
	protected double _HoleRight, _HoleLeft, _HoleBottom, _HoleTop;
	protected boolean _HoleCheckEnabled;
	//Abstract Methods
	
	public abstract Mat process(Mat srcImage); //used when calling the filter instance
	
	//Default Methods
	
	protected List<PolygonCv> findTargets(Mat inputImage) { //finds potential targets in an image
	    List<MatOfPoint> contours  = new ArrayList<>();   //list of objects in image
        List<PolygonCv>  targets   = new ArrayList<>();   //list of potential targets in image
	    Mat              hierarchy = new Mat();           //???
        PolygonCv  		 currentTarget;                   //placeholder
        
        // Get a copy of the Black & White image pixels before findContours messes up the values
        Mat bwOrig = inputImage.clone();
		enableHoleCheck(0, .5, .25, .25);
        
        Imgproc.findContours(inputImage, contours, hierarchy, 
        					 Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0; i < contours.size(); i++) {            
        	currentTarget = PolygonCv.fromContour(contours.get(i), polygonEpsilon); 
        	System.out.println(currentTarget.getBoundingArea());
        	
        	if(currentTarget.getHeight()              > targetHeightMin &&
        	   currentTarget.getWidth()               > targetWidthMin  &&
        	   currentTarget.size() 		          > targetSidesMin  && 
        	   currentTarget.getBoundingAspectRatio() > targetRatioMin  &&  
        	   currentTarget.getBoundingArea()        > targetAreaMin   &&
        	   
        	   currentTarget.getHeight()              < targetHeightMax &&
        	   currentTarget.getWidth()               < targetWidthMax  &&
        	   currentTarget.size()                   < targetSidesMax  &&
        	   currentTarget.getBoundingAspectRatio() < targetRatioMax  &&
        	   currentTarget.getBoundingArea()        < targetAreaMax   &&
        	   
        	   currentTarget.getMinY()             > 50 &&
        	   currentTarget.getMaxY()             < 720 &&
        	   holeCheck(bwOrig, currentTarget)) {
        		
        		targets.add(currentTarget); //if within range, add to list of potential targets
        	}
        }      
        return targets;
    }
	
    protected PolygonCv findBestTarget(List<PolygonCv> targetList) {
    	PolygonCv bestTarget = null;
    	PolygonCv currentTarget;
    	double    bestTargetValue = 0;
    	int       bestTargetIndex = 0;
    	
    	for(int i = 0; i < targetList.size(); i++) {
    		currentTarget = targetList.get(i);
    		
    		if(getTargetRating(currentTarget) > bestTargetValue)  { //if this is better than
    			bestTarget = currentTarget;                         //best so far
    			bestTargetIndex = i;                                //becomes new best
    		}                                                       
    	}
    	
    	//will cause issues if targetList is NULL! this shouldn't happen though
    	targetList.remove(bestTargetIndex); //removes the best target
    	
    	return bestTarget;
    }
    
    protected double getTargetRating(PolygonCv inputTarget) {
    	double targetRating = 1000000;
    	
    	targetRating -= targetHeightWeight 
    			* Math.abs(inputTarget.getHeight()              - targetHeightIdeal);
    	targetRating -= targetWidthWeight  
    			* Math.abs(inputTarget.getWidth()               - targetWidthIdeal);
    	targetRating -= targetSidesWeight  
    			* Math.abs(inputTarget.size()                   - targetSidesIdeal);
    	targetRating -= targetRatioWeight  
    			* Math.abs(inputTarget.getBoundingAspectRatio() - targetRatioIdeal);
    	targetRating -= targetAreaWeight 
    			* Math.abs(inputTarget.getBoundingArea()        - targetAreaIdeal);
    	
    	return targetRating;
    }
	
    public void setNetworkTable(NetworkTable nt) {
    	networkTable = nt;
    }
	/**
	 * Enable check to make sure there is a hole (all white, a.k.a. incorrect in color, pixels in binary
	 * image) at a certain location within each polygon's bounding box.
	 * 
	 * @param top
	 *            How far down from the top side of the polygon's bounding box
	 *            (ratio of height in range of [0, 1.0]).
	 * @param bot
	 *            How far up from the bottom side of the polygon's bounding box
	 *            (ratio of height in range of [0, 1.0]).
	 * @param left
	 *            How far in from the left side of the polygon's bounding box
	 *            (ratio of width in range of [0, 1.0]).
	 * @param right
	 *            How far in from the right side of the polygon's bounding box
	 *            (ratio of width in range of [0, 1.0]).
	 */
	public void enableHoleCheck(double top, double bot, double left,
			double right) {
		_HoleLeft = left;
		_HoleRight = right;
		_HoleTop = top;
		_HoleBottom = bot;
		_HoleCheckEnabled = true;
	}
	
	private boolean holeCheck(Mat binary, PolygonCv poly) {
		if (_HoleCheckEnabled != true) {
			// Just indicate things are OK if hole check has not been enabled.
			return true;
		}

		float h = poly.getHeight();
		float w = poly.getWidth();

		// Figure out bounds in image of the portion of the polygon's bounding
		// box that we want to check
		int begRow = (int) (poly.getMinY() + (_HoleTop * h));
		int endRow = (int) (poly.getMaxY() - (_HoleBottom * h));
		int begCol = (int) (poly.getMinX() + (_HoleLeft * w));
		int endCol = (int) (poly.getMaxX() - (_HoleRight * w));

		// NOTE: We want to grab the pixels from a "clean" binary image
		// (state of binary image before looking for contours/polygons
		// as they corrupt the data during processing)
		Mat bboxPixels = binary.submat(begRow, endRow, begCol, endCol);

		// Compute total number of white pixels possible
		int hBbox = bboxPixels.rows();
		int wBbox = bboxPixels.cols();
		double totalPixels = wBbox * hBbox;

		// See how many white pixels we actually have
		Scalar sum = Core.sumElems(bboxPixels);
		// White pixels have value of 255 in binary images (0xff)
		// Black pixels have a value of 0 (0x00)
		double whitePixels = sum.val[0] / 255;
		int percentWhite = (int) Math.round(100 * whitePixels / totalPixels);

		// Sanity check that all values are 0 or 1
		boolean allInRange = Core.checkRange(bboxPixels, true, 0, 2);
		if (_Debug) {
			System.out.println("Cutout Check: (" + wBbox + "x" + hBbox
					+ ")  total: " + totalPixels + "  white: " + whitePixels
					+ " (" + percentWhite + "%)  binary: " + allInRange);
		}

		// Found hole if none of the black/white pixels were black.
		return (percentWhite <= 10);
	}
	//Static Methods
}
