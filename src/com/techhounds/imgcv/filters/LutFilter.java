package com.techhounds.imgcv.filters;

import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * Wrapper around the OpenCV LUT (Lookup Up Table?) filter which allows you to
 * quickly (native code) transform values in your matrix (uses quick look up
 * tables).
 * 
 * @author Paul Blankenbaker
 */
public class LutFilter implements MatFilter {

	/** Matrix containing LUT lookup values. */
	private Mat _Lut;

	/**
	 * Create a new instance of a LUT transform matrix.
	 * 
	 * @param lutTransform
	 *            A Matrix having 1 row, 256 columns. Each entry in the matrix
	 *            should have the value you want to replace the value in the channel with.
	 *            For example, if column 127 in your matrix contains [0, 17, 232]
	 *            and column 12 contains [100, 50, 200]. A pixel having the 
	 *            value of [12, 127, 12] would be transformed to [100, 17, 200].
	 */
	public LutFilter(Mat lutTransform) {
		_Lut = lutTransform.clone();
	}

	/**
	 * Apply the filter to an image.
	 * 
	 * @param srcImage
	 *            The source image to filter.
	 * 
	 * @return A reference to the original srcImage after its contents have been
	 *         modified.
	 */
	@Override
	public Mat process(Mat srcImage) {
		int lutChannels = _Lut.channels();
		// Can only apply the filter if it is a single channel or matches
		// the number of channels in the source
		if ((lutChannels == 1) || (lutChannels == srcImage.channels())) {
			Core.LUT(srcImage, _Lut, srcImage);
		}
		return srcImage;
	}

}
