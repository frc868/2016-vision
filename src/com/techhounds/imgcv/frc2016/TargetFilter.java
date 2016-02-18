package com.techhounds.imgcv.frc2016;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import com.techhounds.imgcv.PolygonCv;
import com.techhounds.imgcv.filters.BlackWhite;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.ColorSpace;
import com.techhounds.imgcv.filters.CrossHair;
import com.techhounds.imgcv.filters.Dilate;
import com.techhounds.imgcv.filters.Erode;
import com.techhounds.imgcv.filters.GrayScale;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.utils.*;

/*
 * Constructor Codes:
 * 
 * 0: Do Nothing
 * 1: Color Process
 * 2: Analysis Only
 * 3: Classical Renderer
 * 4: Bounding Only Render (preferred by drivers)
 */

public class TargetFilter extends Filter implements MatFilter, TargetFilterConfig {
			
	public TargetFilter(int input) {
		
		stage = input;
		
		polygonEpsilon    = 5.0; //used for detecting polygons from contours
	
		targetHeightMin   = 25;
		targetWidthMin    = 40;
		targetSidesMin    = 3;
		targetRatioMin    = 1;
		targetAreaMin     = 1200; //areas and sizes are of bounding box
		
		targetHeightMax   = 120;
		targetWidthMax    = 200;
		targetSidesMax    = 10;
		targetRatioMax    = 2.5;
		targetAreaMax     = 15000;
		
		targetHeightIdeal = 80;   targetHeightWeight = 1;
		targetWidthIdeal  = 100;  targetWidthWeight  = 1;
		targetSidesIdeal  = 8;    targetSidesWeight  = 100;
		targetRatioIdeal  = 1.5;  targetRatioWeight  = 1000;
		targetAreaIdeal   = 5000; targetAreaWeight   = 0.03;
		
		
	
	}
	
	private int stage;
		
	//filter instances
	
	private final MatFilter _ColorSpace   = ColorSpace.createBGRtoHSV();
	private final MatFilter _ColorRange   = new ColorRange(Imgproc.COLOR_MIN, Imgproc.COLOR_MAX, true);
	private final MatFilter _Erode        = new Erode(Imgproc.ERODE_FACTOR);
	private final MatFilter _Dilate       = new Dilate(Imgproc.DILATE_FACTOR);
	private final MatFilter _GrayScale    = new GrayScale();
	private final MatFilter _BlackWhite   = new BlackWhite(Imgproc.BLACKWHITE_THRESH, 255, true);
	private final MatFilter _CrossHair    = new CrossHair();
	private final PolyArrayRender _OtherTargets = new PolyArrayRender(ScalarColors.BLUE, Render.OUTLINE_THICKNESS);
	private final PolygonRender   _BestTarget   = new PolygonRender(ScalarColors.RED,  Render.OUTLINE_THICKNESS);
	private final RectangleRender _Reticle      = new RectangleRender(ScalarColors.RED, -1); //-1 is filled
	private final RectangleRender _BoundingBox  = new RectangleRender(ScalarColors.GREEN, Render.BOX_THICKNESS);
	
	//should be set by constructor based on stage value (via switch)
		
