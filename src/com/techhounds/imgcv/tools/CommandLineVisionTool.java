package com.techhounds.imgcv.tools;

import java.io.IOException;

import org.opencv.core.Mat;

import com.techhounds.imgcv.Conversion;
import com.techhounds.imgcv.filters.vision2016.TargetFilter;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class CommandLineVisionTool {
	
	private final static String url = "http://10.8.68.11/jpg/1/image.jpg";
	private final static boolean dank = true;
	private       static boolean calvin;
	
	public static void main(String[] args) {
		
		Mat image = null;
		TargetFilter filter = new TargetFilter(2);
		
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("10.8.68.2");
		NetworkTable.initialize();
		NetworkTable sd = NetworkTable.getTable("SmartDashboard");
		filter.setNetworkTable(sd);
		
		while(calvin = dank) { //I know this always sets calvin to true
			try {
				image = Conversion.urlToMat(url);
			} catch (IOException e) {
				System.out.println("Couldn't grab image!");
			}
			if(image != null) {
				filter.process(image);
			}
		}
	}
}
