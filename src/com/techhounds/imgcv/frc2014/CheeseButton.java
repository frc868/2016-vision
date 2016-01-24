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
package com.techhounds.imgcv.frc2014;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.techhounds.imgcv.filters.MatFilter;

/**
 * A image filter which performs all of the operations in an attempt to locate
 * the rectangular targe areas from the 2013 FRC competition.
 *
 * @author Paul Blankenbaker
 */
public final class CheeseButton implements MatFilter {

    /**
     * Used as color for outline of reference area.
     */
    private final Scalar _Color;
    
    /**
     * Color for outline when button is not covered.
     */
    private final Scalar _UncoveredColor;
    
    /**
     * Color for outline when button is covered.
     */
    private final Scalar _CoveredColor;

    /**
     * Thickness of lines used when drawing overlays.
     */
    private final int _Thickness;
    private int _RectHeight;
    private int _RectWidth;
    private final int _Gap;

    /**
     * Constructs a new instance by pre-allocating all of our image filtering
     * objects.
     */
    public CheeseButton() {
        double[] colors = {255, 100, 100};
        _Color = new Scalar(colors);
        
        double[] uncoveredColor = { 100, 255, 100 };
        _UncoveredColor = new Scalar(uncoveredColor);
        
        double[] coveredColor = { 100, 100, 255 };
        _CoveredColor = new Scalar(coveredColor);
        
        _Thickness = 5;
        
        _RectWidth = 80;
        _RectHeight = 60;
        _Gap = 50;
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
        Mat output = new Mat();
        //srcImage.copyTo(output);
        Core.flip(srcImage, output, 1);

        Scalar refColor = getColorValue(output, 0);
        Scalar buttonColor = getColorValue(output, 1);

        fillRect(output, 0, refColor);
        fillRect(output, 1, buttonColor);
       
        Scalar rectColor = _UncoveredColor;
        double[] ref = refColor.val;
        double[] button = buttonColor.val;
        int n = ref.length;
        if (n == button.length) {
           for (int i = 0; i < n; i++) {
              if (Math.abs(ref[i] - button[i]) > 20) {
                  rectColor = _CoveredColor;
                  break;
              }
           }
        }
       
        drawRect(output, 0, _Color);
        drawRect(output, 1, rectColor);
        
        System.out.println("refColor: " + refColor + "  buttonColor: " + buttonColor);

        return output;
    }

    private void drawRect(Mat output, int row, Scalar rectColor) {
        int cx = output.width() / 2;
        int rectLeft = cx - _RectWidth / 2;
        int rectTop = computeRowTop(output, row);
        Point p1 = new Point(rectLeft, rectTop);
        Point p2 = new Point(rectLeft + _RectWidth, rectTop + _RectHeight);
        Core.rectangle(output, p1, p2, rectColor, _Thickness);
    }

    private Scalar getColorValue(Mat output, int row) {
        int cx = output.width() / 2;
        int rowStart = computeRowTop(output, row);
        int rowEnd = rowStart + _RectHeight;
        int colStart = cx - _RectWidth / 2;
        int colEnd = colStart + _RectWidth;
        Mat piece = output.submat(rowStart, rowEnd, colStart, colEnd);
        return Core.mean(piece);
    }

    private int computeRowTop(Mat output, int row) {
        int cy = output.height() / 2;
        int rectTop = cy - ((1 - row) * (_RectHeight + (_Thickness * 2) + _Gap));
        return rectTop;
    }

    private void fillRect(Mat output, int row, Scalar color) {
        int cx = output.width() / 2;
        int rectLeft = cx - _RectWidth / 2;
        int rectTop = computeRowTop(output, row);
        Point p1 = new Point(rectLeft, rectTop);
        Point p2 = new Point(rectLeft + _RectWidth, rectTop + _RectHeight);
        Core.rectangle(output, p1, p2, color, -1);
    }
}
