/*
 * Copyright (c) 2014, pkb
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.techhounds.imgcv;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.opencv.core.Core;

/**
 * Helps deal with persisting configurable settings between run sessions and
 * OpenCV set up.
 * 
 * @author pkb
 */
public class Configuration {

	/** Used to store/retrieve configuration settings. */
	private Preferences _Preferences;

	/**
	 * Key used to look up path for native OpenCV libraries.
	 */
	private static final String JAVA_LIBRARY_PATH = "java.library.path";

	/**
	 * Boolean indicating if we should get video feeds from local web cam or
	 * remote URL.
	 */
	private static final String USE_VIDEO_URL = "UseVideoUrl";

	/**
	 * Integer value (typically 0) indicating which local video device to use.
	 */
	private static final String VIDEO_DEVICE = "VideoDevice";

	/**
	 * The desired width of the web cam video (or 0 to leave untouched)
	 */
	private static final String VIDEO_FRAME_WIDTH = "VideoFrameWidth";

	/**
	 * The desired height of the web cam video (or 0 to leave untouched)
	 */
	private static final String VIDEO_FRAME_HEIGHT = "VideoFrameHeight";

	/**
	 * Key used to look up name of last image URL (for grabbing images).
	 */
	private static final String IMAGE_SOURCE_URL = "LastUrl";
	
	/**
	 * Key used to look up URL for video streams from IP camera.
	 */
	private static final String VIDEO_SOURCE_URL = "VideoUrl";

	/**
	 * Key used to look up name of last file opened.
	 */
	private static final String LAST_OPENED_IMAGE_FILE = "LastImage";

	/**
	 * Key used to look up name of last file saved.
	 */
	private static final String LAST_SAVED_IMAGE_FILE = "LastSaveImage";

	public Configuration() {
		this(Configuration.class);
	}

	public Configuration(Class<?> c) {
		_Preferences = Preferences.userNodeForPackage(Configuration.class);
	}

	/**
	 * Attempts to load the OpenCV native library required for image processing.
	 *
	 * <p>
	 * The method attempts to dynamically update the system library path and
	 * load the OpenCV native library. NOTE: Setting the java.library.path
	 * dynamically is similar to using -Djava.library.path=${HOME}/opencv/lib on
	 * the command line when starting the Java process.
	 * </p>
	 *
	 * <p>
	 * We make use of a trick found on the Internet
	 * (http://stackoverflow.com/questions
	 * /271506/why-cant-system-setproperty-change-the-classpath-at-runtime)
	 * which allows the new library path to be used.
	 * </p>
	 *
	 * <p>
	 * We will make one attempt at loading the library before we mess around
	 * with the java.library.path setting (in case you have provided it on the
	 * command line).
	 * </p>
	 *
	 * <p>
	 * If the first attempt fails, we will try setting the java.library.path to
	 * ${user.home}/opencv/lib (under the user's home directory).
	 * </p>
	 * 
	 * @return true If able to load OpenCV native library. False if not.
	 */
	public boolean loadOpenCvLibrary() {
		// Try loading without changing anything (in case library path has
		// already been set up).
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			return true;
		} catch (UnsatisfiedLinkError ignore) {

		}

		// First attempt failed, try to dynamically update the system libary
		// path and load the OpenCV native libaries
		// NOTE: Setting the java.library.path dynamically is similar to using
		// -Djava.library.path=${HOME}/opencv/lib on the
		// command line when starting the Java process.
		// Assume OpenCv library is under ${user.home}/opencv/lib unless
		// preference is set
		String libPath = System.getProperty("user.home") + File.separator
				+ "opencv/lib";
		libPath = _Preferences.get(JAVA_LIBRARY_PATH, libPath);
		System.setProperty(JAVA_LIBRARY_PATH, libPath);

