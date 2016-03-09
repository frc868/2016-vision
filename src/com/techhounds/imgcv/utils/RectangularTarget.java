package com.techhounds.imgcv.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import com.techhounds.imgcv.PolygonCv;

/**
 * Class designed to help find real world coordinates of a "rectangular" target.
 * 
 * <p>
 * To make use of this class, you must provide:
 * </p>
 * 
 * <ul>
 * <li>The total image size (dimensions in pixels).</li>
 * <li>The physical target size in real world units (like inches or cm).</li>
 * <li>The physical coordinates of the camera relative to the center or rotation
 * of your robot.</li>
 * <li>Your target must have fairly vertical edges on the left and right side.</li>
 * <li>Your camera must be mounted vertically.</li>
 * <li>The total field of view (FOV) of the camera that spans the pixels in the
 * VERTICAL dimension.</li>
 * <li>You must also find the polygon in the image and provide us with the pixel
 * coordinates.</li>
 * </ul>
 * 
 * @author pkb
 *
 */
public class RectangularTarget {

	/** Full field of view of camera in vertical (degrees). */
	double fov;

	/** Width of the image in pixels. */
	double imageWidthPx;

	/** Height of image in pixels (set of pixels covered by FOV). */
	double imageHeightPx;

	/** The width of the target in real world units. */
	double targetWidth;

	/** The height of the target in real world units. */
	double targetHeight;

	/** Camera location relative to center point or robot rotation. */
	private Point3 cameraLoc;

	/**
	 * Allowed tolerance as ratio of width when looking for left and right edges
	 * (0.05 = 5%).
	 */
	private double vertLineTolerance;

	/** Settings for drawing vertical lines. */
	private Scalar vertColors;
	private int vertThickness;

	/** Settings for drawing cross hair at middle point of target. */
	private Scalar crossHairColor;
	private int crossHairSize;
	private int crossHairThickness;

	/** Settings for text labels drawn on results. */
	private Scalar textColor;
	private Scalar textBackground;

	//
	// Following fields are all computed
	//

	/** Will be true if we have a solution, false if not. */
	boolean hasSolution;

	/** Pixel coordinates of bottom point of left vertical edge. */
	private Point leftBot;
	/** Pixel coordinates of top point of left vertical edge. */
	private Point leftTop;
	/** Pixel coordinates of bottom point of right vertical edge. */
	private Point rightBot;
	/** Pixel coordinates of top point of right vertical edge. */
	private Point rightTop;
	/** Pixel coordinates of bottom point of middle vertical line. */
	private Point midBot;
	/** Pixel coordinates of top point of middle vertical line. */
	private Point midTop;

	/** Real world 3D mid point of target relative to camera */
	private Point3 midPtToCamera;

	/** Real world 3D mid point of target relative to robot. */
	private Point3 midPtToRobot;

	/** How far robot should be rotated. */
	private double rotateRobot;

	/** How far from mid point of target to midpoint of robot. */
	private double robotDist;

	/** Calculator used to compute distance and bearing. */
	private LineSegmentTarget lineSegmentTool;

	/** Distance from camera to wall it is looking at. */
	private double cameraDist;

	/** How much camera needs to rotate (in degrees) to pull image to center. */
	private double rotateCamera;

	/** Used when drawing results onto image. */
	private DrawTool drawTool;

	/**
	 * Estimated angle of wall to camera (will be 0 if camera is pointing
	 * directly at wall).
	 */
	private double wallAngle;

	/**
	 * Construct a new instance with default settings (you will want to use the
	 * various "set" methods).
	 */
	public RectangularTarget() {
		this(22.0, 20.0);
	}

	/**
	 * Construct a new instance and set the dimensions of the target.
	 * 
	 * @param targetWidth
	 *            Real world width of the target (in, cm, etc - we don't care
	 *            but you must be consistent).
	 * @param targetHeight
	 *            Real world height of the target in same units.
	 */
	public RectangularTarget(double targetWidth, double targetHeight) {
		this(targetWidth, targetHeight, 640.0, 480.0, 45.0);
	}

