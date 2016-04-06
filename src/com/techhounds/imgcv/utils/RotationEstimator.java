package com.techhounds.imgcv.utils;

/**
 * Class used to estimate degrees to rotate robot by when camera is not located
 * at the same point as the robot rotates around.
 */
public class RotationEstimator {

	// How far to right of camera is center of robot rotation
	private double _RobotX;
	// How far back of camera is center of robot rotation
	private double _RobotY;

	/**
	 * Construct a new instance for a camera positioned relative to robot's
	 * center of rotation.
	 * 
	 * @param xRobotOfs
	 *            How far to right of camera is center of robot rotation.
	 * @param yRobotOfs
	 *            How far back of camera is center of robot rotation
	 */
	public RotationEstimator(double xRobotOfs, double yRobotOfs) {
		_RobotX = xRobotOfs;
		_RobotY = yRobotOfs;
	}

	/**
	 * Computes estimate for how much to rotate robot to get target lined up.
	 * 
	 * @param camAng
	 *            Angle to target from camera's perspective (in degrees).
	 * @param distEst
	 *            Estimated distance to the wall (in same units as measurements
	 *            passed to constructor).
	 * @return How much to rotate the robot by (in degrees).
	 */
	public double computeRotation(double camAng, double distEst) {
		// Estimate x (width) of camera based on angle and estimated distance
		double wEst = distEst * Math.tan(Math.toRadians(camAng));
		double dBot = distEst + _RobotY; // Distance to center of rotation
		double wBot = wEst - _RobotX;
		double thetaEst = Math.atan(wBot / dBot);
		return Math.toDegrees(thetaEst);
	}

	/**
	 * Dumps table of values showing how much error in angle estimate is
	 * expected for the 2016 robot.
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String[] args) {
		// Center of robot is 9 inches to right and 12 inches back of camera
		RotationEstimator estimator = new RotationEstimator(9, 12);

		System.out.println("Dist  CamAng ActAng EstAng EstErr CamErr Better");
		for (int angle = -26; angle <= 26; angle += 2) {
			for (int dist = 75; dist <= 125; dist += 25) {
				double theta = estimator.computeRotation(angle, dist);

				// Use mid point of shooting range as distance estimate
				double distEst = (75 + 120) * 3 / 5;
				double thetaEst = estimator.computeRotation(angle, distEst);
				double err = theta - thetaEst;
				double camErr = theta - angle;
				boolean better = (Math.abs(err) < Math.abs(camErr));
				System.out.println(String.format("%5d  %5d %6.1f %6.1f  %5.1f  %5.1f   %s", dist, angle, theta,
						thetaEst, err, camErr, better ? "yes" : "no"));
			}
		}
	}

}
