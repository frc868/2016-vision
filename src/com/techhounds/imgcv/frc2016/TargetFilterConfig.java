package com.techhounds.imgcv.frc2016;

public interface TargetFilterConfig {

	//Imgproc configs
	
	public final int[] colorFilterMin   = {80, 60, 200};
	public final int[] colorFilterMax   = {255, 255, 255};
	public final int   blackWhiteThresh = 40;
	public final int   dilateFactor     = 4; 
	public final int   erodeFactor      = 5;
	
	//Render configs 
	
	public final int targetOutlineThickness = 1;
	public final int reticleSize            = 2;
	public final int boundingBoxThickness   = 5;
	
	//physical data
	
	public final double targetTapeWidth   = 20; //inches
	public final double targetTapeHeight  = 20;
	public final double targetTowerHeight = 80;//inches, not sure if to bottom or middle of target
	public final double cameraElevation   = 22;//inches
	public final double cameraHorizFOV    = 67; //degrees
	public final double cameraVertFOV     = 51;
	public final double cameraResolutionX = 800;//pixels
	public final double cameraResolutionY = 600;
}
