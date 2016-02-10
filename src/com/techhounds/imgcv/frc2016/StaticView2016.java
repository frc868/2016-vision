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
package com.techhounds.imgcv.frc2016;

import com.techhounds.imgcv.FilterToolGuiOpenCv;

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
public final class StaticView2016 extends FilterToolGuiOpenCv {

    private StaticView2016() { //Constructs a new instance of our example filter tool.
        super("Vision Tool 2016");        
    }

    @Override
    protected void addControls() { //Adding controls and filters to the side bar.
        super.addControls(); //Adds parent controls
        //adds the button for our 2016 filter (VisionFilter2016.java)
        addImageProcessingButton("No Filter", new TargetFilter(0)); 
        addImageProcessingButton("Color Filter", new TargetFilter(1));
        addImageProcessingButton("Classic Filter", new TargetFilter(3));
        addImageProcessingButton("Bounding Filter", new TargetFilter(4));
    }

    /**
     * Main entry point which allows you to run the tool as a Java Application.
     * @param args Array of command line arguments.
     */
    public static void main(String[] args) {
        // Create the GUI application and then start it's main routine
    	final FilterToolGuiOpenCv frame = new StaticView2016();
        frame.main();
    }
}
