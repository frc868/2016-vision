package com.techhounds.imgcv.filters.vision2016;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.techhounds.imgcv.PolygonCv;
import com.techhounds.imgcv.filters.CrossHair;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.standard.ColorRange;
import com.techhounds.imgcv.filters.standard.Erode;
import com.techhounds.imgcv.utils.*;



public class TargetFilter extends Filter implements MatFilter {
	
	//parent configs, ala constructor
	
	private int stage;
	
	public TargetFilter(int stage) {
	
		this.stage = stage;
		
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
		
		targetHeightIdeal = 80;
		targetWidthIdeal  = 100;
		targetSidesIdeal  = 8;
		targetRatioIdeal  = 1.5;
		targetAreaIdeal   = 5000;
	
	}
	
	//Imgproc configs
	
	private static int[]    colorFilterMin    = {40, 175, 40};
	private static int[]    colorFilterMax    = {105, 255, 255};
	private static int		blackWhiteThresh  = 40;
	private static int		dilateFactor      = 4; 
	private static int		erodeFactor       = 5;
	
	//Render configs (all configs below are the same across all instances)
	
	private final static int targetOutlineThickness   = 1;
	private final static int reticleSize     = 2;
	
	private static double[]  bestTargetColors  = {100, 100, 255};
	private static double[]  reticleColors     = {100, 100, 255};
	private static double[]  otherTargetColors = {255, 100, 100};
	
	//physical data
	
	private final static double   targetTapeWidth   = 20; //inches
	private final static double   targetTowerHeight = 80;//inches, not sure if to bottom or middle of target
	private final static double   cameraElevation   = 22;//inches
	private final static double   cameraHorizFOV    = 67; //degrees
	private final static double   cameraResolutionX = 800;//pixels
	
	public Mat process(Mat srcImage) {
		Mat workingImage = srcImage.clone(); //placeholder for operations
		ColorRange colorRange = new ColorRange(colorFilterMin, colorFilterMax, true);
		CrossHair  crossHair  = new CrossHair();
		List<PolygonCv> targets  = new ArrayList<>();
		PolygonCv       bestTarget;
		
		Imgproc.cvtColor(workingImage, workingImage, Imgproc.COLOR_BGR2HSV);
		colorRange.process(workingImage); //this sucks
		Imgproc.erode(workingImage, workingImage, Mat.ones(erodeFactor, erodeFactor, CvType.CV_8U));
		Imgproc.dilate(workingImage, workingImage, Mat.ones(dilateFactor, dilateFactor, CvType.CV_8U));
		Imgproc.cvtColor(workingImage, workingImage, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(workingImage, workingImage, blackWhiteThresh, 255, Imgproc.THRESH_BINARY);
		
		if(stage < 2) return workingImage;
		
		targets = findTargets(workingImage);
		workingImage = srcImage.clone();
		
		if(targets.size() > 0) {
        	bestTarget = findBestTarget(targets);
        	
        	System.out.println(bestTarget.getHeight());
        	System.out.println(bestTarget.getWidth());
        	System.out.println(bestTarget.size());
        	System.out.println(bestTarget.getBoundingAspectRatio());
        	System.out.println(bestTarget.getBoundingArea());
        	System.out.println();
        	System.out.println();
        	System.out.println();
        	System.out.println();
        	System.out.println();
        	System.out.println();
        	
        	if(networkTable != null) { 
        		targetAnalysis(bestTarget); //no return as it simply writes data to netTables 
        		networkTable.putNumber("FrameCount", frameCount++); 
        	}	
        	
        	PolygonRender.drawTargets(workingImage, targets, otherTargetColors, 
        			targetOutlineThickness); //always draw targets if found
        	PolygonRender.drawTarget(workingImage, bestTarget, bestTargetColors, 
        			targetOutlineThickness);
        	PolygonRender.drawPoint(workingImage, bestTarget.getCenterX(), 
        			bestTarget.getMinY(), reticleSize, reticleSize, reticleColors);
        }
        
        crossHair.process(workingImage);
		
		return workingImage;
	}
	
	private void targetAnalysis(PolygonCv foundTarget) { //tells the robo info about the target
        double offCenterDegreesX, targetDistance, baseDistance, cameraAngleElevation; //elevation in RADIANS
        double cameraHorizRads = Math.toRadians(cameraHorizFOV);
        float targetWidth 	   = foundTarget.getWidth();
        float targetX		   = foundTarget.getCenterX();
    	
    	offCenterDegreesX = ((targetX / (cameraResolutionX / 2)) - 1) * cameraHorizFOV;
    	
    	targetDistance = (targetTapeWidth / 2) / 
    					 	Math.tan(
    								 (targetWidth / cameraResolutionX) * (cameraHorizRads / 2));
    	
    	cameraAngleElevation = Math.asin((targetTowerHeight - cameraElevation) / targetDistance);
    	
    	baseDistance = Math.cos(cameraAngleElevation) * targetDistance;
    	
    	networkTable.putNumber("OffCenterDegreesX", offCenterDegreesX);
    	networkTable.putNumber("DistanceToBase",  baseDistance);
    	networkTable.putNumber("DistanceToTarget", targetDistance);  	
    }
}
