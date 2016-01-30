package com.techhounds.imgcv.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.techhounds.imgcv.PolygonCv;

public abstract class PolygonRender {

    public static Mat drawTargets(Mat inputImage, List<PolygonCv> targetList, 
    							  double[] colors, int thickness) {
    	
    	List<MatOfPoint> contours = new ArrayList<>();
    	PolygonCv        currentTarget; 
    	Scalar           colorOverlay = new Scalar(colors);
    	
    	for(int i = 0; i < targetList.size(); i++) {
    		currentTarget = targetList.get(i);
    		
    		contours.add(currentTarget.toContour());
    		currentTarget.drawInfo(inputImage, colorOverlay); 
    	} 
    	
    	Imgproc.drawContours(inputImage, contours, -1, colorOverlay, thickness); 
    	
    	return inputImage; //again only for convenience, such as when clone is passed as argument
    }
    
    public static Mat drawTarget(Mat inputImage, PolygonCv bestTarget, 
    						     double[] colors, int thickness) {
    	
    	List<MatOfPoint> bestContours = new ArrayList<>();
    	Scalar           colorOverlay = new Scalar(colors);
    	
    	bestContours.add(bestTarget.toContour());
    	Imgproc.drawContours(inputImage, bestContours, -1, colorOverlay, thickness);

    	return inputImage;
    }
    
    public static Mat drawPoint(Mat srcImage, double x, double y, 
    							double height, double width, double[] colors) { 
    	
    	Point point1 = new Point(x + width, y + height);
    	Point point2 = new Point(x - width, y - height);
    	
    	Core.rectangle(srcImage, point1, point2, new Scalar(colors), -1);
    	
    	return srcImage;
    }

}
