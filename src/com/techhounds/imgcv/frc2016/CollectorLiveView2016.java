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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.techhounds.imgcv.LiveViewGui;
import com.techhounds.imgcv.filters.MatFilter;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * A simple example of leveraging the {@link LiveViewGui} class to quickly test
 * a {@link MatFilter} against a stream of images.
 *
 * @author pkb
 */
public final class CollectorLiveView2016 extends LiveViewGui {
	public CollectorLiveView2016() throws HeadlessException {
		super("Collector Live View 2016");
		
		// TODO Auto-generated constructor stub
	}

	// Network Table key used to signal that the USB camera is active
    static final String COLLECTOR_CAMERA_ENABLED_KEY = "CollectorCameraEnabled";
    

    private static NetworkTable netTable;
	
    public static void main(String[] args) {
    	
        // Create the GUI application, set the filter then start up the GUI
    	
        final CollectorLiveView2016 frame = new CollectorLiveView2016();
        
        //NetworkTable setup
               
        NetworkTable.setClientMode();
        NetworkTable.setIPAddress("10.8.68.2");
        NetworkTable.initialize();
        netTable = NetworkTable.getTable("SmartDashboard");
        frame.main();
        
        boolean lastState = false;
        netTable.putBoolean(COLLECTOR_CAMERA_ENABLED_KEY, true);
        while(true) { //hangs when NOT cameraEnable
        	boolean enable = netTable.getBoolean(COLLECTOR_CAMERA_ENABLED_KEY, true);
        	if (enable != lastState) {
        		lastState = enable;
        		if (enable) {
            		frame.startVideoFeed();
        		} else {
                	frame.stopVideoFeed();       			
        		}
        		System.out.println("Camera feed toggled to: " + enable);
        	}

        	try{
        		Thread.sleep(10);
        	} catch (Exception e) {	
        	}
        }
        
        //frame.startVideoFeed(); //starts from incorrect source
    }

}
