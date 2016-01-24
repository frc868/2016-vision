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
import org.opencv.core.Mat;

/**
 * Image filter which can be used to build a sequence of other filters.
 *
 * <p>
 * This filter allows you to build a composite filter that is the result of
 * applying a sequence of {@link MatFilter} objects in a specific order. To use:
 * </p>
 * 
 * <ul>
 * <li>Construct a new instance.</li>
 * <li>Add one or more {@link MatFilter} objects in the order you want them
 * applied.</li>
 * <li>Use this filter like any other {@link MatFilter}.</li>
 * </ul>
 * 
 * @author Paul Blankenbaker
 */
public final class Sequence implements MatFilter {
	/** Holds list of image filters to apply. */
	private final ArrayList<MatFilter> _Filters;

	/**
	 * Constructs a new instance with no initial filters.
	 */

	public Sequence() {
		_Filters = new ArrayList<>();
	}

	/**
	 * Adds a new filter to be applied to the image after all of the previously
	 * added filters have been applied.
	 * 
	 * @param filter
	 *            New image filter to add to list (must not be null).
	 */
	public void addFilter(MatFilter filter) {
		_Filters.add(filter);
	}

	/**
	 * Method to filter a source image and return the filtered results.
	 *
	 * @param img
	 *            - The source image to be processed (passing {@code null} is
	 *            not permitted).
	 *
	 * @return The result of applying all of the filters in the order they were
	 *         added to this object.
	 */
	@Override
	public Mat process(Mat img) {
		for (MatFilter filter : _Filters) {
			img = filter.process(img);
		}
		return img;
	}

	/**
	 * Returns the total number of steps (stages) in the sequence (how many
	 * filters we apply).
	 * 
	 * @return Number of filters that have been added to the sequence.
	 */
	public int steps() {
		return _Filters.size();
	}

	/**
	 * Creates a new Sequence filter that includes all of the steps up to the
	 * specified index.
	 * 
	 * @param step
	 *            How many of the steps to add (in the range of [0,
	 *            {@link #steps()}]). NOTE: Passing 0 creates a empty sequence
	 *            (no filtering).
	 * @return A new sequence filter having a subset of the original.
	 */
	public Sequence createStepFilter(int step) {
		Sequence seq = new Sequence();
		for (int i = 0; i < step; i++) {
			seq.addFilter(_Filters.get(i));
		}
		return seq;
	}
}
