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
package com.techhounds.imgcv.pinksquare;

import javax.swing.Action;
import javax.swing.JMenuItem;

import com.techhounds.imgcv.LiveViewGui;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.Sequence;

/**
 * A live view of the {@link FindPinkRectangleFilter} filter.
 *
 * @author pkb
 */
public final class PinkLive extends LiveViewGui {

	// Filter to locate big pink rectangle
	private FindPinkRectangleFilter findPink;
	
	// Filter to locate 2016 FRC target
	private FindPinkRectangleFilter frc2016;
	
	/**
	 * Constructor needs to set the title (we'll set the default filter too).
	 */
	private PinkLive() {
		super("Pink Rectangle Finder");
		findPink = new FindPinkRectangleFilter();
		frc2016 = FindPinkRectangleFilter.createFor2016Target();
		// Set the default filter
		setFilter(findPink);
	}
	
	/**
	 * Override the add menu items if you want to include additional filters.
	 */
	protected void addMenuItems() {
		super.addMenuItems();
		
		// Add filters and color range tweak tools

		ColorRange crf = findPink.getColorRangeFilter();
		String id = findPink.getId();
		addFilter(id, findPink);

		Action pca = crf.getColorRangeAction(id + " Ranges", id);
		addMenuItem("Colors", new JMenuItem(pca));
		
		crf = frc2016.getColorRangeFilter();
		id = frc2016.getId();
		addFilter(id, frc2016);
		pca = crf.getColorRangeAction(id + " Ranges", id);
		addMenuItem("Colors", new JMenuItem(pca));
		
		// Add sequence filters
		Sequence pinkSeq = findPink.createSequence();
		addSequence("Pink Steps", pinkSeq);
		
		Sequence t2016Seq = frc2016.createSequence();
		addSequence("2016 Steps", t2016Seq);
	}

    /**
     * Main entry point to this Java Application.
     *
     * @param args Array of command line arguments (ignored).
     */
    public static void main(String[] args) {
        // Create the GUI application, set the filter then start up the GUI
        final LiveViewGui frame = new PinkLive();
        frame.main();
        // Uncomment if you want to try to connect immediately at start
        // frame.startVideoFeed();
    }

}
