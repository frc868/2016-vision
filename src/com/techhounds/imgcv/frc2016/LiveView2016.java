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
public final class LiveView2016 extends LiveViewGui {
	
    TargetFilter filter = new TargetFilter(4); //default filter to be set
    
    protected JLabel _CameraFps = new JLabel("-");
    protected JLabel _FilterFps = new JLabel("-");
    protected JLabel _Distance = new JLabel("-");
    protected JLabel _Angle = new JLabel("-");
    
    private static NetworkTable netTable;
    
    public LiveView2016(String title) throws HeadlessException {
		super(title);
		setFilter(filter); //set default filter here 
	}
    
    protected void addMenuItems() { //sets menu items
    	super.addMenuItems(); //adds default menu items  	
    	
    	addFilterCategory("2016", "No Filter",       new TargetFilter(0));
    	addFilterCategory("2016", "Color Filter",    new TargetFilter(1));
    	addFilterCategory("2016", "Classic Filter",  new TargetFilter(3));
    	addFilterCategory("2016", "Bounding Filter", new TargetFilter(4));
    	addFilterCategory("2016", "Cube Reticle",    new TargetFilter(5));
    	
    	addMenuItem("File", new JMenuItem(createLoadConfigAction()));
    }
    
    protected void addStatusPanelItems(JPanel statusPanel) { //configs status panel

		statusPanel.add(new JLabel("Camera FPS"));
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(_CameraFps);
		statusPanel.add(Box.createHorizontalStrut(30));
		
		statusPanel.add(new JLabel("Filter FPS"));
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(_FilterFps);
		statusPanel.add(Box.createHorizontalStrut(30));
		
		statusPanel.add(new JLabel("Distance"));
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(_Distance);
		statusPanel.add(Box.createHorizontalStrut(30));
		
		statusPanel.add(new JLabel("Angle"));
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(_Angle);
		statusPanel.add(Box.createHorizontalGlue());
	}

    protected void imageUpdated() {
    	_FilterFps.setText("" + getFilterFps());
    	_CameraFps.setText("" + getFrameGrabberFps());
    	
    	if(netTable != null) {
	    	_Distance.setText(""  + netTable.getNumber("DistanceToBase", 0));
	    	_Angle.setText(""     + netTable.getNumber("OffCenterDegreesX", 0));
	    }
    }
    
    private Action createLoadConfigAction() {
    	final AbstractAction loadAction = new AbstractAction("Load Configs") {
    		
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
    			JFileChooser loadDialog = new JFileChooser();
    			loadDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
    			
    			if (loadDialog.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
    				File selectedFile = loadDialog.getSelectedFile();
    				filter.setColorRangeConfig(selectedFile);
    			}
    		}
    	};
    	
    	return loadAction;
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
    	
        final LiveView2016 frame = new LiveView2016("2016 Vision Viewer");
        
        //NetworkTable setup
               
        NetworkTable.setClientMode();
        NetworkTable.setIPAddress("10.8.68.2");
        NetworkTable.initialize();
        netTable = NetworkTable.getTable("SmartDashboard");
        while(!netTable.getBoolean("cameraEnable", true)) { //hangs when NOT cameraEnable
        	frame.stopVideoFeed();
        	try{
        		Thread.sleep(10);
        	} catch (Exception e) {	
        	}
        	if(netTable.getBoolean("cameraEnable", false)) {
        		frame.startVideoFeed();
        	} //TODO fix disable-disconnect-enable hang
        }
        frame.filter.setNetworkTable(netTable);        
        frame.main();
        
        //frame.startVideoFeed(); //starts from incorrect source
    }

}