		// Trick found on the Internet
		// (http://stackoverflow.com/questions/271506/why-cant-system-setproperty-change-the-classpath-at-runtime)
		// which allows the new library path to be used.
		try {
			Field fieldSysPath = ClassLoader.class
					.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE,
					null, ex);
		}

		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			return true;
		} catch (UnsatisfiedLinkError ignore) {

		}
		return false;
	}

	/**
	 * Get whether the user wants to get video from local web cam or remote URL.
	 * 
	 * @param defVal
	 *            The default value to return if not set.
	 * 
	 * @return true If user wants remote video feed, false if they want to use
	 *         web cam.
	 */

	public boolean isVideoFeedFromUrl(boolean defVal) {
		return _Preferences.getBoolean(USE_VIDEO_URL, defVal);
	}

	/**
	 * Set whether the user wants to get video from local web cam or remote URL.
	 * 
	 * @param useUrl
	 *            Pass true to indicate remote video feed using URL, pass false
	 *            to use local web cam.
	 */
	public void setVideoFeedFromUrl(boolean useUrl) {
		_Preferences.putBoolean(USE_VIDEO_URL, useUrl);
	}

	/**
	 * Get the device ID of the local web cam to use for video feed (typically 0
	 * unless you've plugged in multiple web cams).
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public int getDeviceId(int defVal) {
		return _Preferences.getInt(VIDEO_DEVICE, defVal);
	}

	/**
	 * Set the device ID of the local web cam to use for video capture.
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setDeviceId(int val) {
		_Preferences.putInt(VIDEO_DEVICE, val);
	}

	/**
	 * Get the desired width of the images to grab from the local web cam device
	 * (0 indicates the user does not want to force the size).
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public int getFrameWidth(int defVal) {
		return _Preferences.getInt(VIDEO_FRAME_WIDTH, defVal);
	}

	/**
	 * Set the desired width of the images to grab from the local web cam device
	 * (0 indicates the user does not want to force the size).
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setFrameWidth(int val) {
		_Preferences.putInt(VIDEO_FRAME_WIDTH, val);
	}

	/**
	 * Get the desired height of the images to grab from the local web cam
	 * device (0 indicates the user does not want to force the size).
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public int getFrameHeight(int defVal) {
		return _Preferences.getInt(VIDEO_FRAME_HEIGHT, defVal);
	}

	/**
	 * Set the desired height of the images to grab from the local web cam
	 * device (0 indicates the user does not want to force the size).
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setFrameHeight(int val) {
		_Preferences.putInt(VIDEO_FRAME_HEIGHT, val);
	}

	/**
	 * Get the URL to use for retrieving images via HTTP requests.
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public String getImageUrl(String defVal) {
		return _Preferences.get(IMAGE_SOURCE_URL, defVal);
	}

	/**
	 * Set the URL to use for retrieving images via HTTP requests.
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setImageUrl(String val) {
		_Preferences.put(IMAGE_SOURCE_URL, val);
	}

	/**
	 * Get the URL to use for retrieving video streams from IP cameras.
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public String getVideoUrl(String defVal) {
		return _Preferences.get(VIDEO_SOURCE_URL, defVal);
	}

	/**
	 * Set the URL to use for retrieving video streams from IP cameras.
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setVideoUrl(String val) {
		_Preferences.put(VIDEO_SOURCE_URL, val);
	}

	/**
	 * Get the name of the last image file that was opened by the user.
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public String getLastOpenedFile(String defVal) {
		return _Preferences.get(LAST_OPENED_IMAGE_FILE, defVal);
	}

	/**
	 * Set the name of the last image file that was opened by the user.
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setLastOpenedFile(String val) {
		_Preferences.put(LAST_OPENED_IMAGE_FILE, val);
	}

	/**
	 * Get the name of the last image file that was saved by the user.
	 * 
	 * @param defVal
	 *            Default value to return if not configured yet.
	 * 
	 * @return Either the value stored in the preferences or your default value.
	 */
	public String getLastSavedFile(String defVal) {
		return _Preferences.get(LAST_SAVED_IMAGE_FILE, defVal);
	}

	/**
	 * Set the name of the last image file that was saved by the user.
	 * 
	 * @param val
	 *            The new value to store in the preferences.
	 */
	public void setLastSavedFile(String val) {
		_Preferences.put(LAST_SAVED_IMAGE_FILE, val);
	}

	/**
	 * Get access to the preferences object used to save/load key/value
	 * settings.
	 *
	 * @return A Preferences object you can use to add additional settings to.
	 */
	public Preferences getPreferences() {
		return _Preferences;
	}

}