	/**
	 * Construct a new instance and set the dimensions of the target.
	 * 
	 * @param targetWidth
	 *            Real world width of the target (in, cm, etc - we don't care
	 *            but you must be consistent).
	 * @param targetHeight
	 *            Real world height of the target in same units.
	 * @param imageWidth
	 *            Width of image in pixels.
	 * @param imageHeight
	 *            Height of image in pixels.
	 * @param fov
	 *            The full field of view of the camera in the vertical (covering
	 *            the height of the image) in degrees.
	 */
	public RectangularTarget(double targetWidth, double targetHeight,
			double imageWidth, double imageHeight, double fov) {
		this.fov = fov;
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
		this.imageWidthPx = imageWidth;
		this.imageHeightPx = imageHeight;
		hasSolution = false;
		// Assume camera is at point of rotation
		cameraLoc = new Point3(0, 0, 0);

		vertLineTolerance = 0.05;

		vertColors = new Scalar(80, 255, 255);
		vertThickness = 1;
		crossHairColor = new Scalar(255, 255, 80);
		crossHairThickness = 1;
		crossHairSize = 10;
		drawTool = new DrawTool();

		textBackground = new Scalar(0, 0, 0);
		textColor = new Scalar(255, 255, 255);

		// Vertical line segments
		leftBot = new Point();
		leftTop = new Point();
		rightBot = new Point();
		rightTop = new Point();
		midBot = new Point();
		midTop = new Point();

		// 3D coordinates of the middle of the target
		midPtToCamera = new Point3();
		midPtToRobot = new Point3();

		lineSegmentTool = new LineSegmentTarget(fov, imageWidth, imageHeight,
				targetHeight);
	}

	/**
	 * Adjust the style of the vertical lines drawn on the image.
	 * 
	 * @param color
	 *            The color you want for the vertical lines (or null if you
	 *            don't want to change it).
	 * @param thickness
	 *            The thickness in pixels (or 0 if you don't want to change it).
	 */
	public void setVerticalLineStyle(Scalar color, int thickness) {
		if (thickness > 0) {
			vertThickness = thickness;
		}
		if (color != null) {
			vertColors = color;
		}
	}

	/**
	 * Adjust the style of the cross hair drawn in the middle of the image.
	 * 
	 * @param color
	 *            The color you want (or null if you don't want to change it).
	 * @param size
	 *            How big (in pixels) you want the cross hair to be (or 0 if you
	 *            don't want to change it).
	 * @param thickness
	 *            The thickness in pixels (or 0 if you don't want to change it).
	 */
	public void setCrossHairStyle(Scalar color, int size, int thickness) {
		if (size > 0) {
			crossHairSize = size;
		}
		if (thickness > 0) {
			crossHairThickness = thickness;
		}
		if (color != null) {
			crossHairColor = color;
		}
	}

	/**
	 * Get the full field of view the camera has in the vertical dimension.
	 * 
	 * @return Field of view in degrees.
	 */
	public double getFov() {
		return fov;
	}

	/**
	 * Updates our internal line segment tool calculator based on current
	 * settings.
	 */
	private void resetLineSegmentTool() {
		hasSolution = false;
		lineSegmentTool = new LineSegmentTarget(fov, imageWidthPx,
				imageHeightPx, targetHeight);
	}

	/**
	 * Set the full field of view the camera has in the vertical dimension.
	 * 
	 * @param fov
	 *            Field of view in degrees.
	 */
	public void setFov(double fov) {
		this.fov = fov;
		resetLineSegmentTool();
	}

