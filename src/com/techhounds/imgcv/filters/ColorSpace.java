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
 * Converts the image from one color space (BGR, RGB, HSV, ...) to another.
 *
 * <p>
 * This filter is used to convert a image from one color space to another.</p>

 * @author Paul Blankenbaker
 */
public final class ColorSpace implements MatFilter {

    /**
     * The color conversion mode to use.
     */
    private final int _Mode;

    /**
     * Creates a new instance of the color space conversion filter.
     *
     * <p>
     * Hint: You can use some of the static helper methods if you don't want to
     * look up the opencv color space conversion constants.</p>
     *
     * @param mode One of the opencv color conversion mode constants indicating
     * what type of color space conversion you want to perform
     * ({@link Imgproc#COLOR_BGR2HSV}, {@link Imgproc#COLOR_HSV2BGR}), etc).
     */
    public ColorSpace(int mode) {
        _Mode = mode;
    }

    /**
     * Create a BGR to HSV color space conversion filter.
     *
     * @return Filter that converts from the BGR color space to HSV colorspace.
     */
    public static ColorSpace createBGRtoHSV() {
        return new ColorSpace(Imgproc.COLOR_BGR2HSV);
    }

    /**
     * Create a HSV to BGR color space conversion filter.
     *
     * @return Filter that converts from the BGR color space to HSV colorspace.
     */
    public static ColorSpace createHSVtoBGR() {
        return new ColorSpace(Imgproc.COLOR_HSV2BGR);
    }

    /**
     * Create a RGB to HSV color space conversion filter.
     *
     * @return Filter that converts from the RGB color space to HSV colorspace.
     */
    public static ColorSpace createRGBtoHSV() {
        return new ColorSpace(Imgproc.COLOR_RGB2HSV);
    }

    /**
     * Create a HSV to RGB color space conversion filter.
     *
     * @return Filter that converts from the HSV color space to RGB colorspace.
     */
    public static ColorSpace createHSVtoRGB() {
        return new ColorSpace(Imgproc.COLOR_HSV2RGB);
    }

    /**
     * Create a BGR to XYZ color space conversion filter.
     *
     * @return Filter that converts from the BGR color space to XYZ colorspace.
     */
    public static ColorSpace createBGRtoXYZ() {
        return new ColorSpace(Imgproc.COLOR_BGR2XYZ);
    }

    /**
     * Create a XYZ to BGR color space conversion filter.
     *
     * @return Filter that converts from the XYZ color space to BGR colorspace.
     */
    public static ColorSpace createXYZtoBGR() {
        return new ColorSpace(Imgproc.COLOR_XYZ2BGR);
    }

    /**
     * Create a RGB to XYZ color space conversion filter.
     *
     * @return Filter that converts from the RGB color space to XYZ colorspace.
     */
    public static ColorSpace createRGBtoXYZ() {
        return new ColorSpace(Imgproc.COLOR_RGB2XYZ);
    }

    /**
     * Create a XYZ to RGB color space conversion filter.
     *
     * @return Filter that converts from the XYZ color space to RGB colorspace.
     */
    public static ColorSpace createXYZtoRGB() {
        return new ColorSpace(Imgproc.COLOR_XYZ2RGB);
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param srcImage - The source image to be processed (passing {@code null}
     * is not permitted).
     *
     * @return Applies the {@link Imgproc#cvtColor} opencv function to the image
     * to convert between color space according to the mode the object was
     * constructed with. NOTE: This re-uses the srcImage for the results.
     */
    @Override
    public Mat process(Mat srcImage) {
        Mat dst = srcImage;
        Imgproc.cvtColor(srcImage, dst, _Mode);
        return dst;
    }
}
