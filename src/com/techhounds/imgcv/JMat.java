package com.techhounds.imgcv;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Simple swing component that draws an OpenCV image matrix into the components
 * area in a stretched (silly putty) mode.
 * 
 * @author Paul Blankenbaker
 */
public final class JMat extends JComponent {
	// Something Swing components expect
	private static final long serialVersionUID = 1L;

	/** Copy of the image we need to draw. */
	private BufferedImage _Image;

	/**
	 * Constructs a new blank instance.
	 */
	public JMat() {
		_Image = null;
	}

	/**
	 * Returns the preferred size for the widget.
	 * 
	 * @return Dimensions of last OpenCV image set, or (320, 240) when no image
	 *         has been set.
	 */
	public Dimension getPreferredSize() {
		BufferedImage img = _Image;
		int w = 320;
		int h = 240;

		if (img != null) {
			w = img.getWidth();
			h = img.getHeight();
		}

		return new Dimension(w, h);
	}

	/**
	 * Set a new OpenCV matrix as the image to display.
	 * 
	 * @param mat
	 *            The OpenCV matrix to display as a Java image. Must be
	 *            CvType.CV_8UC3 for color or CvType.CV_8UC1 for grayscale and
	 *            black and white. You may pass null to clear the last image.
	 * 
	 * @return true If we accepted the image, false if it was not a type we are
	 *         able to handle.
	 */
	public boolean setMat(Mat mat) {
		if (mat != null) {
			if (mat.type() == CvType.CV_8UC1) {
				BufferedImage img = Conversion.cvGrayToBufferedImage(mat);
				_Image = img;
				repaint();
			} else if (mat.type() == CvType.CV_8UC3) {
				BufferedImage img = Conversion.cvRgbToBufferedImage(mat);
				_Image = img;
				repaint();
			} else {
				// Not an image type we accept
				return false;
			}
		} else if (_Image != null) {
			_Image = null;
			repaint();
		}
		return true;
	}

	/**
	 * Method which draws the associated OpenCV image onto the components paint
	 * area.
	 */
	protected void paintComponent(Graphics g) {
		BufferedImage img = _Image;
		if (img == null) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		} else {
			int w = img.getWidth();
			int h = img.getHeight();
			g.drawImage(img, 0, 0, getWidth(), getHeight(), 0, 0, w, h, this);
		}
	}

}
