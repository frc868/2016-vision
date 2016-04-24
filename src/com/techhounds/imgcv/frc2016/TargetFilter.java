package com.techhounds.imgcv.frc2016;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

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
			
	private FovCalculator fovCalc;

	private double robotAngleOffset;

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
		targetSidesMax    = 12;
		targetRatioMax    = 3;
		targetAreaMax     = 15000;
		
		targetHeightIdeal = 80;   targetHeightWeight = 1;
		targetWidthIdeal  = 100;  targetWidthWeight  = 1;
		targetSidesIdeal  = 8;    targetSidesWeight  = 100;
		targetRatioIdeal  = 1.5;  targetRatioWeight  = 1000;
		targetAreaIdeal   = 5000; targetAreaWeight   = 0.03;
		double x = (topLeft.x + bottomRight.x)/2;
		lineTop = new Point(800/2 - 172 , 0);
		lineBottom = new Point(800/2 - 20 , 599);
		
		fovCalc = new FovCalculator(Camera.FOV_X_DEGREES, Camera.RESOLUTION_X_PIXELS, 100.0);
		double centerLine = (topLeft.x + bottomRight.x) / 2 - (Camera.RESOLUTION_X_PIXELS / 2);
		robotAngleOffset = fovCalc.pixelFromCenterToDeg(centerLine);
	}
	
	private int stage;
		
	//filter instances
	
	private final MatFilter _ColorSpace   = ColorSpace.createBGRtoHSV();
	private final ColorRange _ColorRange   = new ColorRange(Imgproc.COLOR_MIN, Imgproc.COLOR_MAX, true);
	private final MatFilter _Erode        = new Erode(Imgproc.ERODE_FACTOR);
	private final MatFilter _Dilate       = new Dilate(Imgproc.DILATE_FACTOR);
	private final MatFilter _GrayScale    = new GrayScale();
	private final MatFilter _BlackWhite   = new BlackWhite(Imgproc.BLACKWHITE_THRESH, 255, true);
	private final MatFilter _CrossHair    = new CrossHair();
	private final PolyArrayRender _OtherTargets = new PolyArrayRender(ScalarColors.BLUE, Render.OUTLINE_THICKNESS);
	private final PolygonRender   _BestTarget   = new PolygonRender(ScalarColors.RED,  Render.OUTLINE_THICKNESS);
	private final RectangleRender _Reticle      = new RectangleRender(ScalarColors.RED, -1); //-1 is filled
	private final RectangleRender _BoundingBox  = new RectangleRender(ScalarColors.GREEN, Render.BOX_THICKNESS);

	private Point topLeft = new Point(375, 50);
	private Point bottomRight = new Point(525, 400);
	private Scalar targetRectangleColor = new Scalar(18, 120, 255);
	private Point lineTop;
	private Point lineBottom;
	private Scalar targetLineColor = new Scalar(20, 133, 255);
	
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
//		_GrayScale.process(workingImage);
//		_BlackWhite.process(workingImage);
		
		if(stage == 1) return workingImage;
		
		targets = findTargets(workingImage);
		workingImage = srcImage.clone();
		
		addTargetingRectangle(workingImage);
		
		if(targets.size() > 0) {
        	bestTarget = findBestTarget(targets);
        	        	
        	if(networkTable != null) { 
        		targetAnalysis(bestTarget); //no return as it simply writes data to netTables 
        		networkTable.putNumber("FrameCount", frameCount++); 
        	}
        	targetAnalysis(bestTarget, false);
        	
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
		
		//_CrossHair.process(workingImage);
        		
		return workingImage;
	}
	
	private void addTargetingRectangle(Mat workingImage) {
		DrawTool draw = new DrawTool();
		draw.setImage(workingImage);
		draw.setThickness(12);
		draw.setColor(targetRectangleColor);
		draw.drawRectangle(topLeft, bottomRight);
		draw.setThickness(1);
		draw.setColor(targetLineColor);
		draw.drawLine(lineTop, lineBottom);
		// TODO Auto-generated method stub
		
	}
	public void targetAnalysis(PolygonCv foundTarget){
		targetAnalysis(foundTarget, true);
	}

	private void targetAnalysis(PolygonCv foundTarget, boolean printToNetWorkTable) { //tells the robo info about the target
        double offsetXDegrees, offsetXDegreesIdeal,
        	targetDistanceInches, baseDistanceInches, 
        	cameraAngleElevationRadians, targetAngleRadians,
        	targetAngleFactor; 
            	
        //calculates how far off center the target is from the center of the camera
        //offsetXDegrees = Math.atan((400 - foundTarget.getCenterX()) * Math.tan(Camera.FOV_X_RADIANS/2) / (Camera.RESOLUTION_X_PIXELS/2));
        offsetXDegrees = Math.atan((400 - foundTarget.getMinX()) * Math.tan(Camera.FOV_X_RADIANS/2) / (Camera.RESOLUTION_X_PIXELS/2));
    	
    	//converts from radians (output of Math) to degrees
    	offsetXDegrees = Math.toDegrees(offsetXDegrees);
    	
    	//used to determine size of the target in radians
    	targetAngleFactor = 2 * Math.tan(Camera.FOV_X_RADIANS / 2) / Camera.RESOLUTION_X_PIXELS;
    	
 /*   	//gets size of target in Radians
    	targetAngleRadians = Math.atan(((Camera.RESOLUTION_X_PIXELS/2) - foundTarget.getMaxX()) * targetAngleFactor) - 
    				  		 Math.atan(((Camera.RESOLUTION_X_PIXELS/2) - foundTarget.getMinX()) * targetAngleFactor);  
    	//gets degree value of top and bottom points, and finds difference
    	    	
    	//targetAngleRadians could be negative, compensates for this
    	targetAngleRadians = Math.abs(targetAngleRadians);
    	
    	//gets distance to target
    	targetDistanceInches = (Target.TAPE_WIDTH_INCHES / 2) / Math.tan(targetAngleRadians); 
*/    	//use perspective height rather than targetTapeHeight?
    	
    	//ye olde algorithm
/*    	targetDistanceInches = (Target.TAPE_WIDTH_INCHES / 2) / Math.tan((Target.TAPE_WIDTH_INCHES / Camera.RESOLUTION_X_PIXELS) * (Camera.FOV_X_RADIANS / 2));
*/
    	
    	//new old algo
    	double dpx = (Camera.RESOLUTION_X_PIXELS/2) / Math.tan(Camera.FOV_X_RADIANS/2);
    	double tta = (foundTarget.getWidth()/2) / dpx;
    	targetDistanceInches = (Target.TAPE_WIDTH_INCHES) / Math.tan(tta);
    	
    	//System.out.println(dpx + " " + tta + " " + targetDistanceInches);
    	
    	//gets elevation of target to camera relative to ground
    	cameraAngleElevationRadians = Math.asin((Target.TOWER_HEIGHT_INCHES - Camera.OFFSET_Y_INCHES) / targetDistanceInches);
    	
    	//gets distance to the base of the target
    	baseDistanceInches = Math.cos(cameraAngleElevationRadians) * targetDistanceInches;
    	
    	/* gets 'ideal' target off center angle - because camera is to the side,
    	 * a perfectly zeroed robot will be slightly off from the camera
    	 */
    	//if(Camera.OFFSET_X_INCHES != 0) {
    		//offsetXDegreesIdeal = Math.toDegrees(Math.atan(targetDistanceInches / Camera.OFFSET_X_INCHES)); 
    	//} else { 								//will be positive if cameraCenterOffset is negative
    	//	offsetXDegreesIdeal = 0;
    	//}
    	
    	//compensates for Ideal angle offset
    	//offsetXDegrees = offsetXDegrees - offsetXDegreesIdeal; 
    	
    	double camAngle = fovCalc.pixelFromCenterToDeg(foundTarget.getCenterX() - (Camera.RESOLUTION_X_PIXELS/2));
    	double rot = -(robotAngleOffset - camAngle);
    	
    	offsetXDegrees = rot; //TODO temporary substitution
    	
    	//determines if angle values are reasonable
    	if(offsetXDegrees < (Camera.FOV_X_DEGREES/2) && offsetXDegrees > (-Camera.FOV_X_DEGREES/2) && printToNetWorkTable) {
    		networkTable.putNumber("OffCenterDegreesX", -Math.toDegrees(distanceOffset(foundTarget.getCenterX(), foundTarget.getCenterY())));
    		
    	}
    		
    	System.out.println("Angle: " + offsetXDegrees);
    	    	
    	//writes calculated data to network tables
    	System.out.println("Distance: " + baseDistanceInches);
    	if(printToNetWorkTable){
    	networkTable.putNumber("DistanceToBase",  baseDistanceInches);
    	networkTable.putNumber("DistanceToTarget", targetDistanceInches);  	
    	}
    	
    	//Newest Algorithm
    	distanceOffset(foundTarget.getCenterX(), foundTarget.getCenterY());
    }
	
	//+x is right
	//+y is down
	public double distanceOffset(double x,double y) {
	  //Image height (px)
	  double ih = Camera.RESOLUTION_Y_PIXELS;
	  //Image width (px)
	  double iw = Camera.RESOLUTION_X_PIXELS;
	  //Signed distance from center, in y direction (px)
	  double hv = y - ih/2;
	  //signed distance from center, in x direction (px)
	  double xv = iw/2 - x;

	  //Field of view (rad)
	  double fov = Camera.FOV_Y_RADIANS;
	  //Distance to virtual image plane (px)
	  double dv = (1.0 * ih) / (2 * Math.tan(fov/2));

	  //Height from camera to goal (in)
	  double hr = 77.75;
	  //Angle of inclination of the camera (rad)
	  double ac = 41 * Math.PI / 180;

	  //Distance offset from camera to center of rotation (in)
	  double od = 12;
	  //Horizontal offset from camera to center of rotation (in)
	  double ox = 9.25;

	  //Real distance from center of rotation to goal (in)
	  double dr = hr / Math.tan(ac - Math.atan(hv/dv)) + od;

	  //Real offset from center of rotation to goal (in)
	  double xr = dr * xv / dv + ox;
	  
	  //Angle from center of rotation to goal (rad)
	  double a = Math.atan(xr/dr);
	  
	  //Distance from center of rotation to goal (in)
	  double d = Math.hypot(dr, xr);
	  
	  System.out.println("Real Angle: " + a * 180 / Math.PI);
	  System.out.println("Real dist:  " + d);

	  return a;
	}


	public void setColorRangeConfig(File configFile) {
		try {
			FileReader configFileReader = new FileReader(configFile);
			BufferedReader  configFileBReader = new BufferedReader(configFileReader);
			
			for(int i = 0; i < Imgproc.COLOR_MAX.length; i++) {
				Imgproc.COLOR_MAX[i] = Integer.parseInt(configFileBReader.readLine());
				System.out.println("Read & Set " + Imgproc.COLOR_MAX[i]);
				Imgproc.COLOR_MIN[i] = Integer.parseInt(configFileBReader.readLine());
				System.out.println("Read & Set " + Imgproc.COLOR_MIN[i]);

			}
			
			_ColorRange.setRanges(Imgproc.COLOR_MIN, Imgproc.COLOR_MAX, true);
			
			configFileReader.close();
			configFileBReader.close();

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}