	public Mat process(Mat srcImage) {
		if(stage == 0) return srcImage;
		
		List<PolygonCv> targets  = new ArrayList<>();
		     PolygonCv  bestTarget;
		
		Mat workingImage = srcImage.clone();
		
		_ColorSpace.process(workingImage);
		_ColorRange.process(workingImage);
		_Erode.process(workingImage);     
		_Dilate.process(workingImage);
		_GrayScale.process(workingImage);
		_BlackWhite.process(workingImage);
		
		if(stage == 1) return workingImage;
		
		targets = findTargets(workingImage);
		workingImage = srcImage.clone();
		
		if(targets.size() > 0) {
        	bestTarget = findBestTarget(targets);
        	
        	if(networkTable != null) { 
        		targetAnalysis(bestTarget); //no return as it simply writes data to netTables 
        		networkTable.putNumber("FrameCount", frameCount++); 
        	}	
        	
        	if(stage == 2) return workingImage; //commandline, so don't bother drawing anything
        	
        	if(stage == 3) {
        		_OtherTargets.setPolygon(targets);
        		_OtherTargets.process(workingImage);
        		
        		_BestTarget.setPolygon(bestTarget);
        		_BestTarget.process(workingImage);
        		
        		_Reticle.setCenter(bestTarget.getCenterX(), bestTarget.getMaxY());
        		_Reticle.setSize(Render.RETICLE_SIZE, Render.RETICLE_SIZE);
        		_Reticle.process(workingImage);
        	}
        	
        	if(stage == 4) {
        		_BoundingBox.setCenter(bestTarget.getCenterX(), bestTarget.getCenterY());
        		_BoundingBox.setSize(bestTarget.getHeight() / 2, bestTarget.getWidth() / 2);
        		_BoundingBox.process(workingImage);
        	}
        }
		
		_CrossHair.process(workingImage);
        		
		return workingImage;
	}
	
	private void targetAnalysis(PolygonCv foundTarget) { //tells the robo info about the target
        double offsetXDegrees, offsetXDegreesIdeal,
        	targetDistanceInches, baseDistanceInches, 
        	cameraAngleElevationRadians, targetAngleRadians; 
            	
        //calculates how far off center the target is from the center of the camera
    	offsetXDegrees = Math.toDegrees(
    						Math.atan(2 * foundTarget.getCenterX() * 
    						Math.tan(Camera.FOV_X_RADIANS/2) / Camera.RESOLUTION_X_PIXELS));
    	
    	//gets size of target in Radians
    	targetAngleRadians = Math.atan(2 * foundTarget.getMaxY() * Math.tan(Camera.FOV_Y_RADIANS/2) / Camera.RESOLUTION_Y_PIXELS) - 
    				  Math.atan(2 * foundTarget.getMinY() * Math.tan(Camera.FOV_Y_RADIANS/2) / Camera.RESOLUTION_Y_PIXELS);  
    	//gets degree value of top and bottom points, and finds difference
    	    	
    	//gets distance to target
    	targetDistanceInches = (Target.TAPE_HEIGHT_INCHES / 2) / Math.tan(targetAngleRadians); 
    	//use perspective height rather than targetTapeHeight?
    	
    	//because why not
    	targetDistanceInches = (Target.TAPE_HEIGHT_INCHES / 2) / 
			 	Math.tan(
						 (Target.TAPE_HEIGHT_INCHES / Camera.RESOLUTION_Y_PIXELS) * (Camera.FOV_Y_RADIANS / 2));
    	
    	//gets elevation of target to camera relative to ground
    	cameraAngleElevationRadians = Math.asin((Target.TOWER_HEIGHT_INCHES - Camera.OFFSET_Y_INCHES) / targetDistanceInches);
    	
    	//gets distance to the base of the target
    	baseDistanceInches = Math.cos(cameraAngleElevationRadians) * targetDistanceInches;
    	
    	/* gets 'ideal' target off center angle - because camera is to the side,
    	 * a perfectly zeroed robot will be slightly off from the camera
    	 */
    	if(Camera.OFFSET_X_INCHES != 0) {
    		offsetXDegreesIdeal = Math.toDegrees(Math.atan(targetDistanceInches / Camera.OFFSET_X_INCHES)); 
    	} else { 								//will be positive if cameraCenterOffset is negative
    		offsetXDegreesIdeal = 0;
    	}
    	
    	//compensates for Ideal angle offset
    	offsetXDegrees = offsetXDegrees - offsetXDegreesIdeal; 
    	
    	//determines if angle values are reasonable
    	if(offsetXDegrees < (Camera.FOV_X_DEGREES/2) && offsetXDegrees > (-Camera.FOV_X_DEGREES/2)) 
    		networkTable.putNumber("OffCenterDegreesX", offsetXDegrees);
    	
    	//writes calculated data to network tables
    	networkTable.putNumber("DistanceToBase",  baseDistanceInches);
    	networkTable.putNumber("DistanceToTarget", targetDistanceInches);  	
    }
}
