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

import org.opencv.core.Mat;

/**
 * Adjusts the brightness and contrast of a image.
 *
 * <p>
 * This filter is used to adjust the contrast and/or brightness of a image by applying the following function to every pixel in the image:</p>
 * 
 * <pre>
 * Pnew = G * Pold + B;
 * 
 * Pnew: New pixel value
 * Pold: Old pixel value
 * 
 * G: Gain multiplier (contrast adjustment)
 * B: Bias adjustment (brightness adjustment)
 * </pre>
 * 
 * 
 *
 * @author Paul Blankenbaker
 */
public final class ContrastBrightness implements MatFilter {

    /**
     * The multiplier to apply to every pixel (contrast).
     */
    private final double _Gain;
    
    /**
     * The adjustment to add to every pixel (brightness).
     */
    private final double _Bias;

    /**
     * Creates a new instance of the color space conversion filter.
     *
     * <p>
     * Hint: You can use some of the static helper methods if you don't want to
     * look up the opencv color space conversion constants.</p>
     *
     * @param gain The value to multiply each pixel value by (< 1.0 reduce
     * constrast and > 1.0 to increase contrast).
     * @param bias The value to add to each pixel value (< 0 to decrease
     * brightness > 1 to increase brightness).
     */
    public ContrastBrightness(double gain, double bias) {
        _Gain = gain;
        _Bias = bias;
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param srcImage - The source image to be processed (passing {@code null}
     * is not permitted).
     *
     * @return Applies gain (constrast) and bias (brightness) values passed to
     * the constructor to every pixel in the image producing a new image (your
     * source image remains untouched).
     */
    @Override
    public Mat process(Mat srcImage) {
        Mat dst = Mat.zeros(srcImage.size(), srcImage.type());
        srcImage.convertTo(dst, -1, _Gain, _Bias);
        return dst;
    }
}
