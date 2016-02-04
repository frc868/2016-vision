package com.techhounds.imgcv.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.techhounds.imgcv.filters.MatFilter;

public class RectangleRender implements MatFilter {
	
	private double   centerX;
	private double   centerY;
	private double   height;
	private double   width;
	private int      thickness;
	private Scalar   colors;
	
	public RectangleRender(Scalar colors, int thickness) {
		this.colors    = colors;
		this.thickness = thickness;
	}
	
	public void setCenter(double centerX, double centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
	}
	
	public void setSize(double height, double width) {
		this.height = height;
		this.width  = width;
	}
    
    public Mat process(Mat srcImage) { 
    	
    	Point point1 = new Point(centerX + width, centerY + height);
    	Point point2 = new Point(centerX - width, centerY - height);
    	
    	Core.rectangle(srcImage, point1, point2, colors, thickness);
    	
    	return srcImage;
    }

}
