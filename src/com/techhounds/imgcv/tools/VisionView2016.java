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

import java.awt.HeadlessException;

import org.opencv.core.Mat;
import com.techhounds.imgcv.LiveViewGui;
import com.techhounds.imgcv.filters.DoNothingFilter;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.Sequence;
import com.techhounds.imgcv.filters.VisionFilter2016;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * A simple example of leveraging the {@link LiveViewGui} class to quickly test
 * a {@link MatFilter} against a stream of images.
 *
 * @author pkb
 */
public final class VisionView2016 extends LiveViewGui {
    VisionFilter2016 filter = new VisionFilter2016();
    
    public VisionView2016(String title) throws HeadlessException {
		super(title);
		setFilter(filter);
	}
    
    protected void addMenuItems() {
    	super.addMenuItems();
    	
    	addFilter("Main 2016 Step 1", VisionFilter2016.createDilate());
    	Sequence step2 = new Sequence();
    	step2.addFilter(VisionFilter2016.createDilate());
    	step2.addFilter(VisionFilter2016.createErode());
    	addFilter("Main 2016 Step 2", step2);
        addFilter("Main 2016 Filter", filter);    	
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
        final VisionView2016 frame = new VisionView2016("2016 Vision Viewer");
        NetworkTable.setClientMode();
        NetworkTable.setIPAddress("10.8.68.2");
        NetworkTable.initialize();
        NetworkTable sd = NetworkTable.getTable("SmartDashboard");
        frame.filter.setNetworkTable(sd);
        frame.main();
        //frame.startVideoFeed(); //starts from incorrect source
    }

}
