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

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * A image filter which performs all of the operations in an attempt to locate
 * the rectangular target areas from the 2013 FRC competition.
 *
 * @author Paul Blankenbaker
 */
public final class VisionFilter2016 implements MatFilter {
	
	//Configurations
	
	private static int[]  colorFilterMin   = {70, 100, 25};
	private static int[]  colorFilterMax   = {90, 255, 255};
	private static int    blackWhiteThresh = 40;
	private static int    dilateFactor     = 3;
	private static int    erodeFactor      = 5;
	private static double polygonEpsilon   = 5.0; //used for detecting polygons from contours
	private static double targetHeightMin  = 10.0; 
	private static double targetWidthMin   = 50.0;
	private static int    targetSidesMin   = 2; //ie at least 3 sides
	
	//Processing Filters
	
    private final MatFilter  _ColorRange; //Used to filter image to a specific color range.
    private final Dilate     _Dilate;     //grows remaining parts of the images
    private final Erode      _Erode;      //shrinks remaining parts of the images
    private final GrayScale  _GrayScale; //Used to convert image to a gray scale rendering.
    private final BlackWhite _BlackWhite; //Used to convert from gray scale to black and white.

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
    	Mat output = srcImage.clone();
    	Mat hierarchy = new Mat(); //used in contour detection
    	
    	PolygonCv target;
    	List<MatOfPoint> contours = new ArrayList<>(); //list of objects in image
        List<PolygonCv>  targets = new ArrayList<>();  //list of potential targets in image
    	
        int targetSides, targetsFound;
        float targetHeight, targetWidth, targetRatio, targetArea, targetX, targetY;
        
        Mat coloredImage = _ColorRange.process(srcImage); 
        Mat dilatedImage = _Dilate.process(coloredImage); //what if we erode first?
        Mat erodedImage  = _Erode.process(dilatedImage);
        Mat grayedImage  = _GrayScale.process(erodedImage);
        Mat bwImage      = _BlackWhite.process(grayedImage);
        
        Imgproc.cvtColor(bwImage, output, Imgproc.COLOR_GRAY2BGR);
        Imgproc.findContours(bwImage, contours, hierarchy, 
        					 Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for(int i = 0; i < contours.size(); i++) { //for each 'contour' object
        	target = PolygonCv.fromContour(contours.get(i), polygonEpsilon); //create a polygon
        	
        	targetSides  = target.size();
        	targetHeight = target.getHeight();
        	targetWidth  = target.getWidth();
        	
        	//possibly useful in VisionTool but unnecessary in VisionView
        	//System.out.println("Height: " + targetHeight + " Width: " + targetWidth);
        	//System.out.println("Sides: " + targetSides);
        	//System.out.println();
        	
        	if(targetHeight > targetHeightMin && 
        	    targetWidth > targetWidthMin  && 
        	    targetSides > targetSidesMin) {
        		
        		targets.add(target); //if within range, add to list of potential targets
        	}
        }
        
        targetsFound = targets.size();
        
        if(targetsFound == 1) {	 //if we found only one target
        	target = targets.get(0); //select that target
        	
        	targetSides  = target.size();
        	targetHeight = target.getHeight();
        	targetWidth  = target.getWidth();
        	targetRatio  = target.getBoundingAspectRatio();
        	targetArea   = target.getBoundingArea();
        	targetX		 = target.getCenterX();
        	targetY		 = target.getCenterY();
        	
        	System.out.print("Found target at: " + targetX + ", " + targetY);
        	System.out.print(" Height: " + targetHeight + " Width: " + targetWidth);
        	System.out.print(" Sides: " + targetSides);
        	System.out.print(" Ratio: " + targetRatio + " Area: " + targetArea);
        	
        } else if (targetsFound > 1) {
        	System.out.println("Error: Multiple Targets Found!");
        } else { //targetsFound must equal 0
        	System.out.println("Error: No Targets Found!");
        }
	    
        return output;
    }
}
