/*
 * Copyright (c) 2013, Paul Blankenbaker
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.techhounds.imgcv.filters;

import com.techhounds.imgcv.PolygonCv;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * A image filter which performs all of the operations in an attempt to locate
 * the rectangular target areas from the 2013 FRC competition.
 *
 * @author Paul Blankenbaker
 */
public final class VisionFilter2016 extends Filter2016 {
	
	//Configurations
	
	private static double[] bestTargetColors  = {100, 100, 255};
	private static double[] otherTargetColors = {255, 100, 100};
	private static int      targetOutlineThickness   = 1;

	private static double   polygonEpsilon    = 5.0; //used for detecting polygons from contours
	private static int      targetSidesMin    = 4; //ie at least 3 sides
	private static double   targetRatioMin    = 0.3;
	private static double   targetRatioMax    = 3.0;
	private static double   targetAreaMin     = 1000; //areas and sizes are of bounding box
	private static double   targetAreaMax     = 15000;
	
	private static double   targetHeightIdeal = 30;
	private static double   targetWidthIdeal  = 150;
	private static double   targetSidesIdeal  = 6;
	private static double   targetRatioIdeal  = 0.85;
	private static double   targetAreaIdeal   = 7500;
	
	private static double   targetTapeWidth   = 24; //inches
	private static double   targetTowerHeight = 120; //inches
	private static double   cameraHorizFOV    = 67; //could be wrong
	private static double   cameraResolutionX = 800;

	private        double   frameCount        = 0;
	private  NetworkTable   networkTable;
	
	
	//Processing Filters
	
    private final MatFilter   _ColorRange; //Used to filter image to a specific color range.
    private final Dilate      _Dilate;     //grows remaining parts of the images
    private final Erode       _Erode;      //shrinks remaining parts of the images
    private final GrayScale   _GrayScale;  //Used to convert image to a gray scale rendering.
    private final BlackWhite  _BlackWhite; //Used to convert from gray scale to black and white.
    private final CrossHair   _CrossHair;  //used to draw a crosshair
    private final Scalar      _BestTargetOverlay; 
    private final Scalar      _OtherTargetOverlay;

