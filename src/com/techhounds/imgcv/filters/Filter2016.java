package com.techhounds.imgcv.filters;

import org.opencv.core.Mat;

public abstract class Filter2016 implements MatFilter {

	//CONFIGS
	
	private static int[]	colorFilterMin    = {30, 10, 40}; //TODO make all final as well
	private static int[]	colorFilterMax    = {95, 200, 120};
	private static int		blackWhiteThresh  = 40;
	private static int		dilateFactor      = 3; 
	private static int		erodeFactor       = 5; 
	
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
	
	public abstract Mat process(Mat srcImage);
}
