package com.techhounds.imgcv.pinksquare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.techhounds.imgcv.PolygonCv;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.ColorSpace;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.Morphology;
import com.techhounds.imgcv.filters.Sequence;
import com.techhounds.imgcv.utils.*;

/**
 * An image filter that looks for a large neon pink rectangle that is 22 in wide
 * and 20.125 in tall.
 */
public class FindPinkRectangleFilter implements MatFilter {
	/** Will be true if we found the pink rectangle. */
	private boolean _Found;

	/**
	 * The set of filters to apply to reduce a source image to a black and white
	 * image where (hopefully) the large white rectangle corresponds to the pink
	 * rectangle.
	 */
	private Sequence _Filter;

	/**
	 * A rectangle finder tool that helps locate the image relative to the
	 * camera/robot in real world coordinates.
	 */
	private RectangularTarget _Finder;

	// filter instances

	// Converts BGR to HSV
	private final MatFilter _ColorSpace;

	// Looks for specific colors in HSV space
	private ColorRange _ColorRange;

	// private final MatFilter _Dilate1 = new Dilate(6);
	// private final MatFilter _Erode;
	// private final MatFilter _Dilate2;
	private MatFilter _Morph;

	// Set to true for more diagnostic output to console
	private boolean _Debug;

	// ID of how filter is being used as this filter is fairly adjustable
	private String _Id;

	// Will be non-null if we should draw a rectangular targeting outline
	// where we want the target to appear
	private Point goodMinPoint;
	private Point goodMaxPoint;
	private Point goodTopMid;
	private Point goodBotMid;

	// Used for drawing markers and indicators on image
	private DrawTool _DrawTool;


	// Used to draw "target region" based on whether target is in "good" area of screen
	private static final Scalar IN_REGION_COLOR = new Scalar(100, 255, 100);
	private static final Scalar OUT_REGION_COLOR = new Scalar(150, 100, 50);
	private static final Scalar CENTER_REGION_COLOR = new Scalar(100, 100, 100);

	/**
	 * Constructs a new instance of the filter for a pink rectangle that is 22
	 * in wide and 20.125 in tall using a camera that is set to 640x480 with a
	 * FOV of 44.136 degrees across the 480 pixels.
	 */
	public FindPinkRectangleFilter() {
		_Found = false;
		_Debug = false;
		_Id = "pink";
		_DrawTool = new DrawTool();

		_ColorSpace = ColorSpace.createBGRtoHSV();
		// _Erode = new Erode(6);
		// _Dilate2 = new Dilate(8);
		_Morph = new Morphology(2);

		int[] colorFilterMin = { 140, 80, 100 };
		int[] colorFilterMax = { 200, 240, 255 };
		_ColorRange = new ColorRange(colorFilterMin, colorFilterMax, true);

		_Filter = createSequence();
		// For Lenovo Web Cam
		// _Finder = new RectangularTarget(22, 20.125, 640, 480, 44.136 /* 56.75
		// */);
		_Finder = new RectangularTarget(22, 20.125, 640, 480, 31.638 /* 56.75 */);
		_Finder.setCameraLocation(new Point3(-8.5, 12, 11));
		// Let vertical lines be off as much as 10% of width
		_Finder.setVerticalLineTolerance(0.1);
		loadColorRanges(_Id);
	}

	/**
	 * Static method which makes adjustments to default construction to so the
	 * tool can be used to locate the 2016 target.
	 * 
	 * @return A image filter tuned for the 2016 FRC reflective target below the
	 *         goals.
	 */
	public static FindPinkRectangleFilter createFor2016Target() {
		FindPinkRectangleFilter filter = new FindPinkRectangleFilter();
		filter._Id = "frc-2016";

		int[] minVals = { 40, 40, 40 };
		int[] maxVals = { 130, 210, 255 };
		filter._ColorRange = new ColorRange(minVals, maxVals, true);
		filter.loadColorRanges(filter._Id);

		Morphology morph = new Morphology(3);
		filter._Morph = morph;

		filter._Filter = filter.createSequence();

		// Axis camera has 
		//final double FOV_AXIS_M1018_X_DEGREES       = 67; 
		final double FOV_AXIS_M1018_Y_DEGREES       = 51;
		double projHt = 14 * Math.cos(Math.toRadians(40));
		RectangularTarget finder = new RectangularTarget(20, projHt, 800, 600,
				FOV_AXIS_M1018_Y_DEGREES /* 56.75 */);
		// Camera offset on robot
		finder.setCameraLocation(new Point3(-9.0, 12.5, 12));
		// Let vertical lines be off as much as 10% of width
		finder.setVerticalLineTolerance(0.1);
		filter._Finder = finder;
		
		filter.setGoodRegion(new Point(375, 50), new Point(525, 400));

		return filter;
	}
	
