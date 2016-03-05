package com.techhounds.imgcv.pinksquare;

import java.awt.HeadlessException;

import com.techhounds.imgcv.FilterToolGuiOpenCv;

/**
 * Example of extending the {@link FilterToolGuiOpenCv} class to make your own
 * tool to work with your own custom filters.
 * 
 * @author Paul Blankenbaker
 */
public final class PinkStill extends FilterToolGuiOpenCv {

	private FindPinkRectangleFilter filter;

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
		filter = new FindPinkRectangleFilter();
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
		addImageProcessingButton("Pink Rect", filter, true);
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
				"Filter",
				createImageProcessingMenuItem("Pink Rectangle", filter, true));

		// Adding a sequence filter allows you to view each step of the sequence
		addSequence("Pink Steps", filter.createSequence());
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
