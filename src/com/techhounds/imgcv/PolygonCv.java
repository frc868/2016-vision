/*
 * Copyright (c) 2014, pkb
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.techhounds.imgcv;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * A representation of Polygon objects and related methods typically used when
 * creating object detection filters using the opencv graphics API.
 *
 * <p>
 * In addition to the individual points making up the polygon, this
 * implementation also keeps track of the polygon's "bounding box" (the smallest
 * rectangle which contains all of the points making up the polygon).
 * </p>
 *
 * @author pkb
 */
public class PolygonCv {

	/**
	 * Points defining the vertices of the polygon.
	 */
	private final MatOfPoint2f _Points;

	/**
	 * Constant used to identify the index of the X component within a point
	 * object.
	 */
	public static final int X = 0;
	/**
	 * Constant used to identify the index of the Y component within a point
	 * object.
	 */
	public static final int Y = 1;
	/**
	 * Minimum X value in all points.
	 */
	private float _MinX;
	/**
	 * Maximum X value in all points.
	 */
	private float _MaxX;
	/**
	 * Minimum Y value in all points.
	 */
	private float _MinY;
	/**
	 * Maximum Y value in all points.
	 */
	private float _MaxY;
	/**
	 * Width of the the "bounding box" (Xmax - Xmin).
	 */
	private float _Width;
	/**
	 * Height of the "bounding box" (Ymax - Ymin).
	 */
	private float _Height;
	/**
	 * Aspect ratio of the "bounding box".
	 */
	private float _AspectRatio;
	/**
	 * Area of the "bounding box".
	 */
	private float _BoundsArea;

	/**
	 * Will be true if first and last point in polygon point list are the same.
	 */
	private boolean _Closed;

	/**
	 * Constructs a new instance of a polygon with 0 points.
	 */
	public PolygonCv() {
		_Points = new MatOfPoint2f();
		analyze();
	}

	/**
	 * Constructs a new instance of a polygon with the specified set of points.
	 *
	 * @param points
	 *            Matrix of coordinates where each matrix entry is a vector of
	 *            at least two values (X is taken from first entry in vector and
	 *            Y is taken from second).
	 */
	public PolygonCv(MatOfPoint2f points) {
		_Points = points;
		analyze();
	}

	/**
	 * Constructs a new {@link PolygonCv} object by analyzing a matrix of
	 * contour points.
	 *
	 * @param contour
	 *            A single opencv contour (a pixel outline tracing of a single
	 *            object within a image).
	 * @param epsilon
	 *            This value is used to influence how aggressive the routine is
	 *            about reducing countour points to lines. See
	 *            {@link Imgproc#approxPolyDP(org.opencv.core.MatOfPoint2f, org.opencv.core.MatOfPoint2f, double, boolean)}
	 *            for details.
	 * @return A new {@link PolygonCv} object containing a reduced set of
	 *         polygon lines the "trace over" the rough contour points.
	 */
	public static PolygonCv fromContour(MatOfPoint contour, double epsilon) {
		MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
		MatOfPoint2f poly = new MatOfPoint2f();
		Imgproc.approxPolyDP(contour2f, poly, epsilon, true);
		return new PolygonCv(poly);
	}

	/**
	 * Get the number of points in the polygon.
	 *
	 * @return Number of points you can look at.
	 */
	public int size() {
		return _Points.rows();
	}

	/**
	 * Returns the minimum X value of all of the points in the polygon.
	 *
	 * @return Minimum X value.
	 */
	public float getMinX() {
		return _MinX;
	}

	/**
	 * Returns the minimum Y value of all of the points in the polygon.
	 *
	 * @return Minimum Y value.
	 */
	public float getMinY() {
		return _MinY;
	}

	/**
	 * Returns the maximum X value of all of the points in the polygon.
	 *
	 * @return Maximum X value.
	 */
	public float getMaxX() {
		return _MaxX;
	}

	/**
	 * Returns the maximum Y value of all of the points in the polygon.
	 *
	 * @return Maximum Y value.
	 */
	public float getMaxY() {
		return _MaxY;
	}

	/**
	 * Returns the X value of the center point of the bounding box.
	 *
	 * @return Center X value of rectangle containing all of the polygon's
	 *         points.
	 */
	public float getCenterX() {
		return (_MinX + _MaxX) / 2;
	}

