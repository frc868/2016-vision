package com.techhounds.imgcv.filters.vision2016;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.techhounds.imgcv.PolygonCv;
import com.techhounds.imgcv.filters.CrossHair;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.standard.BlackWhite;
import com.techhounds.imgcv.filters.standard.ColorRange;
import com.techhounds.imgcv.filters.standard.ColorSpace;
import com.techhounds.imgcv.filters.standard.Dilate;
import com.techhounds.imgcv.filters.standard.Erode;
import com.techhounds.imgcv.filters.standard.GrayScale;
import com.techhounds.imgcv.utils.*;

/*
 * Filter Stage List
 * 
 * 0: Do Nothing
 * 1: ColorSpace
 * 7: CrossHair
 * 8: OtherTargets
 * 9: BestTarget
 * 10: BoundingBox
 */

public class TargetFilter extends Filter implements MatFilter, TargetFilterConfig {
			
	public TargetFilter(int stage) {
		
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
		
		if(stage > 0) {
			useColorRange   = true;
			useColorSpace   = true;
		}
		if(stage > 1) useErode        = true;
		if(stage > 2) useDilate       = true;
		if(stage > 3) useGrayScale    = true;
		if(stage > 4) useBlackWhite   = true;
		if(stage > 5) useCrossHair    = true;
		if(stage > 6) useOtherTargets = true;
		if(stage > 7) useBestTarget   = true;
		if(stage > 8) useReticle      = true;
		if(stage > 9) {
			useOtherTargets = false;
			useBestTarget   = false;
			useBoundingBox  = true;
		}
	
	}
		
	//filter instances TODO could we put these into an interface?
	
	private final MatFilter _ColorSpace   = ColorSpace.createBGRtoHSV();
	private final MatFilter _ColorRange   = new ColorRange(colorFilterMin, colorFilterMax, true);
	private final MatFilter _Erode        = new Erode(erodeFactor);
	private final MatFilter _Dilate       = new Dilate(dilateFactor);
	private final MatFilter _GrayScale    = new GrayScale();
	private final MatFilter _BlackWhite   = new BlackWhite(blackWhiteThresh, 255, true);
	private final MatFilter _CrossHair    = new CrossHair();
	private final MatFilter _OtherTargets = new PolygonRender(ScalarColors.BLUE, targetOutlineThickness);
	private final MatFilter _BestTarget   = new PolygonRender(ScalarColors.RED,  targetOutlineThickness);
	private final MatFilter _Reticle      = new RectangleRender(ScalarColors.RED, -1); //-1 is filled
	private final MatFilter _BoundingBox  = new RectangleRender(ScalarColors.GREEN, boundingBoxThickness);
	
	//should be set by constructor based on stage value (via switch)
	
	private boolean   useColorRange;
	private boolean   useColorSpace;
	private boolean   useErode;
	private boolean   useDilate;
	private boolean   useGrayScale;
	private boolean   useBlackWhite;
	private boolean   useCrossHair;
	private boolean   useOtherTargets;
	private boolean   useBestTarget;
	private boolean   useReticle;
	private boolean   useBoundingBox;
	
	public Mat process(Mat srcImage) {
		List<PolygonCv> targets  = new ArrayList<>();
		     PolygonCv  bestTarget;
		     
		Mat workingImage = srcImage.clone();
		
		if(useColorRange) _ColorRange.process(workingImage); else return workingImage;
		if(useColorSpace) _ColorSpace.process(workingImage); else return workingImage;
		if(useErode)      _Erode.process(workingImage);     
		if(useDilate)     _Dilate.process(workingImage);
		
		targets = findTargets(workingImage);
		workingImage = srcImage.clone();
		
		if(targets.size() > 0) {
        	bestTarget = findBestTarget(targets);
        	
        	if(networkTable != null) { 
        		targetAnalysis(bestTarget); //no return as it simply writes data to netTables 
        		networkTable.putNumber("FrameCount", frameCount++); 
        	}	
        	
        	//draw polies here
        	
        }
        		
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
