package com.techhounds.imgcv.utils;

/**
 * Class used to convert between pixel offset from center and angle.
 * 
 * <p>
 * Class based on Jared Russel post
 * (http://www.chiefdelphi.com/forums/showthread.php?threadid=147036) and
 * Wikipedia article (https://en.wikipedia.org/wiki/Angle_of_view).
 * </p>
 * 
 * <p>
 * NOTE: This is a simplified (more generic) FOV calculator, you need to work
 * with values that are offsets from the center of your image. You can typically
 * construct a single instance of this object using constants for either your
 * camera's horizontal (common) or vertical FOV values. Once constructed, this
 * instance can typically be used for both horizontal and vertical calculations
 * as the focal length of cameras tends to be the same in each direction.
 * </p>
 *
 */
public final class AovCalculator {

	/** Holds focal length in pixels. */
	private double _FocalLengthPixels;

	/**
	 * Constructs a new instance where you know the focal length in pixels.
	 * 
	 * @param focalLengthPixels
	 *            The focal length of the camera in pixels.
	 */
	public AovCalculator(double focalLengthPixels) {
		_FocalLengthPixels = focalLengthPixels;
	}

	/**
	 * Constructs a new instance where you know the field of view of the camera
	 * and the number of pixels spanned by the field of view.
	 * 
	 * @param fovDeg
	 *            Typically the entire horizontal field of view of the camera in
	 *            degrees.
	 * @param pixelsSpanned
	 *            Typically the horizontal width of the image in pixels.
	 */
	public AovCalculator(double fovDeg, double pixelsSpanned) {
		this(computeFocalLengthPixels(fovDeg, pixelsSpanned));
	}

	/**
	 * Computes focal length in pixels given camera constants.
	 * 
	 * @param fovDeg
	 *            Typically the entire horizontal field of view of the camera in
	 *            degrees.
	 * @param pixelsSpanned
	 *            Typically the horizontal width of the image in pixels.
	 * @return The focal length in pixels.
	 */
	public static double computeFocalLengthPixels(double fovDeg, double pixelsSpanned) {
		return 0.5 * pixelsSpanned / Math.tan(Math.toRadians(fovDeg) / 2);
	}

	/**
	 * Computes angle from center given a pixel offset from center.
	 * 
	 * @param pixelsFromCenter
	 *            The pixel offset from the center of the image.
	 * @return The angle (in signed degrees).
	 */
	public double toAngle(double pixelsFromCenter) {
		double angle = Math.atan(pixelsFromCenter / _FocalLengthPixels);
		angle = Math.toDegrees(angle);
		return angle;
	}

	/**
	 * Computes pixel location from center given an angle offset from center.
	 * 
	 * @param angleFromCenter
	 *            The signed angle offset from center.
	 * @return The signed pixel offset from the center of the image.
	 */
	public double toPixel(double angleFromCenter) {
		double pixels = Math.tan(Math.toRadians(angleFromCenter)) * _FocalLengthPixels;
		return pixels;
	}

	/**
	 * Simple test method to dump a table and check computed values.
	 * 
	 * @param args
	 *            Command line arguments (ignored).
	 */
	public static void main(String[] args) {
		double fov = 60;
		double width = 800;
		FovCalculator fc = new FovCalculator(fov, width, 100);
		AovCalculator ac = new AovCalculator(fov, width);

		System.out.println("Pixel  FOV Ang  FOV Pix  AOV Ang  AOV Pix  AngDiff");
		System.out.println("-----  -------  -------  -------  -------  -------");
		for (double pixel = -(width / 2); pixel <= (width / 2); pixel += 5) {
			double fcAng = fc.pixelFromCenterToDeg(pixel);
			double acAng = ac.toAngle(pixel);
			double fcPix = fc.degFromCenterToPixel(fcAng);
			double acPix = ac.toPixel(acAng);
			double diff = (fcAng - acAng);
			String line = String.format("%5d  %7.2f  %7.2f  %7.2f  %7.2f  %7.4f", (int) pixel, fcAng, fcPix, acAng,
					acPix, diff);
			System.out.println(line);
		}
	}
}
