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

//import edu.wpi.first.wpilibj.networktables.NetworkTable;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

/**
 * A image filter which performs all of the operations in an attempt to locate
 * the rectangular target areas from the 2013 FRC competition.
 *
 * @author Paul Blankenbaker
 */
public final class VisionFilter2016 implements MatFilter {
	
	//Configurations
	
	private static int[]  colorFilterMin    = {60, 100, 20};
	private static int[]  colorFilterMax    = {90, 255, 255};
	private static int    blackWhiteThresh  = 40;
	private static int    dilateFactor      = 3;
	private static int    erodeFactor       = 5;
	
	private static double polygonEpsilon    = 5.0; //used for detecting polygons from contours
	private static double targetHeightMin   = 10.0; 
	private static double targetWidthMin    = 50.0;
	private static int    targetSidesMin    = 2; //ie at least 3 sides
	
	private static double   targetTapeWidth   = 24; //inches
	private static double    cameraHorizFOV    = 67; //could be wrong
	private static double    cameraResolutionX = 800;
	
	//Processing Filters
	
    private final MatFilter  _ColorRange; //Used to filter image to a specific color range.
    private final Dilate     _Dilate;     //grows remaining parts of the images
    private final Erode      _Erode;      //shrinks remaining parts of the images
    private final GrayScale  _GrayScale; //Used to convert image to a gray scale rendering.
    private final BlackWhite _BlackWhite; //Used to convert from gray scale to black and white.
	//private NetworkTable networkTable;
	//private int frameCnt;

    //Constructs a new instance by pre-allocating all of our image filtering objects.
    public VisionFilter2016() { 
    	_ColorRange = createHsvColorRange();
    	_Dilate 	= new Dilate(dilateFactor);  
    	_Erode		= new Erode(erodeFactor); 
        _GrayScale = new GrayScale();
        _BlackWhite = createBlackWhite();
    }

    /**
     * Helper method to provide a single location that creates the image filter
     * used to go from a gray scale image to a black and white image.
     *
     * @return A image filter that converts a gray scale image to a black and
     * white image.
     */
    public static BlackWhite createBlackWhite() {
        return new BlackWhite(blackWhiteThresh, 255, false);
    }
    
    public static MatFilter createHsvColorRange() {
        Sequence filter = new Sequence();
        filter.addFilter(ColorSpace.createBGRtoHSV());
        filter.addFilter(new ColorRange(colorFilterMin, colorFilterMax, true));
        return filter;
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
    	//frameCnt++;
    	//if (networkTable != null) {
    	//	networkTable.putNumber("TargetingFrame", frameCnt);
    	//}
        Mat outputImage = srcImage.clone();
    	List<PolygonCv>  targets = new ArrayList<>();  //list of potential targets in image
        Mat processedImage = new Mat();
        int targetsFound;
        
        processedImage = primaryProcessing(srcImage);
        Imgproc.cvtColor(processedImage, outputImage, Imgproc.COLOR_GRAY2BGR);
        targets = findTargets(processedImage);
        targetsFound = targets.size();
        
        if(targetsFound == 1) {	 //if we found only one target
        	targetAnalysis(targets.get(0)); //analyze that target
        } else if (targetsFound > 1) {
        	System.out.println("Error: Multiple Targets Found!");
        } else { //targetsFound must equal 0
        	System.out.println("Error: No Targets Found!");
        }
	    
        return outputImage;
    }
    
    private Mat primaryProcessing(Mat inputImage) { //does basic color/erosion processing
    	Mat coloredImage = _ColorRange.process(inputImage); 
        Mat dilatedImage = _Dilate.process(coloredImage); //what if we erode first?
        Mat erodedImage  = _Erode.process(dilatedImage);
        Mat grayedImage  = _GrayScale.process(erodedImage);
        Mat bwImage      = _BlackWhite.process(grayedImage);
        
        return bwImage;
    }
    
    private List<PolygonCv> findTargets(Mat inputImage) { //finds potential targets in an image
	    Mat hierarchy 	          = new Mat();
	    List<MatOfPoint> contours = new ArrayList<>(); //list of objects in image
        List<PolygonCv>  targets  = new ArrayList<>();  //list of potential targets in image
        PolygonCv  		 currentTarget;
        
        int targetSides;
        float targetHeight, targetWidth;
        
        Imgproc.findContours(inputImage, contours, hierarchy, 
        					 Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0; i < contours.size(); i++) { //for each 'contour' object
        	currentTarget = PolygonCv.fromContour(contours.get(i), polygonEpsilon); //create a polygon
        	
        	targetSides  = currentTarget.size();
        	targetHeight = currentTarget.getHeight();
        	targetWidth  = currentTarget.getWidth();
        	
        	//possibly useful in VisionTool but unnecessary in VisionView
        	System.out.println("Height: " + targetHeight + " Width: " + targetWidth);
        	System.out.println("Sides: " + targetSides);
        	System.out.println();
        	
        	if(targetHeight > targetHeightMin && 
        	    targetWidth > targetWidthMin  && 
        	    targetSides > targetSidesMin) {
        		
        		targets.add(currentTarget); //if within range, add to list of potential targets
        	}
        }
        
        return targets;
    }
    
    private void targetAnalysis(PolygonCv foundTarget) {
        int    targetSides;
        float  targetHeight, targetWidth, targetRatio, targetArea, targetX, targetY;
        double offCenterPixelsX, offCenterPercentX, offCenterDegreesX, targetDistance;
    	
    	targetSides  = foundTarget.size();
    	targetHeight = foundTarget.getHeight();
    	targetWidth  = foundTarget.getWidth();
    	targetRatio  = foundTarget.getBoundingAspectRatio();
    	targetArea   = foundTarget.getBoundingArea();
    	targetX		 = foundTarget.getCenterX();
    	targetY      = foundTarget.getCenterY();
    	
    	System.out.print("Found target at: " + targetX + ", " + targetY);
    	System.out.print(" Height: " + targetHeight + " Width: " + targetWidth);
    	System.out.print(" Sides: " + targetSides);
    	System.out.println(" Ratio: " + targetRatio + " Area: " + targetArea);
    	
    	offCenterPixelsX = targetX - (cameraResolutionX / 2);
    	offCenterPercentX = offCenterPixelsX / (cameraResolutionX / 2); //actually decimal -1 to 1
    	offCenterDegreesX = offCenterPercentX * (cameraHorizFOV / 2);
    	
    	targetDistance = (targetTapeWidth / 2) / 
    					 Math.tan(Math.toRadians((targetWidth / cameraResolutionX) * (cameraHorizFOV / 2)));
    	
    	System.out.println("Off By " + offCenterDegreesX + " Degrees");
    	System.out.println("Approx. Distance: " + targetDistance + "'");
    }

    //public void setNetworkTable(NetworkTable nt) {
    //	networkTable = nt;
    //}
}