	/**
	 * Set the camera location on the robot relative to the center of rotation
	 * of the robot.
	 * 
	 * @param pt
	 *            Location of camera relative to center of rotation of robot (x
	 *            is left-/right+ of center, y is front+/back- of center, z is
	 *            typically from floor).)
	 */
	public void setCameraLocation(Point3 pt) {
		cameraLoc = pt;
		resetLineSegmentTool();
	}

	/**
	 * Set the camera's image size in pixels.
	 * 
	 * @param widthPx
	 *            Width in pixels in images produced by camera.
	 * @param heightPx
	 *            Height in pixels in images produced by camera.
	 */
	public void setImageSize(double widthPx, double heightPx) {
		imageWidthPx = widthPx;
		imageHeightPx = heightPx;
		resetLineSegmentTool();
	}

	/**
	 * Get the width of the images we are set up to process.
	 * 
	 * @return How wide (in pixels) the images are.
	 */
	public double getImageWidthPx() {
		return imageWidthPx;
	}

	/**
	 * Get the height of the images we are set up to process.
	 * 
	 * @return How tall (in pixels) the images are.
	 */
	public double getImageHeightPx() {
		return imageHeightPx;
	}

	/**
	 * Sets the real-world size of the target that is found in the image.
	 * 
	 * @param width
	 *            The width in real world units of the actual target.
	 * @param height
	 *            The height in real world units of the actual target.
	 */
	public void setTargetSize(double width, double height) {
		targetWidth = width;
		targetHeight = height;
		resetLineSegmentTool();
	}

	/**
	 * Get the real-world width of the target that is found in the image.
	 * 
	 * @return The width in real world units of the actual target.
	 */
	public double getTargetWidth() {
		return targetWidth;
	}

	/**
	 * Get the real-world height of the target that is found in the image.
	 * 
	 * @return The height in real world units of the actual target.
	 */
	public double getTargetHeight() {
		return targetHeight;
	}

	/**
	 * Indicates whether we successfully computed a solution or not.
	 * 
	 * @return true if we have a solution.
	 */
	public boolean hasSolution() {
		return hasSolution;
	}

	/**
	 * Helper method to append a 3D point to a string like a JSON array ([ X, Y,
	 * Z]).
	 * 
	 * @param sb
	 *            String builder to append to.
	 * @param pt
	 *            The 3D point coordinates.
	 */
	public static void jsonAppendPoint3(StringBuilder sb, Point3 pt) {
		sb.append('[');
		sb.append(pt.x);
		sb.append(", ");
		sb.append(pt.y);
		sb.append(", ");
		sb.append(pt.z);
		sb.append(']');
	}