	/**
	 * Returns the Y value of the center point of the bounding box.
	 *
	 * @return Center Y value of rectangle containing all of the polygon's
	 *         points.
	 */
	public float getCenterY() {
		return (_MinY + _MaxY) / 2;
	}

	/**
	 * Returns the width of the bounding box containing all of the points in the
	 * polygon.
	 *
	 * @return The width of the bounding box.
	 */
	public float getWidth() {
		return _Width;
	}

	/**
	 * Returns the height of the bounding box containing all of the points in
	 * the polygon.
	 *
	 * @return The height of the bounding box.
	 */
	public float getHeight() {
		return _Height;
	}

	/**
	 * Indicates whether the first and last point in the polygon are the same
	 * (closed).
	 * 
	 * @return true If the first point is the same as the last point (closed),
	 *         false if you must connect yourself (open).
	 */
	public boolean isClosed() {
		return _Closed;
	}

	/**
	 * Returns the aspect ratio of the polygon's bounding box (width/height).
	 *
	 * @return Bounding box aspect ratio (< 1.0 indicates tall and skinny, > 1.0
	 *         indicates short and fat).
	 */
	public float getBoundingAspectRatio() {
		return _AspectRatio;
	}

	/**
	 * Returns the area of the polygon's bounding box.
	 *
	 * @return Bounding box area.
	 */
	public float getBoundingArea() {
		return _BoundsArea;
	}

	/**
	 * Get the (x, y) coordinates of a specific point in the polygon.
	 *
	 * @param i
	 *            The index of the point in the range [0, {@link #size()} - 1].
	 * @param data
	 *            Array to store the results in (must be able to hold two
	 *            elements). The X value will be put into data[{@link #X}] and
	 *            the Y value will be put into data[{@link #Y}].
	 */
	public void getPoint(int i, float[] data) {
		_Points.get(i, 0, data);
	}

	/**
	 * Retrieve a specific point from the polygon.
	 *
	 * @param i
	 *            The index of the point in the range [0, {@link #size()} - 1].
	 * @return A {@link Point} object containing the coordinates of the
	 *         requested point.
	 */
	public Point getPoint(int i) {
		float[] vals = new float[2];
		_Points.get(i, 0, vals);
		return new Point(vals[X], vals[Y]);
	}

	/**
	 * Returns the entire collection of points making up the polygon.
	 *
	 * @return Collection of points (floating point values).
	 */
	public MatOfPoint2f toMatOfPoint2f() {
		return _Points;
	}

	/**
	 * Converts the floating point values into regular points that can be used
	 * by opencv methods that work with contours.
	 *
	 * @return A collection of points in a format compatible with opencv
	 *         "contours".
	 */
	public MatOfPoint toContour() {
		return new MatOfPoint(_Points.toArray());
	}

	/**
	 * Attempts to find the left most vertical edge of the polygon (height might
	 * be less than the bounding box height).
	 * 
	 * @param bot
	 *            The bottom end point of the "best fit" line on the left edge.
	 * @param top
	 *            The top end point of the "best fit" line on the left edge.
	 * @param distLimit
	 *            The distance from left edge of bounding box that you are
	 *            willing to accept points (in the range of 0.0 to 1.0).
	 * 
	 * @return true if we were able to find a best fit line segement and set the
	 *         bot and top values.
	 */
	public boolean findLeftEdge(Point bot, Point top, double distLimit) {
		double minX = getMinX();
		double width = getWidth();
		double maxX = minX + (width * distLimit);
		return findVerticalSegment(bot, top, minX, maxX);
	}

	/**
	 * Attempts to find the right most vertical edge of the polygon (height
	 * might be less than the bounding box height).
	 * 
	 * @param bot
	 *            The bottom end point of the "best fit" line on the right edge.
	 * @param top
	 *            The top end point of the "best fit" line on the right edge.
	 * @param distLimit
	 *            The distance from right edge of bounding box that you are
	 *            willing to accept points (in the range of 0.0 to 1.0).
	 * 
	 * @return true if we were able to find a best fit line segement and set the
	 *         bot and top values.
	 */
	public boolean findRightEdge(Point bot, Point top, double distLimit) {
		double maxX = getMaxX();
		double width = getWidth();
		double minX = maxX - (width * distLimit);
		return findVerticalSegment(bot, top, minX, maxX);
	}

