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
package com.techhounds.imgcv.frc2013;

import com.techhounds.imgcv.FilterToolGuiOpenCv;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.MatFilter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

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
public final class FilterTool2013 extends FilterToolGuiOpenCv {

    /** Filter to look for 2013 "blue" color in image. */
    private final ColorRange _ColorRange;
    
    /** Alternative filter which looks for 2013 "blue" in HSV color space. */
    private final MatFilter _HsvRange;

    /**
     * Constructs a new instance of our example filter tool.
     */
    private FilterTool2013() {
        super("Filter Tool 2013");
        _ColorRange = FindTarget2013.createColorRange();        
        _HsvRange = FindTarget2013.createHsvColorRange();
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
        addImageProcessingButton("B&W Tweaked", FindTarget2013.createBlackWhite());
        addImageProcessingButton("2013 RGB Find", _ColorRange);
        addImageProcessingButton("2013 HSV Find", _HsvRange);
        addImageProcessingButton("2013 Target", new FindTarget2013());
    }

    /**
     * Adds application specific menu items.
     */
    @Override
    protected void addMenuItems() {
        // Add parent base menu items
        super.addMenuItems();

        // Add some of our custom 2013 filters to a pull down menu as well
        String label = "2013";
        addMenuItem(label, createImageProcessingMenuItem("B&W Tweaked", FindTarget2013.createBlackWhite()));
        addMenuItem(label, createImageProcessingMenuItem("2013 RGB Find", _ColorRange));
        addMenuItem(label, createImageProcessingMenuItem("2013 HSV Find", _HsvRange));
        addMenuItem(label, createImageProcessingMenuItem("Find Target", new FindTarget2013()));
        
        addMenuItem("Grab 10", new JMenuItem(grab10()));
    }

    /**
     * Main entry point which allows you to run the tool as a Java Application.
     *
     * @param args Array of command line arguments.
     */
    public static void main(String[] args) {
        // Create the GUI application and then start it's main routine
        final FilterToolGuiOpenCv frame = new FilterTool2013();
        frame.main();
    }

    private Action grab10() {
        
        return new AbstractAction("Grab 10") {
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                VideoCapture vc = new VideoCapture("http://admin:1234@192.168.1.25/mjpg/video.mjpg");
                
                Mat mat = new Mat();
                
                for (int i = 0; i < 10; i++) {
                    if (vc.read(mat)) {
                        setImage(mat);
                        System.out.println("Got image: " + i);
                    }
                }
            }
        };
    }

}
