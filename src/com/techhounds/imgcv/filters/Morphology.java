package com.techhounds.imgcv.filters;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * A {@link MatFilter} wrapper around the Imgproc.morphologyEx() filter (good
 * for removing noise).
 */
public class Morphology implements MatFilter {

	// The options to apply to the Imgproc.morpholoyEx() invocation
	private int morphOp;
	private Mat kernel;
	private Point anchor;
	private int iterations;

	/**
	 * Construct a new instance in ImgProc.MORPH_OPEN mode with the default
	 * kernel size (using the Imgproc.MORPH_RECT shape).
	 */
	public Morphology() {
		this(3);
	}

	/**
	 * Construct a new instance in ImgProc.MORPH_OPEN mode for a specific kernel
	 * size (using the Imgproc.MORPH_RECT shape).
	 * 
	 * @param ksize
	 *            How big the kernel should be (radius will be 1+ this size,
	 *            resulting square of 2*ksize + 1 pixels in each dimension).
	 */
	public Morphology(int ksize) {
		this(Imgproc.MORPH_OPEN, Imgproc.MORPH_RECT, ksize);
	}

	/**
	 * Construct a new instance with a specific morphology option and standard
	 * kernel initialization.
	 * 
	 * @param morphOp
	 *            The morphology option to use (ImgProc.MORPH_OPEN,
	 *            ImgProc.MORPH_CLOSE, Imgproc.MORPH_TOPHAT,
	 *            Imgproc.MORPH_BLACKHAT, Imgproc.GRADIENT). *
	 * @param shape
	 *            The kernel shape (ImgProc.MORPH_RECT, ImgProc.MORPH_ELLIPSE,
	 *            ImageProc.MORPH_CROSS).
	 * @param ksize
	 *            How big the kernel should be (radius will be 1+ this size,
	 *            resulting square of 2*ksize + 1 pixels in each dimension).
	 */
	public Morphology(int morphOp, int shape, int ksize) {
		this(morphOp, createKernel(shape, ksize));
	}

	/**
	 * Construct a new instance with a specific morphology shape and kernel.
	 * 
	 * @param morphOp
	 *            The morphology option to use (ImgProc.MORPH_OPEN,
	 *            ImgProc.MORPH_CLOSE, Imgproc.MORPH_TOPHAT,
	 *            Imgproc.MORPH_BLACKHAT, Imgproc.GRADIENT).
	 * @param kernel
	 *            The kernel parameter to pass into the ImgProc.morphologyEx()
	 *            invocation.
	 */
	public Morphology(int morphOp, Mat kernel) {
		this.morphOp = morphOp;
		this.kernel = kernel.clone();
		this.anchor = new Point(-1, -1);
		this.iterations = 1;
	}

	/**
	 * Number of iterations to apply the morphology filter.
	 * 
	 * @param iterCnt
	 *            A value of 1 or larger (default constructor sets it to 1).
	 */
	public void setIterations(int iterCnt) {
		iterations = (iterCnt > 0 ? iterCnt : 1);
	}

	/**
	 * Helper method to create the kernel parameter required for the OpenCV
	 * morphologyEx() method.
	 * 
	 * @param shape
	 *            The kernel shape (ImgProc.MORPH_RECT, ImgProc.MORPH_ELLIPSE,
	 *            ImageProc.MORPH_CROSS).
	 * @param ksize
	 *            How big the kernel should be (radius will be 1+ this size,
	 *            resulting square of 2*ksize + 1 pixels in each dimension).
	 * @return A Mat object containing a kernel for the ImgProc.morphologyEx()
	 *         invocation.
	 */
	public static Mat createKernel(int shape, int ksize) {
		int dim = 2 * ksize + 1;
		Mat kern = Imgproc.getStructuringElement(shape, new Size(dim, dim));
		return kern;
	}

	@Override
	public Mat process(Mat srcImage) {
		int rows = srcImage.rows();
		int cols = srcImage.cols();
		int type = srcImage.type();
		Mat dstImage = new Mat(rows, cols, type);

		Imgproc.morphologyEx(srcImage, dstImage, morphOp, kernel, anchor, iterations);
		return dstImage;
	}

}
