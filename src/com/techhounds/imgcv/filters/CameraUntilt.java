package com.techhounds.imgcv.filters;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

/**
 * This filter is used to "untilt" images when your camera is angled (not
 * perpendicular to the wall).
 * 
 * <p>
 * This filter works with the assumption that rectangles in your image appear as
 * trapezoids (either narrower at the top or at the bottom). This filter
 * "stretches" the image in an attempt to straighten the vertical edges of your
 * target.
 * </p>
 */
public class CameraUntilt implements MatFilter {

	// Used when applying the vertical perspective transform
	private Mat perspectiveTransform;

	// Used when computing transform
	private double width;
	private double height;
	private double ratioOfWidth;

	/**
	 * Default constructor assumes camera titled up and we will come in 10% of
	 * the width at the bottom to try and construct.
	 */
	public CameraUntilt() {
		this(0.1);
	}

	/**
	 * Construct an instance with a specific ratio of the width to come in at
	 * the bottom or top.
	 * 
	 * @param ratioOfWidth
	 *            The a ratio of the width of the image to use when determining
	 *            the "short" edge of the perspective trapezoid. If your camera
	 *            is tilted up, you will want to specify a positive value in the
	 *            range of [0, 0.5]. If your camera is tilted looking down you
	 *            will want to specify a value in the range of [-0.5, 0]. For
	 *            example, specifying .1 for an image that is 800x600 means that
	 *            we will from a perpective trapezoid that squeezes pixels
	 *            together as we near the bottom of the image (the bottom row of
	 *            pixels will come in 80 px on each side).
	 */
	public CameraUntilt(double ratioOfWidth) {
		width = 640;
		height = 640;
		setPerspectiveTransform(ratioOfWidth);
	}

	/**
	 * Set the ratio of the width to use when forming the perspective trapezoid.
	 * 
	 * @param ratioOfWidth
	 *            The a ratio of the width of the image to use when determining
	 *            the "short" edge of the perspective trapezoid. If your camera
	 *            is tilted up, you will want to specify a positive value in the
	 *            range of [0, 0.5]. If your camera is tilted looking down you
	 *            will want to specify a value in the range of [-0.5, 0]. For
	 *            example, specifying .1 for an image that is 800x600 means that
	 *            we will from a perpective trapezoid that squeezes pixels
	 *            together as we near the bottom of the image (the bottom row of
	 *            pixels will come in 80 px on each side).
	 */
	public void setPerspectiveTransform(double ratioOfWidth) {
		this.ratioOfWidth = ratioOfWidth;
		updateTransform();
	}

	/**
	 * Updates the internal perspective transform object whenever the width
	 * ratio or image size changes.
	 */
	private void updateTransform() {
		float ofsTop = 0;
		float ofsBot = 0;
		if (ratioOfWidth < 0) {
			ofsTop = (float) (width * ratioOfWidth);
		} else {
			ofsBot = (float) (width * -ratioOfWidth);
		}

		ArrayList<Point> srcPts = new ArrayList<Point>();
		srcPts.add(new Point(ofsTop, 0));
		srcPts.add(new Point(width - ofsTop, 0));
		srcPts.add(new Point(width - ofsBot, height));
		srcPts.add(new Point(ofsBot, height));
		Mat src = Converters.vector_Point2f_to_Mat(srcPts);

		ArrayList<Point> dstPts = new ArrayList<Point>();
		dstPts.add(new Point(0, 0));
		dstPts.add(new Point(width, 0));
		dstPts.add(new Point(width, height));
		dstPts.add(new Point(0, height));
		Mat dst = Converters.vector_Point2f_to_Mat(dstPts);

		perspectiveTransform = Imgproc.getPerspectiveTransform(src, dst);
	}

	/**
	 * Applies filter to image by stretching pixels (shrinking) to a trapezoid
	 * shape.
	 * 
	 * <p>
	 * NOTE: This is a fairly expensive operation. If you are doing object
	 * detection, you would probably be better off applying a transform to the
	 * polygons of your detected shapes instead of all of the pixels in the
	 * image.
	 * </p>
	 * 
	 * @param srcImage
	 *            The source image to process (must not be null and must have
	 *            non-zero width/height).
	 * @return A new image (srcImage is untouched) containing the perspective
	 *         transformed image.
	 */
	@Override
	public Mat process(Mat srcImage) {
		// Update transform if size of image changes
		Size size = srcImage.size();
		if (size.width != width || size.height != height) {
			width = size.width;
			height = size.height;
			updateTransform();
		}

		// Allocate output image and perform the transform
		Mat dstImage = new Mat(size, srcImage.type());
		Imgproc.warpPerspective(srcImage, dstImage, perspectiveTransform, size);
		return dstImage;
	}

}
