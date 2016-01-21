package com.techhounds.imgcv;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.techhounds.imgcv.filters.MatFilter;

/**
 * Class to facilitate popping a image up in its own window after applying a
 * filter.
 */
public class FilterView {
	/** Used to display the OpenCV image. */
	private JLabel _ImageView;

	/**
	 * Construct an empty instance (no initial image).
	 */
	public FilterView() {
		_ImageView = new JLabel();
	}

	/**
	 * Construct an instance with an initial image.
	 * 
	 * @param img
	 *            The OpenCV image to initialize the object with, must not be
	 *            null.
	 */
	public FilterView(Mat img) {
		this();
		setImage(img);
	}

	/**
	 * Set the image to be displayed.
	 * 
	 * @param img
	 *            The OpenCV image to initialize the object with, must not be
	 *            null.
	 */
	public void setImage(Mat img) {
		int w = img.cols();
		int h = img.rows();

		if (img.type() == CvType.CV_8UC1) {
			BufferedImage iconImg = Conversion.cvGrayToBufferedImage(img);
			_ImageView.setIcon(new ImageIcon(iconImg));
		} else if (img.type() == CvType.CV_8UC3) {
			BufferedImage iconImg = Conversion.cvRgbToBufferedImage(img);
			_ImageView.setIcon(new ImageIcon(iconImg));
		}
		_ImageView.setPreferredSize(new Dimension(w, h));
	}

	/**
	 * Get the swing component that can be added to a GUI application.
	 * 
	 * @return Swing component to throw on your GUI.
	 */
	public JComponent getWidget() {
		return _ImageView;
	}

	/**
	 * Helper method to apply filter to image and display results in new pop-up
	 * frame.
	 * 
	 * @param title
	 *            The title for the frame (should not be null).
	 * @param srcImage
	 *            The source image to display.
	 * @param filter
	 *            The optional filter to apply to the source image prior to
	 *            displaying (or null if you don't need to). NOTE: If we apply
	 *            the filter, we will do so to a clone of the original image
	 *            first so as not to mess up your source.
	 */
	public static void showFilteredImage(String title, Mat srcImage,
			MatFilter filter) {
		JFrame frame = new JFrame(title);
		Mat filteredImage = srcImage;
		if (filter != null) {
			filteredImage = filter.process(srcImage.clone());
		}
		FilterView fv = new FilterView(filteredImage);
		frame.getContentPane().add(fv.getWidget());
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
	}

	/**
	 * Loads an image from a file, applies a filter and displays the results in
	 * a pop-up window.
	 * 
	 * @param file
	 *            The file to load, process and display.
	 * @param filter
	 *            The filter to apply (or null if you just want to display the
	 *            original image in the file).
	 * @return true if we successfully loaded the image and displayed the
	 *         results, false if we encountered a problem.
	 */
	public static boolean showFilteredImage(File file, MatFilter filter) {
		try {
			String path = file.getAbsolutePath();
			Mat newImage = Highgui.imread(path);
			if (newImage == null) {
				return false;
			}
			showFilteredImage(file.getName(), newImage, filter);
			return true;
		} catch (Throwable t) {
			System.err.println("Failed to process/display: " + file);
		}
		return false;
	}

	/**
	 * Display all image (PNG/JPG) files found in a directory.
	 * 
	 * @param dir
	 *            The directory to search for files having .jpg or .png
	 *            extensions.
	 * 
	 * @param filter
	 *            The image filter to apply to each image loaded (or null to
	 *            display original).
	 * 
	 * @return Number of images we successfully loaded and processed (how many
	 *         windows we popped up).
	 */
	public static int showDirectory(File dir, MatFilter filter) {
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".png") || name.endsWith(".jpg");
			}
		});
		int numShown = 0;
		for (String file : files) {
			if (showFilteredImage(new File(dir, file), filter)) {
				numShown++;
			}
		}
		return numShown;
	}
}
