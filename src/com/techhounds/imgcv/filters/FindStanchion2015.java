/*
 * Copyright (c) 2015, Paul Blankenbaker
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
 * the yellow and red stanchions used at the 2015 AVC SparkFun competition.
 *
 * @author Paul Blankenbaker
 */
public final class FindStanchion2015 implements MatFilter {

	/**
	 * How much to dialate the black and white image by.
	 */
    private static final int DILATE_FACTOR = 7;

    /**
     * How much to erode the black and white image by.
     */
	private static final int ERODE_FACTOR = 5;

    /**
     * Used as color for overlays.
     */
    private final Scalar _Color;
    
    /**
     * Will be true if processing image finds a stanchion.
     */
    private boolean _Found = false;

    /**
     * Thickness of lines used when drawing overlays.
     */
    private final int _Thickness;

    /** Set to true for lots of debug output dumped to the console. */
	private boolean _Debug;

	/** Used to crop image down to smaller region of interest before processing. */
	private MatFilter _CropFilter;

	/** Sequence of filters to apply to image to end up with BW to look for objects. */
	private Sequence _Filter;

    /**
     * Constructs a new instance by pre-allocating all of our image filtering
     * objects.
     * 
     * @param red Pass true to find red stanchion, false to find yellow
     */
    public FindStanchion2015(boolean red) {
        _CropFilter = createCropFilter();
    	_Filter = createSteps(red, false);
    	
        double[] colors = {100, 100, 250};
        _Color = new Scalar(colors);
        _Thickness = 1;
    }

    /**
     * Creates color range filter for HSV image to find red stanchion color (lower red hues).
     *
     * @return A new {@link ColorRange} filter.
     */
    public static MatFilter createRedColorRange() {
        return new BitwiseOr(createRedLowerColorRange(), createRedUpperColorRange());
    }

    /**
     * Creates color range filter for HSV image to find red stanchion color (lower red hues).
     *
     * @return A new {@link ColorRange} filter.
     */
    public static ColorRange createRedLowerColorRange() {
            int[] keepMin = {0, 160, 60};
            int[] keepMax = {10, 255, 255};
            return new ColorRange(keepMin, keepMax, true);
    }

    /**
     * Creates color range filter for HSV image to find red stanchion color (upper red hues).
     *
     * @return A new {@link ColorRange} filter.
     */
    public static ColorRange createRedUpperColorRange() {
            int[] keepMin = {160, 165, 60};
            int[] keepMax = {200, 255, 255};
            return new ColorRange(keepMin, keepMax, true);
    }

    /**
     * Creates color range filter for HSV image to find yellow stanchion color.
     *
     * @return A new {@link ColorRange} filter.
     */
    public static ColorRange createYellowColorRange() {
            //int[] keepMin = {10, 160, 70};
            //int[] keepMax = {60, 255, 240};
            int[] keepMin = {10, 200, 70};
            int[] keepMax = {40, 255, 240};
            return new ColorRange(keepMin, keepMax, true);
    }

    /**
     * Helper method to provide a single location that creates the image filter
     * used to go from a gray scale image produced from the HSV filter to a
     * black and white image.
     *
     * @return A image filter that converts a gray scale image to a black and
     * white image.
     */
    public static BlackWhite createBlackWhite() {
        return new BlackWhite(60, 255, false);
    }
    
    /**
     * Filter to crop to the region where we expect to find the stanchions
     * 
     * @return A new crop filter for region where stanchions should be.
     */
    public static MatFilter createCropFilter() {
    	return new Crop(0, 10, 320, 190);
    }
    
