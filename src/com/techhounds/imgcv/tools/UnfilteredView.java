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
package com.techhounds.imgcv.tools;

import com.techhounds.imgcv.LiveViewGui;
import com.techhounds.imgcv.filters.BlackWhite;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.ColorSpace;
import com.techhounds.imgcv.filters.Crop;
import com.techhounds.imgcv.filters.Dilate;
import com.techhounds.imgcv.filters.DoNothingFilter;
import com.techhounds.imgcv.filters.Erode;
import com.techhounds.imgcv.filters.GrayScale;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.Negative;
import com.techhounds.imgcv.filters.Sequence;

/**
 * A simple example of leveraging the {@link LiveViewGui} class to quickly test
 * a {@link MatFilter} against a stream of images.
 *
 * @author pkb
 */
public final class UnfilteredView extends LiveViewGui {
	
	/**
	 * Constructor needs to set the title (we'll set the default filter too).
	 */
	private UnfilteredView() {
		super("Live View Example");
		
		// A no-op filter, shows original
		setFilter(new DoNothingFilter());
	}
	
	/**
	 * Override the add menu items if you want to include additional filters.
	 */
	protected void addMenuItems() {
		super.addMenuItems();
		
		// Example of adding an option to the filter menu
		addFilter("Negative", new Negative());
		
		// Example of creating filter containing a sequence of filters
		// added in a way that we can observe each step of the sequence
		
		Sequence seq = new Sequence();
		// Take a vertical stripe of the original (50% of width down middle)
		seq.addFilter(new Crop(.25, 0, .75, 1.0));
		// Convert to HSV
		seq.addFilter(ColorSpace.createBGRtoHSV());
		
		// Look for "yellow" pixels
		int[] minVals = {20, 190, 120};
		int[] maxVals = {50, 255, 255};
		seq.addFilter(new ColorRange(minVals, maxVals, true));
		
		// Convert to gray scale
		seq.addFilter(new GrayScale());
		
		// Look for bright pixels
		seq.addFilter(new BlackWhite(150, 255, false));
		
		// Remove small stuff
		seq.addFilter(new Dilate(5));
		
		// Grow it back
		seq.addFilter(new Erode(5));
		addSequence("Yellow Items", seq);	
	}

    /**
     * Main entry point to this Java Application.
     * 
     * <ul>
     * <li>Creates a new LiveViewGui object to fetch/display data.</li>
     * <li>Sets the filter we want to apply to incoming images.</li>
     * <li>Starts up the GUI (you will need to select File|Start from menu).</li>
     * </ul>
     *
     * @param args Array of command line arguments (ignored).
     */
    public static void main(String[] args) {
        // Create the GUI application, set the filter then start up the GUI
        final LiveViewGui frame = new UnfilteredView();
        frame.main();
        // Uncomment if you want to try to connect immediately at start
        // frame.startVideoFeed();
    }

}
