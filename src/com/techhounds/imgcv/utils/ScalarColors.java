package com.techhounds.imgcv.utils;

import org.opencv.core.Scalar;

/**
 * Some static color definitions for quick and dirty testing (when you want to mark up an image but don't care too much about color).
 */
public interface ScalarColors {

	public static final double[] red   = {100, 100, 255};
	public static final double[] green = {100, 255, 100};
	public static final double[] blue  = {255, 100, 100};
	public static final double[] white = {255, 255, 255};
	public static final double[] black = {000, 000, 000};
	public static final double[] yellow = {0x0e, 0xfb, 0xf9};
	public static final double[] orange = { 0x4c, 0xb9, 0xec };
	public static final double[] magenta = { 188, 88, 182 };
	public static final double[] cyan = { 224, 216, 105 };
	public static final double[] purple = { 214, 100, 166 };
	
	public static final Scalar RED   = new Scalar(red);
	public static final Scalar GREEN = new Scalar(green);
	public static final Scalar BLUE  = new Scalar(blue);
	public static final Scalar WHITE = new Scalar(white);
	public static final Scalar BLACK = new Scalar(black);
	public static final Scalar YELLOW = new Scalar(yellow);
	public static final Scalar ORANGE = new Scalar(orange);
	public static final Scalar MAGENTA = new Scalar(magenta);
	public static final Scalar CYAN = new Scalar(cyan);
	public static final Scalar PURPLE = new Scalar(purple);
}
