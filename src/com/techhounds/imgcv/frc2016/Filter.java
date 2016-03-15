package com.techhounds.imgcv.frc2016;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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
	
	//Abstract Methods
	
	public abstract Mat process(Mat srcImage); //used when calling the filter instance
	
	//Default Methods
	
	protected List<PolygonCv> findTargets(Mat inputImage) { //finds potential targets in an image
	    List<MatOfPoint> contours  = new ArrayList<>();   //list of objects in image
        List<PolygonCv>  targets   = new ArrayList<>();   //list of potential targets in image
	    Mat              hierarchy = new Mat();           //???
        PolygonCv  		 currentTarget;                   //placeholder
        
        Imgproc.findContours(inputImage, contours, hierarchy, 
        					 Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0; i < contours.size(); i++) {            
        	currentTarget = PolygonCv.fromContour(contours.get(i), polygonEpsilon); 
        	        	
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
        	   currentTarget.getMaxY()             < 720) {
        		
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
    
	//Static Methods
}
