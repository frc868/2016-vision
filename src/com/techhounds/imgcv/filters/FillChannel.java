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
 * Image filter to fill a color channel with a fixed value - typically used to
 * "remove" a channel.
 *
 * @author Paul Blankenbaker
 */
public final class FillChannel implements MatFilter {

    /**
     * The channel to fill.
     */
    private int _Channel;
    /**
     * The value to fill the channel with.
     */
    private int _Value;

    /**
     * Construct a instance which sets a color channel to a specific value.
     *
     * @param channel The index of the color channel that should be filled.
     * @param value The value to fill the channel with.
     */
    public FillChannel(int channel, int value) {
        _Channel = channel;
        _Value = value;
    }

    /**
     * Construct a instance which removes a channel (fills channel with zeros).
     *
     * @param channel The index of the color channel that should be filled.
     */
    public FillChannel(int channel) {
        this(channel, 0);
    }

    /**
     * Method to filter a source image and return the filtered results.
     *
     * @param img The source image to be processed (passing {@code null} is not
     * permitted).
     *
     * @return The filtered image which had a channel filled. NOTE: This filter
     * modifies the contents of the img passed in.
     */
    @Override
    public Mat process(Mat img) {
        int nchannels = img.channels();
        if (nchannels > _Channel) {
            int nrows = img.rows();
            int ncols = img.cols();
            byte[] colors = new byte[nchannels];
            for (int row = 0; row < nrows; row++) {
                for (int col = 0; col < ncols; col++) {
                    img.get(row, col, colors);
                    colors[_Channel] = (byte) _Value;
                    img.put(row, col, colors);
                }
            }
        }
        return img;
    }
}