	/**
	 * Finds the upper and lower two points in a vertical segment of the polygon
	 * (typically used to find the right and left edges of polygon).
	 * 
	 * @param bot
	 *            The bottom end point of the "best fit" line on the right edge.
	 * @param top
	 *            The top end point of the "best fit" line on the right edge.
	 * @param minX
	 *            The minimum X value for a polygon vertice to be included.
	 * @param maxX
	 *            The maximum X value for a polygon vertice to be included.
	 * 
	 * @return true if we were able to find a "vertical" line segement and set
	 *         the bot and top values.
	 */
	public boolean findVerticalSegment(Point bot, Point top, double minX,
			double maxX) {
		ArrayList<Point> pts = getPointsInXRange(minX, maxX);

		// Need at least two points
		int n = pts.size();
		if (n < 2) {
			return false;
		}

		// Move ends to upper and lower
		Point pt0 = pts.get(0);
		bot.x = top.x = pt0.x;
		bot.y = top.y = pt0.y;
		for (int i = 1; i < n; i++) {
			Point pti = pts.get(i);
			double x = pti.x;
			double y = pti.y;

			if (y < bot.y) {
				bot.y = y;
				bot.x = x;
			} else if (y > top.y) {
				top.y = y;
				top.x = x;
			}
		}
		return true;
	}

	/**
	 * Goes through entire set of points in polygon and returns list of points
	 * whose x coordinate falls in specified range (useful when looking for
	 * vertical lines).
	 * 
	 * @param minX
	 *            Minimum x value of points to accept.
	 * @param maxX
	 *            Maximum x value of points to accept.
	 * @return A array list of 0 or more points in the range.
	 */
	public ArrayList<Point> getPointsInXRange(double minX, double maxX) {
		ArrayList<Point> points = new ArrayList<Point>();
		int n = size();

		for (int i = 0; i < n; i++) {
			Point p = getPoint(i);
			double x = p.x;
			if ((x >= minX) && (x <= maxX)) {
				points.add(p);
			}
		}

		return points;
	}

	/**
	 * Draws the polygon onto a image.
	 *
	 * @param img
	 *            The image to draw on.
	 * @param color
	 *            The color to use for the lines of the polygon.
	 * @param thickness
	 *            The thickness of the line (in pixels).
	 * @param lineType
	 *            The line type (see
	 *            {@link Core#polylines(org.opencv.core.Mat, java.util.List, boolean, org.opencv.core.Scalar, int)}
	 *            .
	 * @param shift
	 *            Whether or not the points in the polygon need to be shifted
	 *            (see
	 *            {@link Core#polylines(org.opencv.core.Mat, java.util.List, boolean, org.opencv.core.Scalar, int, int, int)
	 *            .
	 */
	public void draw(Mat img, Scalar color, int thickness, int lineType,
			int shift) {
		List<MatOfPoint> polys = new ArrayList<>();
		polys.add(toContour());
		Core.polylines(img, polys, true, color, thickness, lineType, shift);
	}

	/**
	 * Draws the polygon onto a image using {@link Core.LINE_8} as the line type
	 * and no shifting.
	 *
	 * @param img
	 *            The image to draw on.
	 * @param color
	 *            The color to use for the lines of the polygon.
	 * @param thickness
	 *            The thickness of the line (in pixels).
	 */
	public void draw(Mat img, Scalar color, int thickness) {
		draw(img, color, thickness, Core.LINE_8, 0);
	}

