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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import com.techhounds.imgcv.utils.ColorRangeValues;
import com.techhounds.imgcv.widgets.ColorRangeEditor;

/**
 * A image filter which keeps/removes pixels which fall within a specific color
 * range.
 *
 * <p>
 * This filter is useful if you are only interested in pixels having colors
 * within a certain range. You use it in the following manner:
 * </p>
 *
 * <ul>
 * <li>You create two arrays holding the minimum and maximum values you are
 * interested for each channel (color).</li>
 * <li>You construct a new instance passing the min/max ranges and whether you
 * want to keep or remove the pixels inside the specified range.</li>
 * <li>You then {@link #process(org.opencv.core.Mat) apply} the filter.</li>
 * </ul>
 *
 * @author Paul Blankenbaker
 */
public final class ColorRange implements MatFilter {

	/**
	 * The color range values currently being used.
	 */
	ColorRangeValues _Values;

	/**
	 * Lower bounds of color ranges for Core.inRange() optimized invocation
	 * (will be null if we can't used the optimized method).
	 */
	private Scalar _KeepAllLower;

	/**
	 * Upper bounds of color ranges for Core.inRange() optimized invocation
	 * (will be null if we can't used the optimized method).
	 */
	private Scalar _KeepAllUpper;

	/**
	 * Construct a new instance of the range filter and choose whether to keep
	 * or remove the pixels on each range.
	 *
	 * @param minVals
	 *            A array of lower level limits for each color channel (must be
	 *            at least 1 in length).
	 * @param maxVals
	 *            A array of upper level limits for each color channel (must be
	 *            same size as minVals array).
	 * @param keep
	 *            An array of booleans indicating if you want to keep or exclude
	 *            values that fall within each range (must be same size as
	 *            minVals array).
	 */
	public ColorRange(int[] minVals, int[] maxVals, boolean[] keep) {
		setRanges(minVals, maxVals, keep);
	}

	/**
	 * Construct a new instance of the range filter and choose whether to keep
	 * or remove the pixels inside the ranges.
	 *
	 * @param minVals
	 *            A array of lower level limits for each color channel (must be
	 *            at least 1 in length).
	 * @param maxVals
	 *            A array of upper level limits for each color channel (must be
	 *            same size as minVals array).
	 * @param keep
	 *            Pass true to keep the pixels within all of the ranges. Pass
	 *            false to remove the pixels outside all of the ranges.
	 */
	public ColorRange(int[] minVals, int[] maxVals, boolean keep) {
		setRanges(minVals, maxVals, keep);
	}
	
	/**
	 * Sets the values to use when applying the filter to images.
	 * 
	 * @param crv New color range values to use - must not be null.
	 */
	public void setColorRangeValues(ColorRangeValues crv) {
		_Values = new ColorRangeValues(crv);

		if (crv.getKeepInRangeAll()) {
			// Keep in range is true for all channels, enable optimized
			// color range check
			_KeepAllLower = crv.getMinScalar();
			_KeepAllUpper = crv.getMaxScalar();
		} else {
			// One or more channels want to keep values out of range, fall
			// back to slower check
			_KeepAllLower = null;
			_KeepAllUpper = null;
		}

	}

	/**
	 * Returns a copy of the current color range values being used by the filter.
	 * 
	 * @return A copy of the current values.
	 */
	public ColorRangeValues getColorRangeValues() {
		return new ColorRangeValues(_Values);
	}

	/**
	 * Method to change the ranges used on each channel when processing images.
	 *
	 * @param minVals
	 *            A array of lower level limits for each color channel (must be
	 *            at least 1 in length).
	 * @param maxVals
	 *            A array of upper level limits for each color channel (must be
	 *            same size as minVals array).
	 * @param keep
	 *            An array of booleans indicating if you want to keep or exclude
	 *            values that fall within each of the ranges (must be same size
	 *            as minVals array).
	 */
	public void setRanges(int[] minVals, int[] maxVals, boolean[] keep) {
		int channels = minVals.length;

		if ((channels != maxVals.length) || (channels == 0)) {
			throw new IllegalArgumentException(
					"Color range arrays must have non-zero matching lengths");
		}
		if (keep.length != channels) {
			throw new IllegalArgumentException(
					"Keep flags length does not match the number of channels");
		}
		
		ColorRangeValues crv = new ColorRangeValues(channels);
		for (int i = 0; i < channels; i++) {
			crv.setKeepInRange(i, keep[i]);
			crv.setMax(i, maxVals[i]);
			crv.setMin(i, minVals[i]);
		}

		setColorRangeValues(crv);
	}

