package com.techhounds.imgcv.utils;

import org.opencv.core.Scalar;

public interface ScalarColors {

	public static double[] red   = {100, 100, 255};
	public static double[] green = {100, 255, 100};
	public static double[] blue  = {255, 100, 100};
	public static double[] white = {255, 255, 255};
	public static double[] black = {000, 000, 000};
	
	public static Scalar RED   = new Scalar(red);
	public static Scalar GREEN = new Scalar(green);
	public static Scalar BLUE  = new Scalar(blue);
	public static Scalar WHITE = new Scalar(white);
	public static Scalar BLACK = new Scalar(black);
}
