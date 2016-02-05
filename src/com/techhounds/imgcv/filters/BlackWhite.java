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
import org.opencv.imgproc.Imgproc;

/**
 * Converts each channel in a image to a black and white representation.
 *
 * <p>
 * This filter is used to convert a image to it's "black and white"
 * representation. It is typically applied to gray scale images having a single
 * channel where each value ranges from 0 to 255.</p>
 *
 * <p>
 * When run, this filter goes through each channel in the image (if there is
 * more than one) and maps each value to either 0 or 255).</p>
 *
 * @author Paul Blankenbaker
 */
public final class BlackWhite implements MatFilter {

    /**
     * The threshold to exceed before a value is considered "on".
     */
    private final double _Thresh;
    /**
     * The maximum value allowed (typically 255).
     */
    private final double _MaxVal;
    /**
     * The type of conversion (whether to invert output values).
     */
    private final int _Type;

    /**
     * Creates a new instance of the black and white filter.
     *
     * @param thresh The value that must be met or exceeded for a pixel to be on
     * (typically in the range of [1, 254]).
     * @param maxval The maximum value permitted (typically 255).
     * @param invert Whether output should be inverted (pass true if you want
     * low values to be "white" and "high" values to be black).
     */
    public BlackWhite(double thresh, double maxval, boolean invert) {
        _Thresh = thresh;
        _MaxVal = maxval;
        _Type = (invert ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY);
    }

    /**
     * Creates a new instance of the black and white filter that uses 128 as the
     * threshold.
     */
    public BlackWhite() {
        this(128.0, 255, false);
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param srcImage - The source image to be processed (passing {@code null}
     * is not permitted).
     *
     * @return Applies the {@link Imgproc#threshold} opencv function to the
     * image to produce a "black and white" image according to the settings
     * passed to the constructor.
     */
    @Override
    public Mat process(Mat srcImage) {
        Mat dst = srcImage;
        Imgproc.threshold(srcImage, dst, _Thresh, _MaxVal, _Type);
        return dst;
    }
}
