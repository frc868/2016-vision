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
package com.techhounds.imgcv.frc2013;

import com.techhounds.imgcv.PolygonCv;
import com.techhounds.imgcv.filters.BlackWhite;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.ColorSpace;
import com.techhounds.imgcv.filters.Dilate;
import com.techhounds.imgcv.filters.Erode;
import com.techhounds.imgcv.filters.GrayScale;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.Sequence;

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
public final class FindTarget2013 implements MatFilter {

    /**
     * Used to filter image to a specific color range.
     */
    private final MatFilter _ColorRange;

    /**
     * Used to convert image to a gray scale rendering.
     */
    private final GrayScale _GrayScale;

    /**
     * Used to convert from gray scale to black and white.
     */
    private final BlackWhite _BlackWhite;

    /**
     * Grows pixels out a bit.
     */
    private final Dilate _Dilate;

    /**
     * Shrinks pixel areas back down a bit.
     */
    @SuppressWarnings("unused")
	private final Erode _Erode;

    /**
     * Used as color for overlays.
     */
    private final Scalar _Color;

    /**
     * Thickness of lines used when drawing overlays.
     */
    private final int _Thickness;
    @SuppressWarnings("unused")
	private final ColorSpace _BgrToHsv;
    @SuppressWarnings("unused")
	private final MatFilter _HsvColorRange;
    private final boolean _UseHsv;

    /**
     * Constructs a new instance by pre-allocating all of our image filtering
     * objects.
     */
    public FindTarget2013() {
        // Whether we want to convert to HSV to find colors or not.
        _UseHsv = true;

        _ColorRange = _UseHsv ? createHsvColorRange() : createColorRange();
        _HsvColorRange = createHsvColorRange();
        _BgrToHsv = ColorSpace.createBGRtoHSV();
        _GrayScale = new GrayScale();
        _BlackWhite = _UseHsv ? createHsvBlackWhite() : createBlackWhite();
        _Dilate = new Dilate(3);
        _Erode = new Erode(6);
        double[] colors = {100, 100, 250};
        _Color = new Scalar(colors);
        _Thickness = 1;
    }

    /**
     * Helper method to provide a single location that creates the color range
     * filter used by this filter (in case you want to add it as a stand-alone
     * filter).
     *
     * @return A new {@link ColorRange} filter for the 2013 game.
     */
    public static ColorRange createColorRange() {
        int[] keepBlueMin = {10, 5, 0};
        int[] keepBlueMax = {Integer.MAX_VALUE, Integer.MAX_VALUE, 120};
        return new ColorRange(keepBlueMin, keepBlueMax, true);
    }

    /**
     * Helper method to provide a single location that creates and alternative
     * color range filter that converts the image to the HSV color space first
     * (in case you want to add it as a stand-alone filter).
     *
     * @return A new {@link ColorRange} filter for the 2013 game.
     */
    public static MatFilter createHsvColorRange() {
        Sequence filter = new Sequence();
        filter.addFilter(ColorSpace.createBGRtoHSV());
        int[] keepMin = {78, 146, 78};
        int[] keepMax = {131, 255, 255};
        filter.addFilter(new ColorRange(keepMin, keepMax, true));
        return filter;
    }

    /**
     * Helper method to provide a single location that creates the image filter
     * used to go from a gray scale image to a black and white image.
     *
     * @return A image filter that converts a gray scale image to a black and
     * white image.
     */
    public static BlackWhite createBlackWhite() {
        return new BlackWhite(40, 255, false);
    }

    /**
     * Helper method to provide a single location that creates the image filter
     * used to go from a gray scale image produced from the HSV filter to a
     * black and white image.
     *
     * @return A image filter that converts a gray scale image to a black and
     * white image.
     */
    public static BlackWhite createHsvBlackWhite() {
        return new BlackWhite(60, 255, false);
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
        Mat colorRange = _ColorRange.process(srcImage);

        //Mat output = colorRange.clone();
        Mat gray = _GrayScale.process(colorRange);
        Mat bw = _BlackWhite.process(gray);

        // Uncomment to see black and white image being processed
        // Imgproc.cvtColor(bw, output, Imgproc.COLOR_GRAY2BGR);
        Mat d1 = _Dilate.process(bw);
        // Uncomment to see final black and white image being processed
        // Imgproc.cvtColor(d1, output, Imgproc.COLOR_GRAY2BGR);

        //Mat e1 = _Erode.process(d1);
        //Mat d2 = _Dilate.process(e1);
        // Uncomment to see final black and white image being processed
        //Imgproc.cvtColor(d2, output, Imgproc.COLOR_GRAY2BGR);
        List<MatOfPoint> contours = new ArrayList<>();
        List<PolygonCv> polygons = new ArrayList<>();

        Mat heirarchy = new Mat();
        Imgproc.findContours(d1, contours, heirarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        int n = contours.size();
        for (int i = 0; i < n; i++) {
            MatOfPoint contour = contours.get(i);
            // Hmmm, can we do a quick check on contour height/width before
            // trying to extract polygon?
            PolygonCv poly = PolygonCv.fromContour(contour, 5.0);
            int sides = poly.size();
            if ((sides >= 4) && (sides <= 6) && (poly.getWidth() > 15) && (poly.getHeight() > 5) && (poly.getBoundingAspectRatio() > 1.25)) {
                polygons.add(poly);
/*
            } else {
                System.out.println("Rejected: sides: " + sides + " ("
                                   + poly.getWidth() + ", " + poly.getHeight()
                                   + ")  AR: " + poly.getBoundingAspectRatio());
*/
          }
        }

        int pCnt = polygons.size();
        PolygonCv[] pArr = new PolygonCv[pCnt];
        polygons.toArray(pArr);

        List<MatOfPoint> reduced = new ArrayList<>();

        // Set this variable to false to see ALL polygons found so far.
        // Set it to true to only see pairs of polygons where one is inside the other.
        boolean searchForPairs = true;

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
}
