package com.techhounds.imgcv.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

/**
 * A Field Of View (FOV) calculator.
 * 
 * <p>
 * A simple tool used in building the {@link FovFilter}, but has uses outside of
 * that filter as well.
 * </p>
 */
public class FovCalculator {

	/** Full field of view of camera that spans pixels in degrees. */
	private double _FovDeg;
	/** Cached FOV in radians. */
	private double _FovRad;
	/** The real world distance from the camera to the wall. */
	private double _Distance;
	/** The number of pixels the FOV covers. */
	private double _Pixels;
	/** Cached half of FOV in radians. */
	private double _FovRad2;
	/** Cached value of tan(FOV/2). */
	private double _TanFov2;
	/** The length of the target in real world units that is visible in image. */
	private double _Length;
	/** Distance of camera to wall in pixels. */
	private double _DistPx;

	/**
	 * Constructs a new FOV calculator for 3 specific terms related to the
	 * camera.
	 * 
	 * @param fovDeg
	 *            FOV the camera can see in the dimension you care about in
	 *            degrees.
	 * @param pixels
	 *            Number of pixels the FOV is mapped to in this dimension.
	 * @param distance
	 *            The distance in real world units from the camera to the "wall"
	 *            that is captured.
	 */
	public FovCalculator(double fovDeg, double pixels, double distance) {
		_FovDeg = fovDeg;
		_Pixels = pixels;
		_Distance = distance;
		precompute();
	}

	/**
	 * Constructs a new instance using another FOV calculator as a reference for
	 * a specific number of pixels (often used to compute the vertical FOV if
	 * you have the horizontal FOV and a different number of pixels).
	 * 
	 * @param ref
	 *            FOV to use as a reference for the camera.
	 * @param pixels
	 *            Number of pixels this dimension is mapped to.
	 */
	public FovCalculator(FovCalculator ref, double pixels) {
		this(ref.pixelFromCenterToDeg(pixels / 2) * 2, pixels, ref._Distance);
	}

	/**
	 * Default constructor for a FOV calculator for a camera with a 60 deg FOV
	 * spread across 640 pixels 100.0 units (in, ft, ...) away from the target
	 * wall.
	 */
	public FovCalculator() {
		this(60.0, 640, 100.0);
	}

	/**
	 * Sets the FOV of the camera in degrees.
	 * 
	 * @param fovDeg
	 *            The total FOV that the camera can see.
	 */
	public void setFovDeg(double fovDeg) {
		_FovDeg = fovDeg;
		precompute();
	}

	/**
	 * Returns the total FOV of the camera in degrees.
	 * 
	 * @return The total FOV that the camera can see.
	 */
	public double getFovDeg() {
		return _FovDeg;
	}
	
	/**
	 * Returns the tan(FOV/2) value that has been precomputed.
	 * 
	 * @return Pre-computed tangent of half the FOV.
	 */
	public double getTanFov2() {
		return _TanFov2;
	}

	/**
	 * Returns the total FOV of the camera in radians.
	 * 
	 * @return FOV in radians.
	 */
	public double getFovRad() {
		return _FovRad;
	}

	/**
	 * Returns the length of the image (width or height) in pixels.
	 * 
	 * @return Total number of pixels camera FOV is mapped to.
	 */
	public double getPixels() {
		return _Pixels;
	}

	/**
	 * Set the length fo the image (width or height we don't care) in pixels.
	 * 
	 * @param pixels
	 *            Total number of pixels camera FOV is mapped to.
	 */
	public void setPixels(int pixels) {
		_Pixels = pixels;
		precompute();
	}

	/**
	 * Returns the distance of the camera to the wall in world units (center of
	 * image).
	 * 
	 * @return The distance (in same units as originally set).
	 */
	public double getDistance() {
		return _Distance;
	}

	/**
	 * Returns the distance of the camera to the wall in pixels (center of
	 * image).
	 * 
	 * @return The distance (in pixels).
	 */
	public double getDistancePx() {
		return _DistPx;
	}

	/**
	 * Gets the physical length of the image (width or height depending on how
	 * you are using the calculator) that is visible in the image.
	 * 
	 * @return The physical length the image covers at the given distance.
	 */
	public double getLength() {
		return _Length;
	}

	/**
	 * Computes the number of pixels from the center of the image for the given
	 * number of degrees.
	 * 
	 * @param degFromCenter
	 *            Degrees from center (positive or negative values are
	 *            permitted).
	 * 
	 * @return How far (in pixels) from center this location maps to. NOTE: This
	 *         may fall outside the image if angle exceeds what the FOV is
	 *         capable of.
	 */
	public double degFromCenterToPixel(double degFromCenter) {
		return Math.tan(Math.toRadians(degFromCenter)) * _DistPx;
	}

	/**
	 * Computes the length (in real world units) from the center of the image
	 * for the given number of degrees.
	 * 
	 * @param degFromCenter
	 *            Degrees from center (positive or negative values are
	 *            permitted).
	 * 
	 * @return How far (in real world units) from center this location maps to.
	 *         NOTE: This may fall outside the image if angle exceeds what the
	 *         FOV is capable of.
	 */
	public double degFromCenterToLength(double degFromCenter) {
		return Math.tan(Math.toRadians(degFromCenter)) * _Distance;
	}

	/**
	 * Compute degrees from center for a specific pixel location.
	 * 
	 * @param pixelFromCenter
	 *            The number of pixels from center.
	 * 
	 * @return Number of degrees off center.
	 */
	public double pixelFromCenterToDeg(double pixelFromCenter) {
		return Math.toDegrees(Math.atan(pixelFromCenter / _DistPx));
	}