	/**
	 * Draws text information about the polygon onto a image.
	 *
	 * <p>
	 * This method draws textual information about the polygon (position, size,
	 * etc) onto the image close to where the polygon appears.
	 * </p>
	 *
	 * @param img
	 *            Image to draw text information on.
	 * @param fontFace
	 *            The opencv constant indicating the font to use for the text
	 *            (see {@link Core#putText}).
	 * @param fontScale
	 *            How much to scale the font by (1.0 for no scaling).
	 * @param color
	 *            The color the text should be drawn in.
	 * @param thickness
	 *            The thickness (in pixels) to use when drawing.
	 */
	public void drawInfo(Mat img, int fontFace, double fontScale, Scalar color,
			int thickness) {
		int[] baseline = { 0 };
		StringBuilder dimen = new StringBuilder("(");
		dimen.append(getMinX());
		dimen.append(", ");
		dimen.append(getMinY());
		dimen.append(")");
		String text = dimen.toString();

		Size size = Core.getTextSize(text, fontFace, fontScale, thickness,
				baseline);
		Point p = new Point(getMinX(), getMinY() - baseline[0]);
		Core.putText(img, text, p, fontFace, fontScale, color, thickness);

		dimen.setLength(0);
		dimen.append(getWidth());
		dimen.append("x");
		dimen.append(getHeight());
		text = dimen.toString();

		p = new Point(getMinX(), getMaxY() + 2 + size.height);
		Core.putText(img, text, p, fontFace, fontScale, color, thickness);
	}

	/**
	 * Draws text information about the polygon onto a image.
	 *
	 * <p>
	 * This method draws textual information about the polygon (position, size,
	 * etc) onto the image close to where the polygon appears.
	 * </p>
	 *
	 * @param img
	 *            Image to draw text information on.
	 * @param color
	 *            The color the text should be drawn in.
	 */
	public void drawInfo(Mat img, Scalar color) {
		drawInfo(img, Core.FONT_HERSHEY_PLAIN, 0.75, color, 1);
	}

	/**
	 * Generates a string representation of the polygon for debug output.
	 *
	 * @return A string containing information about the polygon.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PolygonCv(n=");
		sb.append(size());
		sb.append(", x=");
		sb.append(getMinX());
		sb.append(", y=");
		sb.append(getMinY());
		sb.append(", width=");
		sb.append(getWidth());
		sb.append(", height=");
		sb.append(getHeight());
		sb.append(", aspect=");
		sb.append(getBoundingAspectRatio());
		sb.append(", area=");
		sb.append(getBoundingArea());
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Internal helper method to perform some analysis on the points making up
	 * the polygon.
	 *
	 * <p>
	 * This method looks at all of the points in the polygon to pre-compute some
	 * composite values (like those related to the polygon's bounding box).
	 */
	private void analyze() {
		int n = size();
		float[] pt = new float[2];
		float[] minPt = new float[2];
		float[] maxPt = new float[2];
		_Closed = false;

		// If no points, just set everything to 0
		if (n < 1) {
			_MinX = _MaxX = _MinY = _MaxY = 0;
			_Width = _Height = 0;
			_AspectRatio = _BoundsArea = 0;
			return;
		}

		// Initialize min/max values with first point
		getPoint(0, minPt);
		getPoint(0, maxPt);

		// Iterate through rest of points looking for new min/max values
		for (int i = 1; i < n; i++) {
			getPoint(i, pt);
			float x = pt[X];
			float y = pt[Y];

			if (y < minPt[Y]) {
				minPt[Y] = y;
			} else if (y > maxPt[Y]) {
				maxPt[Y] = y;
			}

			if (x < minPt[X]) {
				minPt[X] = x;
			} else if (x > maxPt[X]) {
				maxPt[X] = x;
			}
		}

		// Set determined values
		_MinX = minPt[X];
		_MaxX = maxPt[X];
		_MinY = minPt[Y];
		_MaxY = maxPt[Y];
		_Width = _MaxX - _MinX;
		_Height = _MaxY - _MinY;
		_AspectRatio = (_Height > 0) ? (_Width / _Height) : Float.MAX_VALUE;
		_BoundsArea = (_Width * _Height);

		// Check if closed
		float[] firstPt = new float[2];
		getPoint(0, firstPt);
		float[] lastPt = new float[2];
		getPoint(n - 1, lastPt);
		_Closed = (firstPt[X] == lastPt[X]) && (firstPt[Y] == lastPt[Y]);
	}

	/**
	 * Determines if the bounding box of this polygon contains the bounding box
	 * of another polygon.
	 *
	 * @param p
	 *            The polygon to check.
	 * @return Will return true if the bounding box of "p" falls inside the
	 *         bounding box of this polygon.
	 */
	public boolean contains(PolygonCv p) {
		return (getMinX() <= p.getMinX()) && (getMaxX() >= p.getMaxX())
				&& (getMinY() <= p.getMinY()) && (getMaxY() >= p.getMaxY());
	}

}
