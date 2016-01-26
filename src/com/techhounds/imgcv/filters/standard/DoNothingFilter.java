package com.techhounds.imgcv.filters.standard;

import org.opencv.core.Mat;

import com.techhounds.imgcv.filters.MatFilter;

/**
 * A "do nothing" filter that just returns a copy of the original image passed to it.
 */
public final class DoNothingFilter implements MatFilter {

	/**
	 * A "do nothing" filter that just returns the original image.
	 * 
	 * @param srcImage The image to process.
	 * 
	 * @return The original image unmodified.
	 */
	@Override
	public Mat process(Mat srcImage) {
		return srcImage;
	}

}
