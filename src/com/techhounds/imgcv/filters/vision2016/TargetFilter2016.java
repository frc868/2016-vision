package com.techhounds.imgcv.filters.vision2016;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import com.techhounds.imgcv.filters.CrossHair;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.standard.BlackWhite;
import com.techhounds.imgcv.filters.standard.ColorRange;
import com.techhounds.imgcv.filters.standard.ColorSpace;
import com.techhounds.imgcv.filters.standard.Dilate;
import com.techhounds.imgcv.filters.standard.Erode;
import com.techhounds.imgcv.filters.standard.GrayScale;
import com.techhounds.imgcv.filters.standard.Sequence;

public abstract class TargetFilter2016 implements MatFilter {

	//CONFIGS
	
	//private static int[]	colorFilterMin    = {30, 10, 40}; //TODO make all final as well
	//private static int[]	colorFilterMax    = {95, 200, 120};
	//private static int[]	colorFilterMin    = {50, 75, 70}; 
	//private static int[]	colorFilterMax    = {115, 255, 210};
	private static int[]    colorFilterMin    = {65, 175, 120};
	private static int[]    colorFilterMax    = {110, 255, 255};
	private static int		blackWhiteThresh  = 40;
	private static int		dilateFactor      = 3; 
	private static int		erodeFactor       = 5; 
	
	private static double[] bestTargetColors  = {100, 100, 255};
	private static double[] reticleColors     = {100, 100, 255};
	private static double[] otherTargetColors = {255, 100, 100};
	
	//METHODS
	
    public static Dilate createDilate() {
    	return new Dilate(dilateFactor);
    }
    
    public static Erode createErode() {
    	return new Erode(erodeFactor);
    }
    
    public static GrayScale createGrayScale() {
    	return new GrayScale();
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
	
    public static CrossHair createCrossHair() {
    	return new CrossHair(); 
    }
    
    public static Scalar createBestTargetOverlay() {
    	return new Scalar(bestTargetColors);
    }
    
    public static Scalar createOtherTargetOverlay() {
    	return new Scalar(otherTargetColors);
    }
    
    public static Scalar createReticleOverlay() {
    	return new Scalar(reticleColors);
    }
    
	public abstract Mat process(Mat srcImage);
}
