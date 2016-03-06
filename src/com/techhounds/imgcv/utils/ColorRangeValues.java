package com.techhounds.imgcv.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.opencv.core.Scalar;

/**
 * A class used to manage a set of color range values.
 */
public final class ColorRangeValues {
	// Key names used to store and retrieve values
	private static final String SIZE_KEY = "ColorRangeSize";
	private static final String MIN_KEY = "ColorRangeMin";
	private static final String MAX_KEY = "ColorRangeMax";
	private static final String KEEP_KEY = "ColorRangeKeep";

	/** Minimum values for each channel. */
	private int[] minVals;

	/** Maximum values for each channel. */
	private int[] maxVals;

	/** Whether we want the values inside or outside each range. */
	private boolean[] keepInRange;

	/**
	 * Constructs a new instance of a 3 channel color range where we keep
	 * everything.
	 */
	public ColorRangeValues() {
		this(3);
	}

	/**
	 * Constructs a new instance for a particular number of channels (initially
	 * accepting everything)
	 * 
	 * @param nchannels
	 *            How many channels.
	 */
	public ColorRangeValues(int nchannels) {
		initialize(nchannels);
	}

	/**
	 * Constructs a new instance that has the same values as another color range
	 * object.
	 * 
	 * @param crv
	 *            The other color range object to get values from (must not be
	 *            null).
	 */
	public ColorRangeValues(ColorRangeValues crv) {
		copyFrom(crv);
	}

	/**
	 * Determine if a color for a particular channel should be kept by the
	 * filter settings or not.
	 * 
	 * @param channel
	 *            The channel the color value is from.
	 * @param val
	 *            The color value found.
	 * @return true If the color value should be accepted, false if not.
	 */
	public boolean inRange(int channel, int val) {
		boolean inColorRange = (minVals[channel] <= val) && (val <= maxVals[channel]);
		return inColorRange == keepInRange[channel];
	}

