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
import org.opencv.core.Size;

import com.techhounds.imgcv.utils.FovCalculator;

/**
 * Image filter draws a cross hair across the source image at the specified
 * location.
 *
 * @author Paul Blankenbaker
 */
public final class FovOverlay implements MatFilter {

	// Color of lines
	private Scalar _LineColor = new Scalar(100, 100, 100);

	// Text color
	private Scalar _TextColor = new Scalar(180, 180, 0);

	// Whether to enable horizontal FOV overlay
	private boolean _HorizontalEnable = true;

	// Whether to enable the vertical FOV overlay
	private boolean _VerticalEnable = false;

	// FOV in degrees in horizontal
	// (Paul's ThinkPAD Web Cam is 56.75
	private double _FovDeg = 56.75;

	// Distance to FOV
	private double _Distance = 100;

	// Degree spacing for lines shown
	private double _DegSpacing = 5;

	/**
	 * Construct a new instance with default values.
	 */
	public FovOverlay() {

	}

	/**
	 * Construct a new instance and load settings (if available) from the named
	 * preference.
	 * 
	 * @param prefsKey
	 *            Key used to load preferences from.
	 */
	public FovOverlay(String prefsKey) {
		loadPreferences(prefsKey);
	}

	/**
	 * Load setting from system preferences.
	 * 
	 * @param prefsKey
	 *            The key to use to retrieve settings from.
	 */
	public void loadPreferences(String prefsKey) {

	}

	/**
	 * Enable/disable the display of the horizontal FOV (vertical lines).
	 * 
	 * @param enable
	 *            Pass true to enable (enabled by default).
	 */
	public void setHorizontalVisible(boolean enable) {
		_HorizontalEnable = enable;
	}

	/**
	 * Enable/disable the display of the vertical FOV (horizontal lines).
	 * 
	 * @param enable
	 *            Pass true to enable (disabled by default).
	 */
	public void setVerticalVisible(boolean enable) {
		_VerticalEnable = enable;
	}

	/**
	 * Set the distance to the "wall" that contains the image being rendered.
	 * 
	 * @param dist
	 *            The distance in real world units (we don't care what units you
	 *            use - results will be in same units).
	 */
	public void setDistance(double dist) {
		_Distance = dist;
	}

	/**
	 * Sets the degree spacing between each line drawn.
	 * 
	 * @param degSpacing
	 *            Spacing between lines (in degrees).
	 */
	public void setDegSpacing(double degSpacing) {
		_DegSpacing = degSpacing;
	}

	/**
	 * Set the total FOV of the camera across the horizontal of the image.
	 * 
	 * @param fovDeg
	 *            Field of view in the horizontal dimension for the camera (must
	 *            be greater than 0).
	 */
	public void setFovDeg(double fovDeg) {
		if (fovDeg <= 0) {
			throw new IllegalArgumentException("FOV must be a positive number");
		}
		_FovDeg = fovDeg;
	}

	/**
	 * Set the color the FOV lines - default is new Scalar(100, 100, 100).
	 * 
	 * @param color
	 *            To draw the FOV lines with or null to turn off line drawing.
	 */
	public void setLineColor(Scalar color) {
		_LineColor = color;
	}

	/**
	 * Set the color the FOV line labels - default is new Scalar(180, 180, 0).
	 * 
	 * @param color
	 *            To draw the FOV lines with or null to turn off text labels.
	 */
	public void setTextColor(Scalar color) {
		_TextColor = color;
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
		int[] baseline = { 0 };
		int fontFace = Core.FONT_HERSHEY_PLAIN;
		double fontScale = 0.75;
		int thickness = 1;
		int textGap = 2;

		int w = srcImage.cols();
		if (_HorizontalEnable) {
			FovCalculator fovCalc = new FovCalculator(_FovDeg, w, _Distance);
			fovCalc.drawHorizontal(srcImage, _DegSpacing, _LineColor, _TextColor);

			if (_TextColor != null) {
				String text = "Hor: " + fovCalc.toString();
				Size size = Core.getTextSize(text, fontFace, fontScale, thickness, baseline);

				int textY = textGap + (int) size.height;
				int textX = w - textGap - (int) size.width;
				Point p = new Point(textX, textY);
				Core.putText(srcImage, text, p, fontFace, 0.75, _TextColor, 1);
			}
		}

		if (_VerticalEnable) {
			int h = srcImage.rows();
			// Base FOV in vertical off horizontal
			FovCalculator fovCalcHor = new FovCalculator(_FovDeg, w, _Distance);
			FovCalculator fovCalc = new FovCalculator(fovCalcHor, h);
			fovCalc.drawVertical(srcImage, _DegSpacing, _LineColor, _TextColor);

			if (_TextColor != null) {
				String text = "Ver: " + fovCalc.toString();
				Size size = Core.getTextSize(text, fontFace, fontScale, thickness, baseline);

				int textY = h - textGap - (int) size.height;
				int textX = w - textGap - (int) size.width;
				Point p = new Point(textX, textY);
				Core.putText(srcImage, text, p, fontFace, 0.75, _TextColor, 1);
			}
		}

		return srcImage;
	}
}
