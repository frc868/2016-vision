/*
 * Copyright (c) 2015, Paul Blankenbaker
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
package com.techhounds.imgcv.avc;

import com.techhounds.imgcv.FilterToolGuiOpenCv;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.standard.ColorSpace;
import com.techhounds.imgcv.filters.standard.GrayScale;

/**
 * A more involved example of extending the filter tool for testing filters.
 *
 * <p>
 * In this second example, we exercise some more advanced features
 * including:</p>
 *
 * <ul>
 * <li>Adding preferences for some of our filters that are saved/loaded between
 * sessions.</li>
 * <li>Example of adding a filter implemented in-line.</li>
 * <li>Example of adding a composite filter (multiple filters chained
 * together).</li>
 * </ul>
 *
 * @author pkb
 */
public final class StanchionTool2015 extends FilterToolGuiOpenCv {

    /** Filter to look for 2013 "blue" color in image. */
    private final MatFilter _RedRange;
    
    /** Alternative filter which looks for 2013 "blue" in HSV color space. */
    private final MatFilter _YelRange;

    /**
     * Constructs a new instance of our example filter tool.
     */
    private StanchionTool2015() {
        super("AVC Stanchion Tool 2015");
        _RedRange = FindStanchion2015.createRedColorRange();
        _YelRange = FindStanchion2015.createYellowColorRange();
    }

    /**
     * Adding controls and filters to the side bar.
     */
    @Override
    protected void addControls() {
        // Add parent base side panel controls
        super.addControls();
        addSeparator();

        // Let's add some of our quick access tools
        addImageProcessingButton("BGR->HSV", ColorSpace.createBGRtoHSV());
        addImageProcessingButton("2015 Yel Filt", _YelRange);
        addImageProcessingButton("2015 Red Filt", _RedRange);
        addImageProcessingButton("Gray Scale", new GrayScale());
        addImageProcessingButton("B&W Tweaked", FindStanchion2015.createBlackWhite());
        addImageProcessingButton("2015 Yel", new FindStanchion2015(false), true);
        addImageProcessingButton("2015 Red", new FindStanchion2015(true), true);
    }

    /**
     * Adds application specific menu items.
     */
    @Override
    protected void addMenuItems() {
        // Add parent base menu items
        super.addMenuItems();
        
		addSequence("Yellow", FindStanchion2015.createSteps(false, true));
		addSequence("Red", FindStanchion2015.createSteps(true, true));
    }

    /**
     * Main entry point which allows you to run the tool as a Java Application.
     *
     * @param args Array of command line arguments.
     */
    public static void main(String[] args) {
        // Create the GUI application and then start it's main routine
        final FilterToolGuiOpenCv frame = new StanchionTool2015();
        frame.main();
    }
}