	/**
	 * Computes the real world distance from the center of the image of a
	 * specific pixel.
	 * 
	 * @param pixelFromCenter
	 *            Number of pixels from center of image.
	 * @return The length (distance) in corresponding real world units (same
	 *         units as {@link #getDistance()}).
	 */
	public double pixelFromCenterToLength(double pixelFromCenter) {
		return _Distance / _DistPx * pixelFromCenter;
	}

	/**
	 * Returns a string representation of the FOV having a JSON look to it.
	 * 
	 * @return String representation.
	 */
	public String toString() {
		return String.format("{ \"fov\":%.1f, \"px\":%d, \"dist\":%.1f, \"length\":%.1f }", _FovDeg,
				(int) Math.round(_Pixels), _Distance, _Length);
	}

	/**
	 * Draws FOV lines and text information on image.
	 * 
	 * @param img
	 *            Image to draw on.
	 * @param lineColor
	 *            Color to use for lines (null if you don't want lines).
	 * @param textColor
	 *            Color to use for text (null if you don't want text).
	 * 
	 * @return true If able to draw either horizontal or vertical FOV lines.
	 */
	public boolean draw(Mat img, double degSpacing, Scalar lineColor, Scalar textColor) {
		return drawHorizontal(img, degSpacing, lineColor, textColor)
				&& drawVertical(img, degSpacing, lineColor, textColor);
	}

	/**
	 * Draws horizontal FOV (vertical lines spread across width of image).
	 * 
	 * @param img
	 *            Image to draw on.
	 * @param lineColor
	 *            Color to use for lines (null if you don't want lines).
	 * @param textColor
	 *            Color to use for text (null if you don't want text).
	 * 
	 * @return true If img pixels matched FOV pixels (we ignore request and
	 *         return false if the pixel dimensions don't match).
	 */
	public boolean drawHorizontal(Mat img, double degSpacing, Scalar lineColor, Scalar textColor) {
		int w = img.cols();
		int h = img.rows();
		if (w != _Pixels) {
			return false;
		}

		int[] baseline = { 0 };
		int fontFace = Core.FONT_HERSHEY_PLAIN;
		double fontScale = 0.75;
		int thickness = 1;
		int textGap = 2;
		int n = (int) Math.floor(_FovDeg / 2 / degSpacing);
		int cx = w / 2;
		
		// OpenCV requires us to rotate image to draw text on 90 degree angle
		Core.transpose(img, img);
		Core.flip(img, img, 1);

		for (int i = -n; i <= n; i++) {
			double deg = degSpacing * i;
			int px = cx + (int) Math.round(degFromCenterToPixel(deg));

			if (lineColor != null) {
				Point pt1 = new Point(0, px);
				Point pt2 = new Point(h - 1, px);
				Core.line(img, pt1, pt2, lineColor);
			}

			if (textColor != null) {
				double dist = degFromCenterToLength(deg);
				String text = String.format("Deg: %.1f  Px: %d (%d)  Dist: %.1f", deg, px, px - cx, dist);

				Size size = Core.getTextSize(text, fontFace, fontScale, thickness, baseline);

				int textOfs = (int) (textGap + size.height);
				Point p = new Point(textGap + (size.height * 2), px + (i < 0 ? textOfs : -textGap));
				Core.putText(img, text, p, fontFace, 0.75, textColor, 1);
			}
		}
		
		// Rotate image back
		Core.transpose(img, img);
		Core.flip(img, img, 0);
		return true;
	}

	/**
	 * Draws vertical FOV (horizontal lines spread across height of image).
	 * 
	 * @param img
	 *            Image to draw on.
	 * @param lineColor
	 *            Color to use for lines (null if you don't want lines).
	 * @param textColor
	 *            Color to use for text (null if you don't want text).
	 * 
	 * @return true If img pixels matched FOV pixels (we ignore request and
	 *         return false if the pixel dimensions don't match).
	 */
	public boolean drawVertical(Mat img, double degSpacing, Scalar lineColor, Scalar textColor) {
		int w = img.cols();
		int h = img.rows();
		if (h != _Pixels) {
			return false;
		}

		int[] baseline = { 0 };
		int fontFace = Core.FONT_HERSHEY_PLAIN;
		double fontScale = 0.75;
		int thickness = 1;
		int textGap = 2;

		int n = (int) Math.floor(_FovDeg / 2 / degSpacing);
		int cx = h / 2;
		for (int i = -n; i <= n; i++) {
			double deg = degSpacing * i;
			int px = cx + (int) Math.round(degFromCenterToPixel(deg));

			if (lineColor != null) {
				Point pt1 = new Point(0, px);
				Point pt2 = new Point(w - 1, px);
				Core.line(img, pt1, pt2, lineColor);
			}

			if (textColor != null) {
				double dist = degFromCenterToLength(deg);
				String text = String.format("Deg: %.1f  Px: %d (%d)  Dist: %.1f", deg, px, px - cx, dist);

				Size size = Core.getTextSize(text, fontFace, fontScale, thickness, baseline);

				int textOfs = (int) (textGap + size.height);
				Point p = new Point(textGap, px + (i < 0 ? textOfs : -textGap));
				Core.putText(img, text, p, fontFace, 0.75, textColor, 1);
			}
		}

		return true;
	}

	/**
	 * Compute internal constants used by the calculator.
	 */
	private void precompute() {
		_FovRad = Math.toRadians(_FovDeg);
		_FovRad2 = _FovRad / 2;
		_TanFov2 = Math.tan(_FovRad2);
		_Length = 2 * _Distance * _TanFov2;
		_DistPx = _Distance * _Pixels / _Length;
	}

}