	/**
	 * Determine if all colors are acceptable by all of the color range filter
	 * settings.
	 * 
	 * @param colorVals
	 *            An array of 0 or more color values to check. NOTE: If the
	 *            number of colors in your array does not match {@link #size},
	 *            we will only check the smaller number of channels (minimum of
	 *            your color array length).
	 * @return true If all of the colorVals passed were acceptable (none
	 *         rejected by filter settings).
	 */
	public boolean inRange(int[] colorVals) {
		int n = Math.min(colorVals.length, size());
		for (int i = 0; i < n; i++) {
			if (!inRange(i, colorVals[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the number of color ranges we handle.
	 * 
	 * @return How many color channels this range tool can handle.
	 */
	public int size() {
		return minVals.length;
	}

	/**
	 * Returns whether or not we keep values inside or outside of the range
	 * associated with a specific channel.
	 * 
	 * @param channel
	 *            The channel you are interested in. Range of: [0,
	 *            {@link #size() - 1].
	 * 
	 * @return true if keeping values inside the range, false if keeping values
	 *         outside the range.
	 */
	public boolean getKeepInRange(int channel) {
		return keepInRange[channel];
	}

	/**
	 * Sets whether or not we keep values inside or outside of the range
	 * associated with a specific channel.
	 * 
	 * @param channel
	 *            The channel you are interested in. Range of: [0,
	 *            {@link #size() - 1].
	 * 
	 * @param keep
	 *            Pass true if keeping values inside the range, false if keeping
	 *            values outside the range.
	 */
	public void setKeepInRange(int channel, boolean keep) {
		keepInRange[channel] = keep;
	}

	/**
	 * Returns the minimum value of the range for a particular channel.
	 * 
	 * @param channel
	 *            The channel you are interested in. Range of: [0,
	 *            {@link #size() - 1].
	 * @return The lower bounds of the range for the channel.
	 */
	public int getMin(int channel) {
		return minVals[channel];
	}

	/**
	 * Returns a copy of the lower limits for all channels.
	 * 
	 * @return Array or minimum values used in color range checks.
	 */
	public int[] getMin() {
		return Arrays.copyOf(minVals, minVals.length);
	}

	/**
	 * Sets the minimum value of the range for a particular channel.
	 * 
	 * @param channel
	 *            The channel you are interested in. Range of: [0,
	 *            {@link #size() - 1].
	 * @param minVal
	 *            The lower bounds of the range for the channel.
	 */
	public void setMin(int channel, int minVal) {
		minVals[channel] = minVal;
	}

	/**
	 * Returns the maximum value of the range for a particular channel.
	 * 
	 * @param channel
	 *            The channel you are interested in. Range of: [0,
	 *            {@link #size() - 1].
	 * @return The upper bounds of the range for the channel.
	 */
	public int getMax(int channel) {
		return maxVals[channel];
	}

	/**
	 * Returns a copy of the upper limits for all channels.
	 * 
	 * @return Array or maximum values used in color range checks.
	 */
	public int[] getMax() {
		return Arrays.copyOf(maxVals, maxVals.length);
	}

	/**
	 * Sets the maximum value of the range for a particular channel.
	 * 
	 * @param channel
	 *            The channel you are interested in. Range of: [0,
	 *            {@link #size() - 1].
	 * @param maxVal
	 *            The upper bounds of the range for the channel.
	 */
	public void setMax(int channel, int maxVal) {
		maxVals[channel] = maxVal;
	}

	/**
	 * Determine if we are using "in range" on all channels (no channels set to
	 * "out of range").
	 * 
	 * @return true If all channels have the {@link #getKeepInRange(int)}
	 *         attribute set to true.
	 */
	public boolean getKeepInRangeAll() {
		int n = size();
		for (int i = 0; i < n; i++) {
			if (!keepInRange[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get an OpenCV Scalar of the minimum values for each channel.
	 * 
	 * @return OpenCV Scalar with lower limits.
	 */
	public Scalar getMinScalar() {
		return getScalar(minVals);
	}

	/**
	 * Get an OpenCV Scalar of the maximum values for each channel.
	 * 
	 * @return OpenCV Scalar with upper limits.
	 */
	public Scalar getMaxScalar() {
		return getScalar(maxVals);
	}

	/**
	 * Helper method to convert integer array into OpenCV Scalar.
	 * 
	 * @param vals
	 *            Array of integer values to convert.
	 * @return An OpenCV Scalar containing values from original array.
	 */
	private Scalar getScalar(int[] vals) {
		int n = size();
		double[] dvals = new double[n];
		for (int i = 0; i < n; i++) {
			dvals[i] = vals[i];
		}
		return new Scalar(dvals);
	}

	/**
	 * Copies the color range information from another color range object into
	 * this one.
	 * 
	 * @param crv
	 *            The other color range object to get values from (must not be
	 *            null).
	 */
	public void copyFrom(ColorRangeValues crv) {
		int n = crv.minVals.length;
		minVals = Arrays.copyOf(crv.minVals, n);
		maxVals = Arrays.copyOf(crv.maxVals, n);
		keepInRange = Arrays.copyOf(crv.keepInRange, n);
	}

	/**
	 * Creates a standard {@link Properties} object containing current values
	 * stored in the object.
	 * 
	 * @return A set of properties that you can store and the load later via
	 *         {@link #setProperties(Properties)}.
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		int n = size();
		props.setProperty(SIZE_KEY, Integer.toString(n));
		for (int i = 0; i < n; i++) {
			props.setProperty(KEEP_KEY + i, Boolean.toString(getKeepInRange(i)));
			props.setProperty(MIN_KEY + i, Integer.toString(getMin(i)));
			props.setProperty(MAX_KEY + i, Integer.toString(getMax(i)));
		}
		return props;
	}

	/**
	 * Loads values from property settings previously stored via
	 * {@link #getProperties()}.
	 * 
	 * @param props
	 *            The properties object containing the values to load (typically
	 *            created from a prior {@link #getProperties()} call).
	 * @throws IllegalArgumentException
	 *             If the properties were missing one or more expected settings.
	 * @throws NumberFormatException
	 *             If the propertive values are corrupt (failed to parse numeric
	 *             value).
	 */
	public void setProperties(Properties props) throws IllegalArgumentException, NumberFormatException {
		int n = Integer.parseInt(props.getProperty(SIZE_KEY, "3"));
		initialize(n);
		for (int i = 0; i < n; i++) {
			String keep = props.getProperty(KEEP_KEY + i, "true");
			String minVal = props.getProperty(MIN_KEY + i, "0");
			String maxVal = props.getProperty(MAX_KEY + i, "255");

			setKeepInRange(i, Boolean.parseBoolean(keep));
			setMin(i, Integer.parseInt(minVal));
			setMax(i, Integer.parseInt(maxVal));
		}
	}

	/**
	 * Returns the "default directory" location (where applications should
	 * probably look for and store settings under).
	 * 
	 * @return A file pointing at the default directory for color range
	 *         configurations (we will try to create any missing directories).
	 */
	public static File getDefaultDir() {
		File defaultDir = new File(System.getProperty("user.home"), ".etc");
		defaultDir = new File(defaultDir, "color-range");
		return defaultDir;
	}

	/**
	 * Returns the "default file" location (what applications might try to load
	 * at start up).
	 * 
	 * @return A file pointing at the default file location (we will try to
	 *         create any missing directories).
	 */
	public static File getDefaultFile() {
		return getFile("default");
	}

	/**
	 * Converts a simple name (like "pink") into the fully qualified path where
	 * we expect the config to be stored.
	 * 
	 * @param name
	 *            Base name of the file name of the configuration file you are
	 *            interested in.
	 * @return A file object with the fully qualified path and extension to
	 *         store the file under.
	 */
	public static File getFile(String name) {
		File defaultDir = getDefaultDir();
		return new File(defaultDir, name + ".color-range.xml");
	}

	/**
	 * Saves the settings to a file.
	 * 
	 * @param f
	 *            The file to save the settings to (must not be null).
	 * @throws FileNotFoundException
	 *             If unable to write to file.
	 * @throws IOException
	 *             If write failed.
	 */
	public void saveSettings(File f) throws FileNotFoundException, IOException {
		File pdir = f.getParentFile();
		if (!pdir.isDirectory() && !pdir.mkdirs()) {
			throw new IOException("Directory " + pdir + " does not exist and we were unable to create it");
		}
		try (FileOutputStream out = new FileOutputStream(f)) {
			Properties props = getProperties();
			props.storeToXML(out, "Color range settings");
			out.close();
		}
	}

	/**
	 * Loads settings from contents of file (typically from previous
	 * {@link #saveSettings(File)} invocation).
	 * 
	 * @param f
	 *            File to load the settings from.
	 * @throws FileNotFoundException
	 *             If file doesn't exist.
	 * @throws IOException
	 *             If problem reading from file.
	 * @throws IllegalArgumentException
	 *             If failed to find a valid configuration file.
	 */
	public void loadSettings(File f) throws FileNotFoundException, IOException, IllegalArgumentException {
		try (FileInputStream in = new FileInputStream(f)) {
			Properties props = new Properties();
			props.loadFromXML(in);
			in.close();
			setProperties(props);
		}
	}

	/**
	 * Initializes (resets) the color range values for a specific number of
	 * channels (keeping everything in the range of 0-255 on each channel).
	 * 
	 * @param n
	 *            The number of channels to initialize the color range for.
	 */
	private void initialize(int n) {
		minVals = new int[n];
		maxVals = new int[n];
		keepInRange = new boolean[n];

		for (int i = 0; i < n; i++) {
			minVals[i] = 0;
			maxVals[i] = 255;
			keepInRange[i] = true;
		}
	}
}
