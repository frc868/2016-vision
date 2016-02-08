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
		
	//filter instances TODO could we put these into an interface?
	
	private final MatFilter _ColorSpace   = ColorSpace.createBGRtoHSV();
	private final MatFilter _ColorRange   = new ColorRange(colorFilterMin, colorFilterMax, true);
	private final MatFilter _Erode        = new Erode(erodeFactor);
	private final MatFilter _Dilate       = new Dilate(dilateFactor);
	private final MatFilter _GrayScale    = new GrayScale();
	private final MatFilter _BlackWhite   = new BlackWhite(blackWhiteThresh, 255, true);
	private final MatFilter _CrossHair    = new CrossHair();
	private final PolyArrayRender _OtherTargets = new PolyArrayRender(ScalarColors.BLUE, targetOutlineThickness);
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
        		_OtherTargets.setPolygon(targets);
        		_OtherTargets.process(workingImage);
        		
        		_BestTarget.setPolygon(bestTarget);
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
    	
    	offCenterDegreesX = Math.atan(2 * targetX * Math.tan(cameraHorizRads/2) / cameraResolutionX);
    	
    	targetDistance = (targetTapeWidth / 2) / 
    					 	Math.tan(
    								 (targetWidth / cameraResolutionX) * (cameraHorizRads / 2));
    	
    	cameraAngleElevation = Math.asin((targetTowerHeight - cameraElevation) / targetDistance);
    	
    	baseDistance = Math.cos(cameraAngleElevation) * targetDistance;
    	
    	if(offCenterDegreesX < (cameraHorizFOV/2) && offCenterDegreesX > (-cameraHorizFOV/2)) 
    		networkTable.putNumber("OffCenterDegreesX", offCenterDegreesX);
    	
    	networkTable.putNumber("DistanceToBase",  baseDistance);
    	networkTable.putNumber("DistanceToTarget", targetDistance);  	
    }
}