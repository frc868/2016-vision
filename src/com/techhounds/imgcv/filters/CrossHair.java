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

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Image filter draws a cross hair across the source image at the specified
 * location.
 *
 * @author Paul Blankenbaker
 */
public final class CrossHair implements MatFilter {

	// Used computing location of crosshairs 
	private static final int SCALE_FACTOR = 10000;
	
	// Position of horizontal line proportion of SCALE_FACTOR
	private int yMul = SCALE_FACTOR / 2;

	// Position of vertical line proportion of SCALE_FACTOR
	private int xMul = SCALE_FACTOR / 2;

	// Thickness of lines in pixels
	private int thickness = 1;
	
	// Color of lines
	private Scalar color = new Scalar(255, 255, 0);

	/**
	 * By default the horizontal line appears .5 (50%) of the way down the
	 * screen - use this method to adjust.
	 * 
	 * @param fraction
	 *            Fractional portion of screen height to position the horizontal
	 *            line at (in the range of 0.0 to 1.0).
	 */
	public void setHorizontal(double fraction) {
		yMul = (int) (fraction * SCALE_FACTOR);
	}

	/**
	 * By default the vertical line appears .5 (50%) of the way across the
	 * screen - use this method to adjust.
	 * 
	 * @param fraction
	 *            Fractional portion of screen width to position the vertical
	 *            line at (in the range of 0.0 to 1.0).
	 */
	public void setVertical(double fraction) {
		xMul = (int) (fraction * SCALE_FACTOR);
	}

	/**
	 * Set the thickness of the cross hair line (in pixels).
	 * 
	 * @param thickness
	 *            in pixels to draw the line with.
	 */
	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	/**
	 * Set the color the cross hair line - default is new Scalar(255, 255, 0).
	 * 
	 * @param color
	 *            to draw the lines with.
	 */
	public void setColor(Scalar color) {
		if (color == null) {
			throw new NullPointerException();
		}
		this.color = color;
	}

	/**
	 * Method to filter a source image and return the filtered results.
	 *
	 * @param srcImage
	 *            - The source image to be processed (passing {@code null} is
	 *            not permitted).
	 *
	 * @return The original srcImage with a horizontal and vertical line drawn
	 *         on it.
	 */
	@Override
	public Mat process(Mat srcImage) {
		int w = srcImage.cols();
		int h = srcImage.rows();
		int y = h * yMul / SCALE_FACTOR;
		int x = w * xMul / SCALE_FACTOR;

		Point left = new Point(0, y);
		Point right = new Point(w - 1, y);
		Core.line(srcImage, left, right, color, thickness);

		Point top = new Point(x, 0);
		Point bot = new Point(x, h - 1);
		Core.line(srcImage, top, bot, color, thickness);

		return srcImage;
	}
}