	/**
	 * Builds a JSON looking string containing information about the location of
	 * the target to the camera and robot.
	 * 
	 * @return String with diagnostic values (for debugging or feeding to a JSON
	 *         parser).
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("{ \"hasSolution\":");
		boolean hasSolution = hasSolution();
		sb.append(hasSolution);
		if (hasSolution) {
			sb.append(", \"botCentDist\":");
			sb.append(robotDist);
			sb.append(", \"botRot\":");
			sb.append(rotateRobot);
			sb.append(", \"midPtFromBot\":");
			jsonAppendPoint3(sb, midPtToRobot);
			sb.append(", \"camCentDist\":");
			sb.append(cameraDist);
			sb.append(", \"camRot\":");
			sb.append(rotateCamera);
			sb.append(", \"midPtFromCam\":");
			jsonAppendPoint3(sb, midPtToCamera);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Attempt to compute a solution for a given polygon containing points for
	 * recognized target.
	 * 
	 * @param poly
	 *            The polygon to compute the solution for.
	 * @return true if we found a reasonable solution.
	 */
	public boolean computeSolution(PolygonCv poly) {

		if (poly.findLeftEdge(leftBot, leftTop, vertLineTolerance)
				&& poly.findRightEdge(rightBot, rightTop, vertLineTolerance)) {

			// Compute vertical mid line segment
			midBot.x = (leftBot.x + rightBot.x) / 2;
			midBot.y = (leftBot.y + rightBot.y) / 2;
			midTop.x = (leftTop.x + rightTop.x) / 2;
			midTop.y = (leftTop.y + rightTop.y) / 2;

			// Try to find a solution for the middle vertical line segment
			hasSolution = lineSegmentTool.setTargetCoordinates(midBot.x,
					midBot.y, midTop.x, midTop.y);
			if (hasSolution) {
				// Have a solution, compute the real world 3D coordinates for
				// the mid point of target
				// (both for camera and for rotation point of robot)
				midPtToCamera = lineSegmentTool.getPointMid();
				midPtToRobot.x = midPtToCamera.x + cameraLoc.x;
				midPtToRobot.y = midPtToCamera.y + cameraLoc.y;
				midPtToRobot.z = midPtToCamera.z + cameraLoc.z;

				double cameraDist2 = (midPtToCamera.x * midPtToCamera.x)
						+ (midPtToCamera.y * midPtToCamera.y);
				cameraDist = Math.sqrt(cameraDist2);
				rotateCamera = Math.toDegrees(Math.atan(midPtToCamera.x
						/ midPtToCamera.y));

				double robotDist2 = (midPtToRobot.x * midPtToRobot.x)
						+ (midPtToRobot.y * midPtToRobot.y);
				robotDist = Math.sqrt(robotDist2);
				rotateRobot = Math.toDegrees(Math.atan(midPtToRobot.x
						/ midPtToRobot.y));

				// To get wall angle and distance relative to camera (mounted
				// vertically), need two targets.
				// Wall angle (alpha), phi0 and phi1 are bearings to each
				// target,
				// l1 is real 2D line distance to point 1
				// w is target width in real world units
				// alpha = 90 - arcsin(w / l1 * sin(phi1 - phi0)) - phi0 - phi1
				lineSegmentTool.setTargetCoordinates(leftBot.x, leftBot.y,
						leftTop.x, leftTop.y);
				Point3 p0 = lineSegmentTool.getPointMid();
				lineSegmentTool.setTargetCoordinates(rightBot.x, rightBot.y,
						rightTop.x, rightTop.y);
				Point3 p1 = lineSegmentTool.getPointMid();

				double dx = p1.x - p0.x;
				double dy = p1.y - p0.y;
				wallAngle = Math.toDegrees(Math.atan(dy / dx));
				/*
				double targetWidthEst = Math.sqrt(dx * dx + dy * dy);
				System.out.println("p0: " + p0);
				System.out.println("p1: " + p1);
				System.out.println("dx: " + dx + "  dy: " + dy
						+ "  targetWidthEst: " + targetWidthEst);
						*/
			}
		}

		return hasSolution;
	}

	/**
	 * Draws 3 vertical lines on image (left, middle and right) if we have a
	 * solution.
	 * 
	 * @param output
	 *            Image to draw the lines on.
	 */
	public void drawVerticalLines(Mat output) {
		if (hasSolution()) {
			drawTool.setImage(output);
			drawTool.setColor(vertColors);
			drawTool.setThickness(vertThickness);

			drawTool.drawLine(leftBot, leftTop);
			drawTool.drawLine(rightBot, rightTop);
			drawTool.drawLine(midBot, midTop);
		}

	}

	/**
	 * Draws a cross hair on image at the center point of the target if we have
	 * a solution.
	 * 
	 * @param output
	 *            Image to draw the cross hair on.
	 */
	public void drawCrossHair(Mat output) {
		if (hasSolution()) {
			drawTool.setImage(output);
			drawTool.setColor(crossHairColor);
			drawTool.setThickness(crossHairThickness);
			double cx = (midBot.x + midTop.x) / 2;
			double cy = (midBot.y + midTop.y) / 2;
			Point p0 = new Point(cx - crossHairSize, cy);
			Point p1 = new Point(cx + crossHairSize, cy);
			drawTool.drawLine(p0, p1);

			p0.x = p1.x = cx;
			p0.y = cy - crossHairSize;
			p1.y = cy + crossHairSize;
			drawTool.drawLine(p0, p1);
		}
	}

