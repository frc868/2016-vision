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
		
		targetHeightIdeal = 80;
		targetWidthIdeal  = 100;
		targetSidesIdeal  = 8;
		targetRatioIdeal  = 1.5;
		targetAreaIdeal   = 5000;
	
	}
	
	private int stage;
		
	//filter instances TODO could we put these into an interface?
	
	private final MatFilter _ColorSpace   = ColorSpace.createBGRtoHSV();
	private final MatFilter _ColorRange   = new ColorRange(colorFilterMin, colorFilterMax, true);
	private final MatFilter _Erode        = new Erode(erodeFactor);
	private final MatFilter _Dilate       = new Dilate(dilateFactor);
	private final MatFilter _GrayScale    = new GrayScale();
	private final MatFilter _BlackWhite   = new BlackWhite(blackWhiteThresh, 255, true);
	private final MatFilter _CrossHair    = new CrossHair();
	private final PolygonRender   _OtherTargets = new PolygonRender(ScalarColors.BLUE, targetOutlineThickness);
	private final PolygonRender   _BestTarget   = new PolygonRender(ScalarColors.RED,  targetOutlineThickness);
	private final RectangleRender _Reticle      = new RectangleRender(ScalarColors.RED, -1); //-1 is filled
	private final RectangleRender _BoundingBox  = new RectangleRender(ScalarColors.GREEN, boundingBoxThickness);
	
	//should be set by constructor based on stage value (via switch)
		
	public Mat process(Mat srcImage) {
		if(stage == 0) return srcImage;
		
		List<PolygonCv> targets  = new ArrayList<>();
		     PolygonCv  bestTarget;
		
		Mat workingImage = srcImage.clone();
		
		_ColorRange.process(workingImage);
		_ColorSpace.process(workingImage);
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
        	
        	if(stage == 2) return workingImage;
        	
        	if(stage == 3) {
        		for(int i = 0; i < targets.size(); i++) { //size will be one less than prev call
        			_OtherTargets.setPolygon(targets.get(i));
        			_OtherTargets.process(workingImage);
        		}
        		_BestTarget.process(workingImage);
        		
        		_Reticle.setCenter(bestTarget.getCenterX(), bestTarget.getMaxY());
        		_Reticle.setSize(reticleSize, reticleSize);
        		_Reticle.process(workingImage);
        	}
        	
        	if(stage == 4) {
        		_BoundingBox.setCenter(bestTarget.getCenterX(), bestTarget.getCenterY());
        		_BoundingBox.setSize(bestTarget.getHeight(), bestTarget.getWidth());
        		_BoundingBox.process(workingImage);
        	}
        	
        }
		
		_CrossHair.process(workingImage);
        		
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
