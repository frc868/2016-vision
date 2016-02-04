package com.techhounds.imgcv;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 * Helper class that runs a background thread to fetch images as quickly as
 * possible from the video capture device.
 * 
 * @author Paul Blankenbaker
 */
public class FrameGrabber {

	/** Background thread that reads in images from capture device. */
	private CaptureThread _CaptureThread;

	/**
	 * Construct a new instance in a a disconnected state (not running until you
	 * "start").
	 */
	public FrameGrabber() {
		_CaptureThread = null;
	}

	/**
	 * @return A string representation of the state of the object.
	 */
	@Override
	public String toString() {
		CaptureThread ct = _CaptureThread;
		if (ct == null) {
			return "No Connection";
		}
		return ct.toString();
	}

	/**
	 * Returns a copy of the last image retrieved (if available).
	 * 
	 * @return A cloned copy of the last image retrieved (or null if no image
	 *         retrieved yet since the last start).
	 */
	public Mat getLastImage() {
		CaptureThread ct = _CaptureThread;
		Mat img = null;
		if (ct != null) {
			synchronized (ct) {
				if (ct._LastImage != null) {
					img = ct._LastImage.clone();
				}
			}
		}
		return img;
	}

	/**
	 * The total number of frames retrieved from the video capture device since
	 * it was last started.
	 * 
	 * @return Frame count since start.
	 */
	public long getFrameCount() {
		CaptureThread ct = _CaptureThread;
		return (ct == null) ? null : ct._FrameCount;
	}
	
	/**
	 * The frame rate retrieved from the source computed since started.
	 * 
	 * @return The frames per second we are getting from the source.
	 */
	public int getFps() {
		CaptureThread ct = _CaptureThread;
		if (ct == null) {
			return 0;
		}
		return ct.getFps();
	}

	/**
	 * Helper method to open a VideoCapture device.
	 * 
	 * @param url
	 *            A URL of a IP camera device (or null if you want to open a
	 *            local web cam).
	 * @param devId
	 *            When URL is null, this must be the ID of a local video device
	 *            (web cam).
	 * @param width
	 *            Pass non-zero value if you want us to try and set the video
	 *            width (not used if URL is non-null).
	 * @param height
	 *            Pass non-zero value if you want us to try and set the video
	 *            height (not used if URL is non-null).
	 * @return A VideoCapture device.
	 */
	public static VideoCapture open(String url, int devId, int width, int height) {
		VideoCapture vc = new VideoCapture();
		if (url != null) {
			vc.open(url);

			if (vc.isOpened()) {
				System.err.println("Starting IP camera feed from: " + url);
			} else {
				System.err.println("Failed to start IP camera feed from: "
						+ url);
				vc = null;
			}
		} else {
			vc.open(devId);
			if (vc.isOpened()) {
				if (width > 0 && height > 0) {
					vc.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, width);
					vc.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, height);
				}

				System.err.println("Starting Video Feed using Web Cam: "
						+ devId);
			} else {
				System.err.println("Failed to start Video Feed using Web Cam: "
						+ devId);
				vc = null;
			}
		}
		return vc;
	}

	/**
	 * Start a video feed using built-in camera or USB web cam.
	 * 
	 * @param devId
	 *            The ID of a local video device (web cam). Typically 0 for
	 *            first web cam.
	 * @param width
	 *            Pass non-zero value if you want us to try and set the video
	 *            width.
	 * @param height
	 *            Pass non-zero value if you want us to try and set the video
	 *            height.
	 */
	public void start(int devId, int width, int height) {
		stop();
		_CaptureThread = new CaptureThread(devId, null, width, height);
		_CaptureThread.start();
	}

	/**
	 * Start a video feed using an IP based camera.
	 * 
	 * @param URL
	 *            The URL to connect to for the video feed.
	 */
	public void start(String url) {
		stop();
		_CaptureThread = new CaptureThread(-1, url, 0, 0);
		_CaptureThread.start();
	}

	/**
	 * Stop the video feed connection.
	 */
	public void stop() {
		CaptureThread t = _CaptureThread;
		if (t != null) {
			// Set flag to let thread shut down nicely
			t._Continue = false;
			/**
			 * This is a more drastic way to shut it down more quickly if
			 * (t.isAlive() && !t.isInterrupted()) { try { t.wait(100); if
			 * (t.isAlive()) { return; } } catch (InterruptedException e) {
			 * System.err.println("Capture thread failed to shutdown cleanly");
			 * } t.interrupt(); try { t.wait(100); } catch (InterruptedException
			 * e) {
			 * System.err.println("Capture thread failed to shutdown cleanly");
			 * } }
			 */
		}
	}

	/**
	 * Indicates whether we think we are still trying to capture images.
	 * 
	 * @return true If thread is running trying to grab new images.
	 */
	public boolean isRunning() {
		Thread t = _CaptureThread;
		return (t != null) && t.isAlive();
	}

	/**
	 * Helper class that does the work of connecting to the web cam and grabbing
	 * images in a background thread.
	 * 
	 * @author pkb
	 */
	private class CaptureThread extends Thread {
		private static final long MIN_FRAMES_FOR_FPS = 150;
		private int _DevId;
		private String _Url;
		private int _Width;
		private int _Height;
		private Mat _LastImage;
		private long _FrameCount;
		private boolean _Continue;
		private long _FirstFrameTime;
		private long _LastFrameTime;

		CaptureThread(int devId, String url, int width, int height) {
			_DevId = devId;
			_Url = url;
			_Width = width;
			_Height = height;
			_FrameCount = 0;
			_LastImage = null;
			_Continue = true;
		}

		/**
		 * Returns the average FPS after we have at least two frames.
		 * 
		 * @return Frames Per Second (FPS).
		 */
		public int getFps() {
			int fps = 0;
			synchronized (this) {
				long dur = _LastFrameTime - _FirstFrameTime;
				if ((dur > 0) && (_FrameCount >= MIN_FRAMES_FOR_FPS)) {
					fps = (int) ((_FrameCount - MIN_FRAMES_FOR_FPS + 1) * 1000 / dur);
				}
			}
			return fps;
		}

		@Override
		public void run() {
			VideoCapture vc = open(_Url, _DevId, _Width, _Height);
			if (vc == null) {
				return;
			}

			while (!isInterrupted() && _Continue) {
				if (vc.grab()) {
					Mat img = new Mat();
					synchronized (this) {
						if (vc.retrieve(img)) {
							_LastImage = img;
							_LastFrameTime = System.currentTimeMillis();
							if (_FrameCount == MIN_FRAMES_FOR_FPS) {
								_FirstFrameTime = _LastFrameTime;
							}
							_FrameCount++;
						}
					}
					/*
					if (_FrameCount % 100 == 0) {
						System.err.println("FPS from camera: " + getFps());
					}
					*/
				}
			}
			System.err.println("Video Capture thread is stopping");
			_FrameCount = 0;
			_LastFrameTime = _FirstFrameTime = 0;
			vc.release();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Frame: ");
			sb.append(_FrameCount);

			Mat img = _LastImage;
			if (img != null) {
				sb.append(" (");
				sb.append(img.rows());
				sb.append(", ");
				sb.append(img.cols());
				sb.append(", ");
				sb.append(img.depth());
				sb.append(")");
			}

			if (_DevId != -1) {
				sb.append("  WebCam(");
				sb.append(_DevId);
				sb.append(')');
			}

			if (_Url != null) {
				sb.append("  URL(");
				sb.append(_Url);
				sb.append(')');
			}

			return sb.toString();
		}
	}
}
