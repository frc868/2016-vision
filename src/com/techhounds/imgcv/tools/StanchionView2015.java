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

import org.opencv.core.Mat;

import com.techhounds.imgcv.LiveViewGui;
import com.techhounds.imgcv.filters.FindStanchion2015;
import com.techhounds.imgcv.filters.MatFilter;

/**
 * A simple example of leveraging the {@link LiveViewGui} class to quickly test
 * a {@link MatFilter} against a stream of images.
 *
 * @author pkb
 */
public final class StanchionView2015 {
	class FindYellowOrRed implements MatFilter {
		private FindStanchion2015 yelFilter = new FindStanchion2015(false);
		private FindStanchion2015 redFilter = new FindStanchion2015(true);

		@Override
		public Mat process(Mat srcImage) {
			Mat results = yelFilter.process(srcImage);
			if (!yelFilter.foundStanchion()) {
				results = redFilter.process(srcImage);
			}
			return results;
		}
		
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
        final LiveViewGui frame = new LiveViewGui("2015 AVC Stanchion Viewer");
        frame.setFilter(new FindStanchion2015(false));
        //frame.setFilter(FindTarget2013.createHsvColorRange());
        frame.main();
    }

}
