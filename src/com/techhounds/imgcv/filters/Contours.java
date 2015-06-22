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

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * A image filter which accepts a single channel (typically black & white) image
 * and attempts to find shapes and trace them with contour lines.
 *
 * @author Paul Blankenbaker
 */
public final class Contours implements MatFilter {

    /**
     * The contour mode to use - see opencv API.
     */
    private int _Mode;

    /**
     * The contour method to use - see opencv API.
     */
    private int _Method;

    /**
     * Color to use when drawing contour lines.
     */
    private final Scalar _Color;

    /**
     * Construct a new instance using a specific mode and method.
     *
     * @param mode The contour mode to use (see
     * {@link Imgproc#findContours(org.opencv.core.Mat, java.util.List, org.opencv.core.Mat, int, int)}).
     * @param method The contour method to use (see
     * {@link Imgproc#findContours(org.opencv.core.Mat, java.util.List, org.opencv.core.Mat, int, int)}).
     */
    public Contours(int mode, int method) {
        _Mode = mode;
        _Method = method;
        double[] colors = {50, 255, 100};
        _Color = new Scalar(colors);
    }

    /**
     * Construct a new instance of the object using the
     * {@link Imgproc#RETR_LIST} mode and {@link Imgproc#CHAIN_APPROX_SIMPLE}
     * method.
     */
    public Contours() {
        this(Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param srcImage - The source image to be processed (passing {@code null}
     * is not permitted). NOTE: This filter expects a single channel image. If
     * you pass it a multi-channel image, it will try to convert it to gray
     * scale.
     *
     * @return A reference to a image having the filter applied. Searches for
     * objects in image and draws contour lines around them.
     */
    @Override
    public Mat process(Mat srcImage) {
        // If multi-channel image, try to convert to single scale
        if (srcImage.channels() != 1) {
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_RGB2GRAY);
        }

        // Need a color image to draw final contours on
        Mat dst = new Mat(srcImage.rows(), srcImage.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(srcImage, dst, Imgproc.COLOR_GRAY2BGR);

        // Search for countours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat heirarchy = new Mat();
        Imgproc.findContours(srcImage, contours, heirarchy, _Mode, _Method);

        // Draw all found contours onto output image
        Imgproc.drawContours(dst, contours, -1, _Color);
        return dst;
    }
}
