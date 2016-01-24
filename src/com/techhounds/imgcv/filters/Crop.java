package com.techhounds.imgcv.filters;

import org.opencv.core.Mat;

/**
 * A crop filter that allows you to specify the region of the crop in absolute pixel values or as a relative portion of the original image.
 * 
 * @author Paul Blankenbaker
 */
public class Crop implements MatFilter {

	/** Will be true if in absolute pixel mode or false in portion of image mode. */
	private boolean _PixelMode;
	
	/** The constant used for the left edge of the crop region. */
	private double _StartCol;
	
	/** The constant used for the top edge of the crop region. */
	private double _StartRow;
	
	/** The constant used for the right edge of the crop region. */
	private double _EndCol;
	
	/** The constant used for the bottom edge of the crop region. */
	private double _EndRow;

	/**
	 * Creates a "do nothing" crop filter (returns a copy of the entire image).
	 */
	public Crop() {
		setPortion(0.0, 0.0, 100.0, 100.0);
	}

	/**
	 * Creates a crop filter using a absolute pixel values to specify the
	 * ranges.
	 * 
	 * @param left
	 *            Defines the left edge of the region as the number of pixels in
	 *            from the left.
	 * @param top
	 *            Defines the top edge of the region as the number of pixels to
	 *            come down from the top.
	 * @param right
	 *            Defines the right edge of the region as the number of pixels
	 *            in from the left. Must be larger than the "left" value.
	 * @param bottom
	 *            Defines the bottom edge of the region as the number of pixels
	 *            to come down from the top. Must be larger than the "top"
	 *            value.
	 */
	public Crop(int left, int top, int right, int width) {
		setPixels(left, top, right, width);
	}

	/**
	 * Creates a crop filter using a percentage (portion) values to specify the
	 * ranges.
	 * 
	 * @param left
	 *            Defines the left edge of the region as a portion of the width
	 *            in the range of [0, 1.0] to come in from the left.
	 * @param top
	 *            Defines the top edge of the region as a portion of the height
	 *            in the range of [0, 1.0] to come down from the top.
	 * @param right
	 *            Defines the right edge of the region as a portion of the width
	 *            in the range of [0, 1.0] to come in from the left. Must be
	 *            larger than the "left" value.
	 * @param bottom
	 *            Defines the bottom edge of the region as a portion of the
	 *            height in the range of [0, 1.0] to come down from the top.
	 *            Must be larger than the "top" value.
	 */
	public Crop(double left, double top, double right, double bottom) {
		setPortion(left, top, right, bottom);
	}

	/**
	 * Sets the crop filter region using a absolute pixel values to specify the
	 * ranges.
	 * 
	 * @param left
	 *            Defines the left edge of the region as the number of pixels in
	 *            from the left.
	 * @param top
	 *            Defines the top edge of the region as the number of pixels to
	 *            come down from the top.
	 * @param right
	 *            Defines the right edge of the region as the number of pixels
	 *            in from the left. Must be larger than the "left" value.
	 * @param bottom
	 *            Defines the bottom edge of the region as the number of pixels
	 *            to come down from the top. Must be larger than the "top"
	 *            value.
	 */
	public void setPixels(int left, int top, int right, int bottom) {
		setPortion(left, top, right, bottom);
		_PixelMode = true;
	}

	/**
	 * Sets the crop region using a percentage (portion) values to specify the
	 * ranges.
	 * 
	 * @param left
	 *            Defines the left edge of the region as a portion of the width
	 *            in the range of [0, 1.0] to come in from the left.
	 * @param top
	 *            Defines the top edge of the region as a portion of the height
	 *            in the range of [0, 1.0] to come down from the top.
	 * @param right
	 *            Defines the right edge of the region as a portion of the width
	 *            in the range of [0, 1.0] to come in from the left. Must be
	 *            larger than the "left" value.
	 * @param bottom
	 *            Defines the bottom edge of the region as a portion of the
	 *            height in the range of [0, 1.0] to come down from the top.
	 *            Must be larger than the "top" value.
	 */
	private void setPortion(double left, double top, double right, double bottom) {
		if (top >= bottom || left >= right) {
			throw new IllegalArgumentException(
					"Crop region has width or height <= 0");
		}
		_PixelMode = false;
		_StartCol = left;
		_StartRow = top;
		_EndCol = right;
		_EndRow = bottom;
	}

	/**
	 * Applies the crop settings returning an image that is a rectangular subset
	 * of the original source image.
	 * 
	 * @param srcImage
	 *            The source image to process (it will remain untouched).
	 * 
	 * @return The typically smaller portion of the image we cropped out.
	 */
	@Override
	public Mat process(Mat srcImage) {
		int w = srcImage.cols();
		int h = srcImage.rows();

		// In absolute coordinate mode, use region specified as is
		if (_PixelMode) {
			int startRow = Math.max(0, (int) _StartRow);
			int endRow = Math.min(h, (int) _EndRow);
			int startCol = Math.max(0, (int) _StartCol);
			int endCol = Math.min(w, (int) _EndCol);
			return srcImage.submat(startRow, endRow, startCol, endCol);
		}

		// In portion mode, compute pixel offsets based on size of image being
		// processed
		int startRow = Math.max(0, (int) (_StartRow * h));
		int endRow = Math.min(h, (int) (_EndRow * h));
		int startCol = Math.max(0, (int) (_StartCol * w));
		int endCol = Math.min(w, (int) (_EndCol * w));
		return srcImage.submat(startRow, endRow, startCol, endCol);
	}

}
