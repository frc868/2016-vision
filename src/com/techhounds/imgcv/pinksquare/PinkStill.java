package com.techhounds.imgcv.pinksquare;

import java.awt.HeadlessException;

import javax.swing.Action;
import javax.swing.JMenuItem;

import com.techhounds.imgcv.FilterToolGuiOpenCv;

/**
 * Example of extending the {@link FilterToolGuiOpenCv} class to make your own
 * tool to work with your own custom filters.
 * 
 * @author Paul Blankenbaker
 */
public final class PinkStill extends FilterToolGuiOpenCv {

	// Filter to locate big pink rectangle
	private FindPinkRectangleFilter findPink;
	
	// Filter to locate 2016 FRC target
	private FindPinkRectangleFilter target2016;

	/**
	 * The constructor pretty much just needs to set the title line for your
	 * tool.
	 * 
	 * @throws HeadlessException
	 *             If user tries to run from a command line that doesn't support
	 *             GUIs.
	 */
	protected PinkStill() throws HeadlessException {
		super("Image Filter Tool");
		findPink = new FindPinkRectangleFilter();
		target2016 = FindPinkRectangleFilter.createFor2016Target();
	}

	/**
	 * You can override the {@link FilterToolGuiOpenCv#addControls} method to
	 * add your own "quick" buttons to the side control panel.
	 */
	protected void addControls() {
		// Go ahead and add the standard controls
		super.addControls();

		addSeparator();
		// Apply pink rectangle filter to original
		addImageProcessingButton("Pink Rect", findPink, true);
		addImageProcessingButton("2016 Target", target2016, true);
	}

	/**
	 * You can override the {@link FilterToolGuiOpenCv#addMenuItems} method to
	 * add your own items to the menu bar.
	 */
	protected void addMenuItems() {
		super.addMenuItems();

		// You can add to the menu (last parameter indicates if you want to
		// revert or not)
		addMenuItem(
				"Pink Filter",
				createImageProcessingMenuItem("Pink Rectangle", findPink, true));
		Action pca = getColorRangeAction("Color Ranges", "pink", findPink.getColorRangeFilter());
		addMenuItem("Pink Filter", new JMenuItem(pca));

		addMenuItem(
				"2016 Filter",
				createImageProcessingMenuItem("2016 Target", target2016, true));
		Action t2016ca = getColorRangeAction("Color Ranges", "2016-target", target2016.getColorRangeFilter());
		addMenuItem("2016 Filter", new JMenuItem(t2016ca));
		// Adding a sequence filter allows you to view each step of the sequence
		addSequence("Pink Steps", findPink.createSequence());
		addSequence("2016 Steps", target2016.createSequence());
	}

	/**
	 * Create and show the tool.
	 * 
	 * @param args
	 *            Command line arguments (ignored).
	 */
	public static void main(String[] args) {
		// Create and start tool
		PinkStill tool = new PinkStill();
		tool.main();
	}

}
