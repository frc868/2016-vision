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
	private FindPinkRectangleFilter frc2016;

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
		frc2016 = FindPinkRectangleFilter.createFor2016Target();
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
		addImageProcessingButton(findPink.getId(), findPink, true);
		addImageProcessingButton(frc2016.getId(), frc2016, true);
	}

	/**
	 * You can override the {@link FilterToolGuiOpenCv#addMenuItems} method to
	 * add your own items to the menu bar.
	 */
	protected void addMenuItems() {
		super.addMenuItems();

		// You can add to the menu (last parameter indicates if you want to
		// revert or not)
		String pinkId = findPink.getId();
		addMenuItem(
				pinkId,
				createImageProcessingMenuItem("Find Target", findPink, true));
		Action pca = getColorRangeAction("Color Ranges", pinkId, findPink.getColorRangeFilter());
		addMenuItem(pinkId, new JMenuItem(pca));

		String frc2016id = frc2016.getId();
		addMenuItem(
				frc2016id,
				createImageProcessingMenuItem("Find Target", frc2016, true));
		Action t2016ca = getColorRangeAction("Color Ranges", frc2016id, frc2016.getColorRangeFilter());
		addMenuItem(frc2016id, new JMenuItem(t2016ca));
		// Adding a sequence filter allows you to view each step of the sequence
		addSequence(pinkId + " Steps", findPink.createSequence());
		addSequence(frc2016id + " Steps", frc2016.createSequence());
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