    /**
     * Creates a full sequence to reduce a source image to a black and white image we can use to search for stanchions.
     * 
     * @param red Pass true if you want to find red stanchions (false if not).
     * @param crop Pass true if you want the initial crop applied.
     * @return A sequence of steps to reduce a source image to a black and white image.
     */
    public static Sequence createSteps(boolean red, boolean crop) {
    	Sequence seq = new Sequence();
    	if (crop) {
    		seq.addFilter(createCropFilter());
    	}
    	seq.addFilter(ColorSpace.createBGRtoHSV());
    	seq.addFilter(red ? createRedColorRange() : createYellowColorRange());
    	seq.addFilter(new GrayScale());
    	seq.addFilter(createBlackWhite());
    	seq.addFilter(new Erode(ERODE_FACTOR));
    	seq.addFilter(new Dilate(DILATE_FACTOR));
    	return seq;
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
    	_Found = false;
        int hImg = srcImage.rows();
        int imgMid = hImg / 2;
        Mat output = _CropFilter.process(srcImage);

        Mat d1 = _Filter.process(output.clone());

        // Uncomment to see final black and white image being processed
        //Imgproc.cvtColor(d1, output, Imgproc.COLOR_GRAY2BGR);
        
        List<MatOfPoint> contours = new ArrayList<>();
        List<PolygonCv> polygons = new ArrayList<>();

        Mat heirarchy = new Mat();
        Imgproc.findContours(d1, contours, heirarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        int n = contours.size();
        for (int i = 0; i < n; i++) {
            MatOfPoint contour = contours.get(i);
            // Hmmm, can we do a quick check on contour height/width before
            // trying to extract polygon?
            PolygonCv poly = PolygonCv.fromContour(contour, 8.0);
            int pts = poly.size();
            float h = poly.getHeight();
            float w = poly.getWidth();
            int hw = (int) (w > 0 ? h / w * 100: 0);
            float distFromTop = poly.getMinY();
            float distFromMid = imgMid - (distFromTop + h); 

            if ((w > 5) && (h > 15) && (hw > 50) && (hw < 800) && (pts >= 4) && (pts < 20) && (distFromTop > distFromMid) && (distFromTop < imgMid)) {
                polygons.add(poly);
                _Found = true;
                if (_Debug) {
                System.out.println("Accepted: sides: " + pts + " ("
                                   + poly.getWidth() + ", " + poly.getHeight()
                                   + ")  H/W: " + hw + "  distFromTop: " + distFromTop + "  distFromMid: " + distFromMid);
                }

            } else if (_Debug) {
                System.out.println("Rejected: sides: " + pts + " ("
                                   + poly.getWidth() + ", " + poly.getHeight()
                                   + ")  H/W: " + hw + "  distFromTop: " + distFromTop + "  distFromMid: " + distFromMid);
           }
        }

        int pCnt = polygons.size();
        PolygonCv[] pArr = new PolygonCv[pCnt];
        polygons.toArray(pArr);

        List<MatOfPoint> reduced = new ArrayList<>();

        // Set this variable to false to see ALL polygons found so far.
        // Set it to true to only see pairs of polygons where one is inside the other.
        boolean searchForPairs = false;

        if (searchForPairs) {
            // Start checking at the second item.
            for (int i = 1; i < pCnt; i++) {
                PolygonCv pi = pArr[i];

                // Check against all other prior polygons that we haven't used
                for (int j = 0; j < i; j++) {
                    PolygonCv pj = pArr[j];

                    // If prior polygon not used yet and it contains us or we contain it, then add both to the list of found targets.
                    // (We might want to change this to just adding the outer polygon).
                    if ((pj != null) && (pi.contains(pj) || pj.contains(pi))) {
                        reduced.add(pi.toContour());
                        reduced.add(pj.toContour());
                        pi.drawInfo(output, _Color);
                        pj.drawInfo(output, _Color);

                        // Indicate that we have used both polygons (prevent them
                        // from being used again)
                        pArr[i] = pArr[j] = null;
                    }
                }
            }
        } else {
            // Not searching for pairs, just add contours for all of the polygons
            for (int i = 0; i < pCnt; i++) {
                reduced.add(pArr[i].toContour());
            }
        }

        // Draw lines for all polygons that we found
        Imgproc.drawContours(output, reduced, -1, _Color, _Thickness);

        return output;
    }

    /**
     * Returns true if we found a stanchion the last time we processed an image.
     * 
     * @return true if stanchion was found, false if not.
     */
	public boolean foundStanchion() {
		return _Found;
	}
}
