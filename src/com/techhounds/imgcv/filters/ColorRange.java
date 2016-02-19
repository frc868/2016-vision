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
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

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
	 * The lower limits for each color channel.
	 */
	private int[] _MinVals;

	/**
	 * The upper limits for each color channel.
	 */
	private int[] _MaxVals;

	/**
	 * Whether we keep or remove the values inside the range.
	 */
	private boolean[] _Keep;

	/**
	 * List of listeners to notify if values are changed in GUI editor.
	 */
	private final ArrayList<ChangeListener> _Listeners;

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
		_Listeners = new ArrayList<>();
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
		_Listeners = new ArrayList<>();
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
		_MinVals = minVals.clone();
		_MaxVals = maxVals.clone();
		_Keep = keep.clone();
		boolean keepAll = true;
		double[] minScalar = new double[channels];
		double[] maxScalar = new double[channels];

		for (int i = 0; i < channels; i++) {
			keepAll = keepAll && _Keep[i];
			minScalar[i] = minVals[i];
			maxScalar[i] = maxVals[i];
		}

		if (keepAll) {
			// Keep in range is true for all channels, enable optimized
			// color range check
			_KeepAllLower = new Scalar(minScalar);
			_KeepAllUpper = new Scalar(maxScalar);
		} else {
			// One or more channels want to keep values out of range, fall
			// back to slower check
			_KeepAllLower = null;
			_KeepAllUpper = null;
		}

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
		int ncolors = _MinVals.length;

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
						boolean chanInRange = (color >= _MinVals[c])
								&& (color <= _MaxVals[c]);
						if (chanInRange != _Keep[c]) {
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
	 * Creates a GUI widget that allows the user to view/change the settings.
	 * 
	 * @return A Swing widget to be inserted into your GUI application.
	 */
	public JPanel createPreferencesPanel() {
		JPanel widget = new JPanel();
		widget.setLayout(new BoxLayout(widget, BoxLayout.Y_AXIS));

		int n = _MaxVals.length;
		final JCheckBox[] keepCheckboxes = new JCheckBox[n];
		final JSlider[] _MinSliders = new JSlider[n];
		final JSlider[] _MaxSliders = new JSlider[n];

		ChangeListener updateState = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// Transfer values from GUI
				int n = _MaxSliders.length;
				boolean[] keep = new boolean[n];
				int[] minVals = new int[n];
				int[] maxVals = new int[n];
				for (int i = 0; i < n; i++) {
					keep[i] = keepCheckboxes[i].isSelected();
					minVals[i] = _MinSliders[i].getValue();
					maxVals[i] = _MaxSliders[i].getValue();
				}
				setRanges(minVals, maxVals, keep);
				notifyListeners();
			}
		};

		for (int i = 0; i < n; i++) {
			JCheckBox keep = new JCheckBox("Channel " + i + " Keep in Range");
			keep.setSelected(true);
			keepCheckboxes[i] = keep;
			widget.add(keep);
			keep.addChangeListener(updateState);

			widget.add(new JLabel("Channel " + i + " Min"));
			JSlider minSlider = createSlider(_MinVals[i]);
			widget.add(minSlider);
			_MinSliders[i] = minSlider;
			minSlider.addChangeListener(updateState);

			widget.add(new JLabel("Channel " + i + " Max"));
			JSlider maxSlider = createSlider(_MaxVals[i]);
			widget.add(maxSlider);
			_MaxSliders[i] = maxSlider;
			maxSlider.addChangeListener(updateState);
		}

		return widget;
	}

	/**
	 * Register a listener to be notified if values are changed on the GUI
	 * widget.
	 * 
	 * @param l
	 *            The change listener that should be added.
	 */
	public void addListener(ChangeListener l) {
		_Listeners.add(l);
	}

	/**
	 * Unregister a listener to be notified if values are changed on the GUI
	 * widget.
	 * 
	 * @param l
	 *            The change listener that should no longer be notified.
	 */
	public void removeListener(ChangeListener l) {
		_Listeners.remove(l);
	}

	/**
	 * Helper method to notify all registered listeners that a value has
	 * changed.
	 */
	private void notifyListeners() {
		for (ChangeListener l : _Listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * Helper method to create a color slider widget.
	 * 
	 * @param val
	 *            Initial value for slider (we force to range of [0, 255]).
	 * @return A GUI widget to add to your panel.
	 */
	private JSlider createSlider(int val) {
		int minVal = 0;
		int maxVal = 255;
		JSlider slider = new JSlider(minVal, maxVal, Math.min(maxVal,
				Math.max(minVal, val)));
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		return slider;
	}
	
	public int[] getMaxVals() {
		return _MaxVals;
	}
	
	public int[] getMinVals() {
		return _MinVals;
	}
}
