package com.techhounds.imgcv.utils;

import org.opencv.core.Point;
import org.opencv.core.Point3;

/**
 * Class to determine the (x, y, z) real world coordinates for the two end
 * points of a vertical line segment.
 * 
 * <p>
 * In order to make use of this class, you must:
 * </p>
 * 
 * <ul>
 * <li>Have determined the total vertical Field Of View (FOV) for your camera.</li>
 * <li>Know the number of pixels within the FOV (typically height of image in
 * pixels).</li>
 * <li>Know the actual length (in real world units) of the target line segment
 * you want to locate.</li>
 * <li>You can find pixel coordinates (x, y) values compute from (0, 0) at top
 * left corner or image (positive y goes down image).</li>
 * <li>Know the (x, y) coordinates in your pixel image for both end points of
 * the vertical line segment.</li>
 * <li>You are consistent with your real world coordinates (if you give us
 * inches, we compute in inches and return real world values in inches).</li>
 * </ul>
 * 
 * <p>
 * Given the above, this class aids in the computation of:
 * </p>
 * 
 * <ul>
 * <li>The real world (x, y, z) coordinates of each end point of the target line
 * segment (the camera focal point being (0, 0, 0), y axis extends forward out
 * of the camera, x axis extends out the right side of the camera, z axis is
 * vertical).</li>
 * <li>The angle the vertical line segment is rotated off of center (right of
 * center is positive, left of center is negative).</li>
 * </ul>
 */
public class LineSegmentTarget {

	/** Calculator with FOV and total pixel information (given - but we recompute distance to wall). */
	private FovCalculator _Calc;

	/** Given real world size of target (given). */
	private double _TargetSize;

	/** Bottom pixel coordinates of target on screen (given). */
	private Point _Pt0Px;

	/** Top pixel coordinates of target on screen (given). */
	private Point _Pt1Px;

	/** Bottom point of real world target line segment (computed). */
	private Point3 _Pt0;

	/** Top point of real world target line segment (computed). */
	private Point3 _Pt1;

	/** Size of target in pixels (computed). */
	private double _TargetSizePx;

	/** Will be true once we have successfully computed a solution. */
	private boolean _HasSolution;

	/** Width of image in pixels. */
	private double _WidthPx;

	/**
	 * Construct a new instance to compute target locations with (no initial
	 * computation).
	 * 
	 * @param fovDeg
	 *            Total FOV of camera in degrees (in vertical).
	 * @param widthPx
	 *            Width of image in pixels.
	 * @param heightPx
	 *            Height in pixels (total number of pixels FOV covers).
	 * @param targetSize
	 *            Size of vertical edge of target in real world units.
	 */
	public LineSegmentTarget(double fovDeg, double widthPx, double heightPx, double targetSize) {
		this(new FovCalculator(fovDeg, heightPx, 100.0), widthPx, targetSize, 0, 0, 0, 0);
	}

	/**
	 * Construct a new instance with an existing FovCalculator, target size and
	 * target coordinates.
	 * 
	 * @param calc
	 *            The FOV calculator to get FOV and pixel dimensions from.
	 * @param widthPx
	 *            Width of image in pixels.
	 * @param targetSize
	 *            The size of the target in real world units.
	 * @param ax
	 *            The x coordinate of the lower pixel of target line segment
	 *            (from image).
	 * @param ay
	 *            The y coordinate of the lower pixel of target line segment
	 *            (from image).
	 * @param bx
	 *            The x coordinate of the upper pixel of target line segment
	 *            (from image).
	 * @param by
	 *            The y coordinate of the upper pixel of target line segment
	 *            (from image).
	 */
	public LineSegmentTarget(FovCalculator calc, double widthPx, double targetSize, double ax,
			double ay, double bx, double by) {
		_HasSolution = false;
		_Calc = new FovCalculator(calc.getFovDeg(), calc.getPixels(),
				calc.getDistance());
		_TargetSize = targetSize;
		_WidthPx = widthPx;
		_Pt0 = new Point3();
		_Pt1 = new Point3();
		setTargetCoordinates(ax, ay, bx, by);
	}

	/**
	 * Set new pixel coordinates of target determined from image.
	 * 
	 * @param ax
	 *            The x coordinate of the lower pixel of target line segment
	 *            (from image).
	 * @param ay
	 *            The y coordinate of the lower pixel of target line segment
	 *            (from image).
	 * @param bx
	 *            The x coordinate of the upper pixel of target line segment
	 *            (from image).
	 * @param by
	 *            The y coordinate of the upper pixel of target line segment
	 *            (from image).
	 * 
	 * @return true If we were able to find a solution, false if not.
	 */
	public boolean setTargetCoordinates(double ax, double ay, double bx,
			double by) {
		// Y pixels are from top of screen, force _Pt0Px to be below _Pt1Px
		if (ay > by) {
			_Pt0Px = new Point(ax, ay);
			_Pt1Px = new Point(bx, by);
		} else {
			_Pt1Px = new Point(ax, ay);
			_Pt0Px = new Point(bx, by);
		}

		return compute();
	}

	/**
	 * Indicates whether we were able to determine a solution.
	 * 
	 * <p>
	 * NOTE: If this method returns false, then many of the "get" methods will
	 * return bogus information.
	 * </p>
	 * 
	 * @return true If we found a solution, false if not.
	 */
	public boolean hasSolution() {
		return _HasSolution;
	}

