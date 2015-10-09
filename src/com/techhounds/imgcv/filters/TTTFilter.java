package com.techhounds.imgcv.filters;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class TTTFilter implements MatFilter{
	
	private int lRowStart = 10;
	private int lRowEnd = 130;
	private int lColStart = 50;
	private int lColEnd = 280;
	
	private int rRowStart = 150;
	private int rRowEnd = 300;
	private int rColStart = 300;
	private int rColEnd = 530;

	@Override
	public Mat process(Mat srcImage) {
		//System.out.println("Right");
		Scalar left = getColorValue(srcImage, lRowStart, lRowEnd, lColStart, lColEnd);
		drawRect(srcImage, lRowStart, lRowEnd, lColStart, lColEnd, left);
		Scalar right = getColorValue(srcImage, rRowStart, rRowEnd, rColStart, rColEnd);
		drawRect(srcImage, rRowStart, rRowEnd, rColStart, rColEnd, right);
		
		double leftAvg = getAverage(left);
		double rightAvg = getAverage(right);
		double percAvg = getPercAverage(leftAvg, rightAvg);
		
		//If % negative right is brighter, if positive left is brighter
		if(percAvg > 0. ){ 
			System.out.println("Left!");
			System.out.println("Left Average: " + leftAvg + "  Right Average: " + rightAvg + "\nPercent Difference: " + percAvg);
		}
		else if(percAvg < -0.35){
			System.out.println("Right!");
			System.out.println("Left Average: " + leftAvg + "  Right Average: " + rightAvg + "\nPercent Difference: " + percAvg);
		}
		return srcImage;
	}
	
	private Scalar getColorValue(Mat output, int rowStart, int rowEnd, int colStart, int colEnd) {
        Mat piece = output.submat(rowStart, rowEnd, colStart, colEnd);
        return Core.mean(piece);
    }
	
	private void drawRect(Mat output, int rowStart, int rowEnd, int colStart, int colEnd, Scalar rectColor) {
        Point p1 = new Point(colStart, rowStart);
        Point p2 = new Point(colEnd, rowEnd);
        Core.rectangle(output, p1, p2, rectColor, -1);
    }
	
	private double getAverage(Scalar scalar){
		double total = scalar.val[0] + scalar.val[1] + scalar.val[2];
		if(total == 0) return 0.00000001;
		else return total / 3;
	}
	
	private double getPercAverage(double lAvg, double rAvg){
		return (lAvg - rAvg) / lAvg;
	}
}