	/**
	 * Helper method to draw text at the left edge.
	 * 
	 * @param img
	 *            Image to draw on.
	 * @param row
	 *            The row of the text you want to draw where 0 is top row, 1 is
	 *            next row, ... (NOTE: If the image is above the center, then we
	 *            go from the bottom up instead of top down).
	 * @param text
	 *            The text to draw.
	 */
	private void drawText(Mat img, int row, String text) {
		Size s = drawTool.getTextSize(text);
		drawTool.setBackgroundColor(textBackground);
		drawTool.setColor(textColor);

		int lineHeight = (int) (s.height + 2);

		int y = lineHeight * row;
		double cy = (midBot.y + midTop.y) / 2;
		if (cy < (imageHeightPx / 2)) {
			y = (int) (imageHeightPx - lineHeight - y);
		}

		drawTool.setImage(img);
		drawTool.drawTextTopLeft(text, 0, y);
	}

	/**
	 * Helper method to format and draw text information onto image.
	 * 
	 * @param img
	 *            Image to draw on.
	 * @param row
	 *            The row of the text you want to draw where 0 is top row, 1 is
	 *            next row, ... (NOTE: If the image is above the center, then we
	 *            go from the bottom up instead of top down).
	 * @param label
	 *            The label for the row.
	 * @param rotate
	 *            How much to rotate to center target (in degrees).
	 * @param dist
	 *            Distance to wall.
	 * @param pt
	 *            3D coordinates of target with respect to labeled object.
	 */
	private void drawTextInfo(Mat img, int row, String label, double rotate,
			double dist, Point3 pt) {
		StringBuilder sb = new StringBuilder(label);
		sb.append(": ");
		sb.append(String.format("%.1f", rotate));
		sb.append(" deg, ");
		sb.append(String.format("%.1f", dist));
		sb.append(" [");
		sb.append(String.format("%.1f", pt.x));
		sb.append(", ");
		sb.append(String.format("%.1f", pt.y));
		sb.append(", ");
		sb.append(String.format("%.1f", pt.z));
		sb.append("]");

		String text = sb.toString();
		drawText(img, row, text);
	}

	/**
	 * Draws solution information relative to camera onto image.
	 * 
	 * @param output
	 *            The image to draw the information on (will be drawn on row 1).
	 */
	public void drawCamInfo(Mat output) {
		if (hasSolution()) {
			drawTextInfo(output, 1, "CAM", rotateCamera, cameraDist,
					midPtToCamera);
		}
	}

	/**
	 * Draws solution information relative to robot onto image.
	 * 
	 * @param output
	 *            The image to draw the information on (will be drawn on row 0).
	 */
	public void drawRobotInfo(Mat output) {
		if (hasSolution()) {
			drawTextInfo(output, 0, "BOT", rotateRobot, robotDist, midPtToRobot);
		}
	}

	/**
	 * Draws solution information about the wall rotation relative to the
	 * camera.
	 * 
	 * @param output
	 *            The image to draw the information on (will be drawn on row 2).
	 */
	public void drawWallInfo(Mat output) {
		if (hasSolution()) {
			StringBuilder sb = new StringBuilder("WALL: ");
			sb.append(String.format("%.1f", wallAngle));
			sb.append(" deg");

			drawText(output, 2, sb.toString());
		}
	}

	/**
	 * Set the tolerance of how vertical the right and left edge lines must be in order to be considered valid.
	 * 
	 * @param tolerance The tolerance in the range 0.0 (completely vertical) to 1.0 (way off of vertical).
	 */
	public void setVerticalLineTolerance(double tolerance) {
		this.vertLineTolerance = tolerance;
	}

}