	/**
	 * Returns a FOV calculator based on the current solution.
	 * 
	 * <p>
	 * NOTE: This is only valid if {@link #hasSolution()} indicates that a
	 * solution is available.
	 * </p>
	 * 
	 * @return A FOV calculator you can use to compute things like bearing to
	 *         target.
	 */
	public FovCalculator getFovCalculator() {
		return new FovCalculator(_Calc.getFovDeg(), _Calc.getPixels(),
				_Calc.getDistance());
	}

	/**
	 * The vertical height of the target (in real world units).
	 * 
	 * @return Height of actual target we are trying to find.
	 */
	public double getTargetSize() {
		return _TargetSize;
	}

	/**
	 * Size of target in pixels (how many pixels tall is the vertical line
	 * segment).
	 * 
	 * @return Height in pixels.
	 */
	public double getTargetSizePx() {
		return _TargetSizePx;
	}

	/**
	 * Get access to the real world coordinates of the lower point of the target
	 * line segment.
	 * 
	 * <p>
	 * NOTE: This is only valid if {@link #hasSolution()} indicates that a
	 * solution is available.
	 * </p>
	 * 
	 * @return The (x, y, z) coordinates relative to the camera focal point in
	 *         real world units (positive x is out right side of camera,
	 *         positive y is out front of camera, positive z is up out of
	 *         camera).
	 */
	public Point3 getPoint0() {
		return new Point3(_Pt0.x, _Pt0.y, _Pt0.z);
	}

	/**
	 * Get access to the real world coordinates of the upper point of the target
	 * line segment.
	 * 
	 * <p>
	 * NOTE: This is only valid if {@link #hasSolution()} indicates that a
	 * solution is available.
	 * </p>
	 * 
	 * @return The (x, y, z) coordinates relative to the camera focal point in
	 *         real world units (positive x is out right side of camera,
	 *         positive y is out front of camera, positive z is up out of
	 *         camera).
	 */
	public Point3 getPoint1() {
		return new Point3(_Pt1.x, _Pt1.y, _Pt1.z);
	}

	/**
	 * Get access to the real world coordinates of the mid point of the target
	 * line segment.
	 * 
	 * <p>
	 * NOTE: This is only valid if {@link #hasSolution()} indicates that a
	 * solution is available.
	 * </p>
	 * 
	 * @return The (x, y, z) coordinates relative to the camera focal point in
	 *         real world units (positive x is out right side of camera,
	 *         positive y is out front of camera, positive z is up out of
	 *         camera).
	 */
	public Point3 getPointMid() {
		return new Point3((_Pt0.x + _Pt1.x) / 2, (_Pt0.y + _Pt1.y) / 2, (_Pt0.z + _Pt1.z) / 2);
	}

	/**
	 * Get access to the lower pixel coordinates of the target line segment
	 * drawn on image.
	 * 
	 * @return The (x, y) pixel coordinates where (0, 0) is top left of image
	 *         and x is positive to right and y is positive down.
	 */
	public Point getPoint0Px() {
		return new Point(_Pt0Px.x, _Pt0Px.y);
	}

	/**
	 * Get access to the upper pixel coordinates of the target line segment
	 * drawn on image.
	 * 
	 * @return The (x, y) pixel coordinates where (0, 0) is top left of image
	 *         and x is positive to right and y is positive down.
	 */
	public Point getPoint1Px() {
		return new Point(_Pt1Px.x, _Pt1Px.y);
	}

	/**
	 * Convert object to string for more informative debug output.
	 * 
	 * @return A JSON formatted string containing some fields of interest.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("{ \"solution\": ");
		sb.append(hasSolution());
		sb.append(", \"bearing\": ");
		sb.append(String.format("%.1f", _Calc.pixelFromCenterToDeg((_Pt0Px.x + _Pt1Px.x - _WidthPx) / 2)));
		sb.append(", \"fov\": ");
		sb.append(_Calc.toString());
		return sb.toString();
	}

	/**
	 * The meat of the class - computes real world coordinates and information
	 * based on target information and camera FOV.
	 */
	private boolean compute() {
		// Assumes we will fail to find a solution
		_HasSolution = false;

		// Size of target in pixels (assumes 0 is top of image and _Pt0Px is
		// below _Pt1Px on image).
		_TargetSizePx = _Pt0Px.y - _Pt1Px.y;

		// Something bad has happened if target isn't bigger than 0
		if (_TargetSizePx <= 0) {
			return _HasSolution;
		}

		// Compute height of total area
		double heightPx = _Calc.getPixels();
		double heightPx2 = heightPx / 2;
		double widthPx2 = _WidthPx / 2;
		double h = _TargetSize / _TargetSizePx * heightPx;
		double h2 = h / 2;

		// Compute real world distance and reset FOV calculator
		double dist = h2 / _Calc.getTanFov2();
		_Calc = new FovCalculator(_Calc.getFovDeg(), heightPx, dist);
		_Pt0.x = _Calc.pixelFromCenterToLength(_Pt0Px.x - widthPx2);
		_Pt0.y = dist;
		_Pt0.z = _Calc.pixelFromCenterToLength(heightPx2 - _Pt0Px.y);
		_Pt1.x = _Calc.pixelFromCenterToLength(_Pt1Px.x - widthPx2);
		_Pt1.y = dist;
		_Pt1.z = _Calc.pixelFromCenterToLength(heightPx2 - _Pt1Px.y);
		_HasSolution = true;

		return _HasSolution;
	}
	
	/**
	 * A quick and dirty thing to run to test results.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		LineSegmentTarget lst = new LineSegmentTarget(44.1, 640, 480, 20.125);
		lst.setTargetCoordinates(242, 243, 242, 28);
		System.out.println(lst);
		lst.setTargetCoordinates(7, 243, 9, 28);
		System.out.println(lst);
	}
}