    //Constructs a new instance by pre-allocating all of our image filtering objects.
    public VisionFilter2016() { 
    	_ColorRange         = super.createHsvColorRange();
    	_Dilate 	        = super.createDilate();
    	_Erode		        = super.createErode();
        _GrayScale          = super.createGrayScale();
        _BlackWhite         = super.createBlackWhite(); 
        _CrossHair          = new CrossHair();    
        _BestTargetOverlay  = new Scalar(bestTargetColors);
        _OtherTargetOverlay = new Scalar(otherTargetColors);
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param srcImage - The source image to be processed (passing {@code null}
     * is not permitted).
     *
     * @return The original image with overlay information applied (we do a lot
     * of filtering and try to locate the 2013 FRC rectangular target regions).
     */
    @Override
    public Mat process(Mat srcImage) {
    	List<PolygonCv> targets             = new ArrayList<>();
    	PolygonCv       bestTarget;
        Mat             processedImage      = new Mat();
        Mat             outputImage         = srcImage.clone();
        
        
        processedImage = primaryProcessing(srcImage.clone()); //creates new color processed image
        targets        = findTargets(processedImage); //detects targets in new processed image
        
        if(targets.size() > 0) {
        	bestTarget = findBestTarget(targets);
        	
        	if(networkTable != null) { 
        		targetAnalysis(bestTarget); //no return as it simply writes data to netTables 
        		networkTable.putNumber("FrameCount", frameCount++); 
        	}	
        	
        	drawTargets(outputImage, targets); //always draw targets if found
        	drawTarget(outputImage,  bestTarget);
        	drawReticle(outputImage, bestTarget);
        }
        
        drawCrossHair(outputImage);
        
        return outputImage;
    }
    
    private Mat primaryProcessing(Mat inputImage) { //does basic color/erosion processing
    	_ColorRange.process(inputImage); 
    	_Erode.process(inputImage);
        _Dilate.process(inputImage);
        _GrayScale.process(inputImage);
        _BlackWhite.process(inputImage);
        //blur is done via camera focus, not here
        
        return inputImage; //convenience's sake, not necessary
    }
    
    private List<PolygonCv> findTargets(Mat inputImage) { //finds potential targets in an image
	    Mat hierarchy 	          = new Mat();
	    List<MatOfPoint> contours = new ArrayList<>(); //list of objects in image
        List<PolygonCv>  targets  = new ArrayList<>(); //list of potential targets in image
        PolygonCv  		 currentTarget;
        
        Imgproc.findContours(inputImage, contours, hierarchy, //doesn't modify inputImage
        					 Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0; i < contours.size(); i++) {            //for each 'contour' object
        	currentTarget = PolygonCv.fromContour(
        			        contours.get(i), polygonEpsilon); //create a polygon
        	        	
        	if(currentTarget.size() 		          > targetSidesMin && //filters out polygons 
        	   currentTarget.getBoundingAspectRatio() > targetRatioMin && //that are obviously
        	   currentTarget.getBoundingAspectRatio() < targetRatioMax && //not the target
        	   currentTarget.getBoundingArea()        > targetAreaMin  &&
        	   currentTarget.getBoundingArea()        < targetAreaMax) {
        		
        		targets.add(currentTarget); //if within range, add to list of potential targets
        	}
        }      
        return targets;
    }
 
    private PolygonCv findBestTarget(List<PolygonCv> targetList) {
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
    
    private double getTargetRating(PolygonCv inputTarget) {
    	double targetRating = 1000000;
    	
    	targetRating -= Math.abs(inputTarget.getHeight() -  targetHeightIdeal); //TODO remove these?
    	targetRating -= Math.abs(inputTarget.getWidth() -   targetWidthIdeal);
    	targetRating -= 100 * Math.abs(inputTarget.size() - targetSidesIdeal);
    	targetRating -= 1000 * Math.abs(inputTarget.getBoundingAspectRatio() - targetRatioIdeal);
    	targetRating -= 0.03 * Math.abs(inputTarget.getBoundingArea() - targetAreaIdeal);
    	
    	return targetRating;
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
    	
    	cameraAngleElevation = Math.asin(targetTowerHeight / targetDistance);
    	
    	baseDistance = Math.cos(cameraAngleElevation) * targetDistance;
    	
    	networkTable.putNumber("OffCenterDegreesX", offCenterDegreesX);
    	networkTable.putNumber("DistanceToBase",  baseDistance);
    }

    private Mat drawTargets(Mat inputImage, List<PolygonCv> targetList) {
    	List<MatOfPoint> contours = new ArrayList<>();
    	PolygonCv        currentTarget; 
    	
    	for(int i = 0; i < targetList.size(); i++) {
    		currentTarget = targetList.get(i);
    		
    		contours.add(currentTarget.toContour());
    		currentTarget.drawInfo(inputImage, _OtherTargetOverlay);
    	} //TODO avoid drawing best target twice, maybe remove from master target list
    	
    	Imgproc.drawContours(inputImage, contours, -1, _OtherTargetOverlay, targetOutlineThickness); 
    	//TODO figure out what the -1 is for
    	
    	return inputImage; //again only for convenience, such as when clone is passed as argument
    }
    
    private Mat drawTarget(Mat inputImage, PolygonCv bestTarget) {
    	List<MatOfPoint> bestContours = new ArrayList<>();
    	
    	bestContours.add(bestTarget.toContour());
    	Imgproc.drawContours(inputImage, bestContours, -1, _BestTargetOverlay, targetOutlineThickness);

    	return inputImage;
    }
    
    private Mat drawReticle(Mat inputImage, PolygonCv bestTarget){
    	
    	return inputImage;
    }
    
    private Mat drawCrossHair(Mat inputImage) {
    	return _CrossHair.process(inputImage);
    }
    
    public void setNetworkTable(NetworkTable nt) {
    	networkTable = nt;
    }
}


