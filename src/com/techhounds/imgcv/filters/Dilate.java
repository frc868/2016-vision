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

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * A dilation filter (causes objects to fill-in and grow).
 *
 * <p>
 * This filter is typically applied to black and white images and is useful to
 * clean up speckles (missing dots). It "grows" the white areas a bit in a black
 * and white image.</p>
 *
 * @author Paul Blankenbaker
 */
public final class Dilate implements MatFilter {

    /**
     * The kernel to use when dilating the image (lager sized kernels cause
     * larger growth).
     */
    private Mat _Kernel;

    /**
     * Construct a new instance with a specific kernel matrix.
     *
     * @param kernel A small matrix used by the opencv algorithm when dilating
     * the image.
     */
    private Dilate(Mat kernel) {
        _Kernel = kernel;
    }

    /**
     * Construct a new instance of the filter with a specific dilation size.
     *
     * @param size The size (in pixels) to dilate the image by (for example, if
     * you pass 5 it would result in a 5x5 kernel producing a fairly large
     * amount of dilation)
     */
    public Dilate(int size) {
        this(Mat.ones(size, size, CvType.CV_8U));
    }

    /**
     * Construct a new instance of the filter with a 3 pixel dilation.
     */
    public Dilate() {
        this(3);
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param srcImage - The source image to be processed (passing {@code null}
     * is not permitted).
     *
     * @return The image after applying the
     * {@link Imgproc#dilate(org.opencv.core.Mat, org.opencv.core.Mat, org.opencv.core.Mat)}
     * filter. NOTE: This method re-uses the source image (your original image
     * is replaced).
     */
    @Override
    public Mat process(Mat srcImage) {
        Mat dst = srcImage;
        Imgproc.dilate(srcImage, dst, _Kernel);
        return dst;
    }
}
