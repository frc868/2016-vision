package com.techhounds.imgcv.tools;

import com.techhounds.imgcv.Configuration;
import com.techhounds.imgcv.FrameGrabber;
import com.techhounds.imgcv.filters.vision2016.TargetFilter;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class CommandLineVisionTool {
	
	private static long previousFrame = 0;
	private static long currentFrame = 0;
	
	public static void main(String[] args) {
		Configuration config = new Configuration();
		config.loadOpenCvLibrary();
		
		TargetFilter filter = new TargetFilter(2);	
		FrameGrabber frameGrabber = new FrameGrabber();
		
		
		//frameGrabber.start("http://10.8.68.1/mjpg/video.mjpg");
		frameGrabber.start(0, 640, 480);
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("10.8.68.2");
		NetworkTable.initialize();
		NetworkTable sd = NetworkTable.getTable("SmartDashboard");
		filter.setNetworkTable(sd);
		
		while(true) {
			
			currentFrame = frameGrabber.getFrameCount();
			
			if(currentFrame != previousFrame) {
				filter.process(frameGrabber.getLastImage());
				previousFrame = currentFrame;
				System.out.println(currentFrame);
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				
			}
		}
	}
}
