package com.techhounds.imgcv.frc2016;

public interface TargetFilterConfig {
	
	interface Render {
		public final int OUTLINE_THICKNESS = 1;
		public final int RETICLE_SIZE      = 2;
		public final int BOX_THICKNESS     = 3;
	}
	
	interface Imgproc {
		public int[] COLOR_MAX         = {109, 255, 255};
		public int[] COLOR_MIN         = {74, 232, 140 /*215*/};
		public final int   BLACKWHITE_THRESH = 40;
		public final int   DILATE_FACTOR     = 4; 
		public final int   ERODE_FACTOR      = 5;
	}
	
	interface Target {
		public final double TAPE_WIDTH_INCHES   = 20;
		public final double TAPE_HEIGHT_INCHES  = 14;
		public final double TOWER_HEIGHT_INCHES = 80;
	}
	
	interface Camera {
		public final double OFFSET_Y_INCHES     = 12; //amount above ground
		public final double OFFSET_X_INCHES     = 9;  //negative is to left
		public final double FOV_X_DEGREES       = 67; 
		public final double FOV_Y_DEGREES       = 51;
		public final double FOV_X_RADIANS       = Math.toRadians(FOV_X_DEGREES);
		public final double FOV_Y_RADIANS       = Math.toRadians(FOV_Y_DEGREES);
		public final double RESOLUTION_X_PIXELS = 800;
		public final double RESOLUTION_Y_PIXELS = 600;
		
	}
}
