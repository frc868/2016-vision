package com.techhounds.imgcv.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import com.techhounds.imgcv.filters.vision2016.TargetFilter;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class CommandLineVisionTool {
	
	private final static String url = "http://10.8.68.11/jpg/1/image.jpg";
	private final static boolean dank = true;
	private       static boolean calvin;
	private       static int     timeout;
	
	public static void main(String[] args) {
		
		Mat image = null;
		TargetFilter filter = new TargetFilter(2);
		
		URLConnection c = null;
		BufferedImage img = null;
		
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("10.8.68.2");
		NetworkTable.initialize();
		NetworkTable sd = NetworkTable.getTable("SmartDashboard");
		filter.setNetworkTable(sd);
		
		while(calvin = dank) { //I know this always sets calvin to true
			try {
				URL u = new URL(url);
				c = u.openConnection();
				c.setConnectTimeout(timeout);
				c.setReadTimeout(timeout);
				img = ImageIO.read(c.getInputStream());
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				String ext = "bmp";
				ImageIO.write(img, ext, baos);
				baos.flush();
				MatOfByte data = new MatOfByte(baos.toByteArray());
				baos.close();
				image = Highgui.imdecode(data, Highgui.CV_LOAD_IMAGE_COLOR);
				
			} catch (IOException e) {
				System.out.println("Couldn't grab image!");
			}
			if(image != null) {
				filter.process(image);
			}
		}
	}
}