	/**
	 * Enable debug (diagnostic output) to system.out.
	 * 
	 * @param enable Pass true to enable, false to disable.
	 */
	public void setDebug(boolean enable) {
		_Debug = enable;
	}

	/**
	 * Returns short ASCII ID (like "pink" or "frc-2016") used for loading
	 * configuration and saving images.
	 * 
	 * @return A short ASCII ID set at time of construction.
	 */
	public final String getId() {
		return _Id;
	}

	/**
	 * Helper method to load color range values in from a file (if present).
	 * 
	 * @param cfgName
	 *            Simple name (like "pink") of the default configuration to
	 *            load.
	 */
	private void loadColorRanges(String cfgName) {
		try {
			// See if there are color values saved that we can load
			File f = ColorRangeValues.getFile(cfgName);
			ColorRangeValues crv = new ColorRangeValues();
			crv.loadSettings(f);
			_ColorRange.setColorRangeValues(crv);
		} catch (IllegalArgumentException | IOException e) {
			// If error, we just use the built-in values
		}
	}

	/**
	 * Constructs a sequence of filters to reduce a color image to a black and
	 * white where the white rectangle left in the image corresponds to the pink
	 * rectangle we are designed to find.
	 * 
	 * @return A sequence filter that can be applied to any color image to
	 *         produce a black and white image.
	 */
	public Sequence createSequence() {
		Sequence s = new Sequence();
		s.addFilter(_ColorSpace);
		s.addFilter(_ColorRange);
		// s.addFilter(_Dilate1);
		// s.addFilter(_Erode);
		// s.addFilter(_Dilate2);
		s.addFilter(_Morph);
		return s;
	}

	/**
	 * Get direct access to the color range filter.
	 * 
	 * @return Reference to the internal color range filter.
	 */
	public ColorRange getColorRangeFilter() {
		return _ColorRange;
	}

