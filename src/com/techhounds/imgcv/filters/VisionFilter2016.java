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
 * the rectangular target areas from the 2013 FRC competition.
 *
 * @author Paul Blankenbaker
 */
public final class VisionFilter2016 implements MatFilter {

    private final MatFilter _ColorRange; //Used to filter image to a specific color range.
    private final Dilate    _Dilate;     //grows remaining parts of the images
    private final Erode     _Erode;      //shrinks remaining parts of the images
    private final GrayScale _GrayScale; //Used to convert image to a gray scale rendering.
    private final BlackWhite _BlackWhite; //Used to convert from gray scale to black and white.

    //Constructs a new instance by pre-allocating all of our image filtering objects.
    public VisionFilter2016() { 
    	_ColorRange = createHsvColorRange();
    	_Dilate 	= new Dilate(3); //higher erode/dilate values create smoother close images
    	_Erode		= new Erode(4);  //but smaller and more broken far images
        _GrayScale = new GrayScale();
        _BlackWhite = createBlackWhite();
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
    
    public static MatFilter createHsvColorRange() {
        Sequence filter = new Sequence();
        filter.addFilter(ColorSpace.createBGRtoHSV());
        int[] colorMin = {70, 100, 025};
        int[] colorMax = {90, 255, 255};
        filter.addFilter(new ColorRange(colorMin, colorMax, true));
        return filter;
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
    	//takes previous filter '_ColorRange' and processes 'srcImage' with it
        Mat coloredImage = _ColorRange.process(srcImage); 
        Mat dilatedImage = _Dilate.process(coloredImage); //what if we erode first?
        Mat erodedImage  = _Erode.process(dilatedImage);
        Mat grayedImage  = _GrayScale.process(erodedImage);
        Mat bwImage      = _BlackWhite.process(grayedImage);
        Imgproc.cvtColor(bwImage, output, Imgproc.COLOR_GRAY2BGR);
	    
        return erodedImage;
    }
}