	/**
	 * Method to change the ranges used on each channel when processing images.
	 *
	 * @param minVals
	 *            A array of lower level limits for each color channel (must be
	 *            at least 1 in length).
	 * @param maxVals
	 *            A array of upper level limits for each color channel (must be
	 *            same size as minVals array).
	 * @param keep
	 *            Pass true to keep the pixels within all of the ranges. Pass
	 *            false to remove the pixels outside all of the ranges.
	 */
	public void setRanges(int[] minVals, int[] maxVals, boolean keep) {
		// Create keep array to match range sizes
		boolean[] keepChannels = new boolean[minVals.length];
		Arrays.fill(keepChannels, keep);
		setRanges(minVals, maxVals, keepChannels);
	}

	/**
	 * Method apply the filter a source image and return the filtered results.
	 *
	 * @param img
	 *            - The image to be processed (passing {@code null} is not
	 *            permitted).
	 *
	 * @return This filter modifies the contents of the img passed in and
	 *         returns a reference to the same img passed in.
	 */
	@Override
	public Mat process(Mat img) {
		int nchannels = img.channels();
		int ncolors = _Values.size();

		// If only keeping values within range and number of channels
		// match, use native OpenCV inrange function to optimize performance
		if ((_KeepAllLower != null) && (nchannels == ncolors)) {
			Core.inRange(img, _KeepAllLower, _KeepAllUpper, img);
			return img;
		}

		if (nchannels <= ncolors) {
			int nrows = img.rows();
			int ncols = img.cols();
			byte[] colors = new byte[nchannels];
			byte[] clear = new byte[nchannels];

			for (int row = 0; row < nrows; row++) {
				for (int col = 0; col < ncols; col++) {
					img.get(row, col, colors);

					boolean outOfRange = false;
					for (int c = 0; c < nchannels; c++) {
						int color = 0xff & ((int) colors[c]);
						if (!_Values.inRange(c, color)) {
							outOfRange = true;
							break;
						}
					}
					// If pixel not in desired range, then clear value
					if (outOfRange) {
						img.put(row, col, clear);
					}
				}
			}
		}
		return img;
	}

	/**
	 * Returns a copy of the upper limits for all channels.
	 * 
	 * @return Array or maximum values used in color range checks.
	 */
	public int[] getMaxVals() {
		return _Values.getMax();
	}
	
	/**
	 * Returns a copy of the lower limits for all channels.
	 * 
	 * @return Array or minimum values used in color range checks.
	 */	
	public int[] getMinVals() {
		return _Values.getMin();
	}
	

	/**
	 * Builds a action handler to displays a color range editor to allow the
	 * user to quickly see the impact of adjusting the color ranges.
	 *
	 * @param Name
	 *            The ASCII name to associate with the color range set.
	 * @param defCfgName
	 *            The default configuration name (like: "pink") to use when user
	 *            presses save defaults or load defaults.
	 * @return Action that can be assigned to a button or menu item.
	 */
	public Action getColorRangeAction(final String name, final String defCfgName) {

		Action action = new AbstractAction(name) {
			private static final long serialVersionUID = 1L;

			private JPanel createColorRangeEditor() {
				final ColorRangeEditor cre = new ColorRangeEditor(getColorRangeValues());
				cre.setDefaultCfgName(defCfgName);
				cre.addListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						// Transfer values to filter as user adjusts them
						setColorRangeValues(cre.getValues());
					}
				});
				return cre;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				// When action occurs, create and display editor
				JFrame frame = new JFrame(name);
				frame.setMinimumSize(new Dimension(480, 200));
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setContentPane(createColorRangeEditor());
				frame.pack();
				frame.setVisible(true);
			}
		};
		return action;
	}

}
