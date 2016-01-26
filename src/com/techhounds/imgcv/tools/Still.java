package com.techhounds.imgcv.tools;

import java.awt.HeadlessException;

import com.techhounds.imgcv.FilterToolGuiOpenCv;
import com.techhounds.imgcv.filters.FillChannel;
import com.techhounds.imgcv.filters.standard.GrayScale;
import com.techhounds.imgcv.filters.standard.Negative;
import com.techhounds.imgcv.filters.standard.Sequence;

/**
 * Example of extending the {@link FilterToolGuiOpenCv} class to make your own
 * tool to work with your own custom filters.
 * 
 * @author Paul Blankenbaker
 */
public final class Still extends FilterToolGuiOpenCv {

	/**
	 * The constructor pretty much just needs to set the title line for your
	 * tool.
	 * 
	 * @throws HeadlessException
	 *             If user tries to run from a command line that doesn't support
	 *             GUIs.
	 */
	protected Still() throws HeadlessException {
		super("Image Filter Tool");
	}

	/**
	 * You can override the {@link FilterToolGuiOpenCv#addControls} method to
	 * add your own "quick" buttons to the side control panel.
	 */
	protected void addControls() {
		// Go ahead and add the standard controls
		super.addControls();

		addSeparator();
		addImageProcessingButton("Gray", new GrayScale());
		// Passing true causes image to revert to original prior to applying
		// filter
		addImageProcessingButton("Green", createGreenChannel(), true);
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
				createImageProcessingMenuItem("Negative", new Negative(), false));
		addMenuItem(
				"Filter",
				createImageProcessingMenuItem("Green Only",
						createGreenChannel(), true));

		// Adding a sequence filter allows you to view each step of the sequence
		addSequence("Green Only", createGreenChannel());
	}

	/**
	 * Example of building a image filter by combining a sequence of existing
	 * filters.
	 * 
	 * @return A filter that zeros out the blue channel and then zeros out the
	 *         red channel.
	 */
	private Sequence createGreenChannel() {
		Sequence seq = new Sequence();
		seq.addFilter(new FillChannel(0, 0));
		seq.addFilter(new FillChannel(2, 0));
		return seq;
	}

	/**
	 * Create and show the tool.
	 * 
	 * @param args
	 *            Command line arguments (ignored).
	 */
	public static void main(String[] args) {
		// Create and start tool
		Still tool = new Still();
		tool.main();
	}

}