	/**
	 * Applies the filter to an image drawing results onto the image and
	 * updating the state of the filter.
	 */
	public Mat process(Mat srcImage) {
		_Found = false;
		int hImg = srcImage.rows();
		int wImg = srcImage.cols();
		int imgMid = hImg / 2;
		Mat output = srcImage;

		Mat copy = srcImage.clone();
		Mat d1 = _Filter.process(copy);

		// Uncomment to use BW image as output to draw on
		// Imgproc.cvtColor(d1, output, Imgproc.COLOR_GRAY2BGR);

		List<MatOfPoint> contours = new ArrayList<>();
		List<PolygonCv> polygons = new ArrayList<>();

		float maxWidth = -1;

		Mat heirarchy = new Mat();
		Imgproc.findContours(d1, contours, heirarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		int n = contours.size();
		for (int i = 0; i < n; i++) {
			MatOfPoint contour = contours.get(i);
			// Hmmm, can we do a quick check on contour height/width before
			// trying to extract polygon?
			PolygonCv poly = PolygonCv.fromContour(contour, 6.0);
			int pts = poly.size();
			float h = poly.getHeight();
			float w = poly.getWidth();
			int hw = (int) (w > 0 ? h / w * 100 : 0);
			float distFromTop = poly.getMinY();
			float distFromMid = imgMid - (distFromTop + h);

			if ((w > 10) && (h > 10) && (hw > 50) && (hw < 300) && (pts >= 4) && (pts <= 16)) {
				Point leftBot = new Point();
				Point leftTop = new Point();
				poly.findLeftEdge(leftBot, leftTop, 0.15);

				Point rightBot = new Point();
				Point rightTop = new Point();
				poly.findRightEdge(rightBot, rightTop, 0.15);

				double leftHeight = Math.abs(leftTop.y - leftBot.y);
				double rightHeight = Math.abs(rightTop.y - rightBot.y);
				double heightRatio = (leftHeight > 1) ? rightHeight / leftHeight : 0;
				if (leftHeight >= 10 && rightHeight >= 10 && (heightRatio > 0.75) && (heightRatio < 1.25)) {
					polygons.add(poly);
					_Found = true;
					if (w > maxWidth) {
						maxWidth = w;
					}
					if (_Debug) {
						System.out.println("Accepted: sides: " + pts + " (" + poly.getWidth() + ", " + poly.getHeight()
								+ ")  H/W: " + hw + "  distFromTop: " + distFromTop + "  distFromMid: " + distFromMid);
					}
				} else {
					if (_Debug) {
						System.out.println("Rejected: left height: " + leftHeight + "  right height: " + rightHeight);
					}
				}

			} else if (_Debug) {
				System.out.println("Rejected: sides: " + pts + " (" + poly.getWidth() + ", " + poly.getHeight()
						+ ")  H/W: " + hw + "  distFromTop: " + distFromTop + "  distFromMid: " + distFromMid);
			}
		}

		int pCnt = polygons.size();
		PolygonCv[] pArr = new PolygonCv[pCnt];
		polygons.toArray(pArr);

		// Not searching for pairs, just add contours for all of the polygons
		PolygonCv good = null;

		for (int i = 0; i < pCnt; i++) {
			PolygonCv p = pArr[i];
			float w = p.getWidth();
			if ((w == maxWidth) && (good == null)) {
				p.draw(output, ScalarColors.GREEN, 1);
				p.drawInfo(output, ScalarColors.GREEN);

				_Finder.setImageSize(wImg, hImg);
				if (_Finder.computeSolution(p)) {
					good = p;
				}
				if (_Debug) {
					System.out.println(_Finder);
				}
			} else {
				p.draw(output, ScalarColors.BLUE, 1);
			}
		}

		// Draw target region (if enabled)
		if (isTargetRegionEnabled()) {
			_DrawTool.setImage(output);
			_DrawTool.setThickness(3);
			_DrawTool.setColor(inGoodRegion(good) ? IN_REGION_COLOR : OUT_REGION_COLOR);
			_DrawTool.drawRectangle(goodMinPoint, goodMaxPoint);
			_DrawTool.setThickness(1);
			_DrawTool.setColor(CENTER_REGION_COLOR);
			_DrawTool.drawLine(goodTopMid, goodBotMid);
		}

		// Draw details about target (if found)
		if (good != null) {
			_Finder.drawVerticalLines(output);
			_Finder.drawCrossHair(output);
			_Finder.drawCamInfo(output);
			_Finder.drawRobotInfo(output);
			_Finder.drawWallInfo(output);
		}

		return output;
	}

	/**
	 * Defines a region on the image where the target should be if it is in a
	 * "GOOD" position.
	 * 
	 * @param pt1
	 *            Corner point of good region.
	 * @param pt2
	 *            Opposite corner point.
	 */
	public void setGoodRegion(Point pt1, Point pt2) {
		double x1 = pt1.x;
		double x2 = pt2.x;
		double y1 = pt1.y;
		double y2 = pt2.y;
		double xMid = (x1 + x2) / 2;
		goodMinPoint = new Point(Math.min(x1, x2), Math.min(y1, y2));
		goodMaxPoint = new Point(Math.max(x1, x2), Math.max(y1, y2));
		goodTopMid = new Point(xMid, goodMinPoint.y);
		goodBotMid = new Point(xMid, goodMaxPoint.y);		
	}
	
	/**
	 * Indicates whether the "good region" has been defined.
	 * 
	 * @return true if a "good region" has been set on the filter.
	 */
	public boolean isTargetRegionEnabled() {
		return (goodMaxPoint != null) && (goodMinPoint != null);
	}

	/**
	 * Determine whether or not polygon found falls within the "good region" (if
	 * defined).
	 * 
	 * @param poly
	 *            The polygon to check (if you pass null we return false).
	 * @return true if "good region" defined and polygon is within it, otherwise
	 *         false.
	 */
	private boolean inGoodRegion(PolygonCv poly) {
		if (poly == null || !isTargetRegionEnabled()) {
			return false;
		}

		return (poly.getMinY() >= goodMinPoint.y) && (poly.getMaxY() <= goodMaxPoint.y)
				&& (poly.getMinX() >= goodMinPoint.x) && (poly.getMaxX() <= goodMaxPoint.x);
	}

	/**
	 * Indicates whether we found the pink rectangle in the last image that was
	 * processed.
	 * 
	 * @return true if we found the pink rectangle, false if not.
	 */
	public boolean wasFound() {
		return _Found;
	}
}
