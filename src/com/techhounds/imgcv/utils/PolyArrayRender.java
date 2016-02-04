package com.techhounds.imgcv.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Scalar;
import org.opencv.core.Mat;

import com.techhounds.imgcv.PolygonCv;

public class PolyArrayRender extends PolygonRender {
	
	List<PolygonCv> polygons = new ArrayList<>();
	
	public PolyArrayRender(Scalar colors, int thickness) {
		super(colors, thickness);
	}
	
	public void setPolygon(List<PolygonCv> polygons) {
		this.polygons = polygons;
	}
	
	public Mat process(Mat srcImage) {
		for(int i = 0; i < polygons.size(); i ++) {
			super.setPolygon(polygons.get(i));
			super.process(srcImage);
		}
		return srcImage;
	}
}
