package com.techhounds.imgcv.widgets;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.techhounds.imgcv.utils.FovCalculator;

/**
 * An overlay tool that allows you to see distances and angles measured from the
 * center of image.
 * 
 * <p>
 * This overlay is intended as an aid in checking your image processing code.
 * This filter draws an overlay of lines on the image (either horizontal,
 * vertical or both). The line can be labeled or spaced as desired.
 * </p>
 * 
 * <p>
 * NOTE: The following assumptions are being made:
 * </p>
 * 
 * <ul>
 * <li>The camera is not tipped forward or backward (facing straight at the
 * wall).</li>
 * <li>You will need to provide the total FOV of your image across the
 * HORIZONTAL (we will derive the vertical).</li>
 * <li>You will need to provide the distance from the camera to the center point
 * of the image on the wall in real-world units (if omitted then horizontal
 * real-world unit measurements will be off).</li>
 * </ul>
 * 
 * @author pkb
 *
 */
public class FovEditor extends JPanel {

	/**
	 * Prevents Swing warnings
	 */
	private static final long serialVersionUID = 1876783869159638691L;

	/** Key used to look up comment setting from properties. */
	private static final String COMMENT_KEY = "FovComment";

	/** Key used to look up FOV setting from properties. */
	private static final String FOV_KEY = "FovKey";

	/** Key used to look up distance setting from properties. */
	private static final String DISTANCE_KEY = "FovDistance";

	/** Key used to look up length (in pixels) setting from properties. */
	private static final String LENGTH_PX_KEY = "FovLengthPx";

	/** Field holding user comment for FOV settings. */
	private JTextField _CommentEditor;

	/** Field holding FOV of camera. */
	private JTextField _FovEditor;

	/** Field holding distance of camera from wall. */
	private JTextField _DistanceEditor;

	/** Field holding length of entire image in pixels. */
	private JTextField _LengthPxEditor;

	/** Calculated real-world length of image. */
	private JTextField _LengthViewer;

	/** Calculated pixel distance from camera to wall. */
	private JTextField _DistancePxViewer;

	/** Last file opened or saved (or null if none yet). */
	private File _LastFile;

	/**
	 * Construct an instance of the editor with some initial default values.
	 */
	public FovEditor() {
		this("", "45.0", "100.0", "640");
	}

	/**
	 * Construct an instance of the editor with initial values.
	 * 
	 * @param comment
	 *            Text comment for user comment field.
	 * @param fov
	 *            Text FOV of camera in degrees.
	 * @param dist
	 *            Text distance from camera to wall in real world unit.
	 * @param lengthPx
	 *            Length of image in pixels (width or height we don't care).
	 */
	public FovEditor(String comment, String fov, String dist, String lengthPx) {
		createComponents();
		layoutComponents();

		setComment(comment);
		setFov(fov);
		setDistance(dist);
		setLengthPx(lengthPx);

		updateComputedValues();
	}

	/**
	 * Sets the length in pixels field to a new value (call
	 * {@link #updateComputedValues()} after done making changes).
	 * 
	 * @param lengthPx
	 *            Length of image covered by FOV in pixels.
	 */
	private void setLengthPx(String lengthPx) {
		_LengthPxEditor.setText(lengthPx);
	}

	/**
	 * Gets the length of the image in pixels that the user has typed into the
	 * editor.
	 * 
	 * @return Lenght of image in pixels the FOV spans.
	 * @throws NumberFormatException
	 *             If user has not entered a valid number.
	 */
	public int getLengthPx() throws NumberFormatException {
		return Integer.parseInt(_LengthPxEditor.getText());
	}

	/**
	 * Sets the distance in real world units from the camera focal point to the
	 * center of the image on the wall (call {@link #updateComputedValues()}
	 * after done making changes).
	 * 
	 * @param dist
	 *            The distance to the wall in real world units (in, m, ft, etc).
	 */
	public void setDistance(String dist) {
		_DistanceEditor.setText(dist);
	}

	/**
	 * Gets the distance in real world units from the camera focal point to the
	 * center of the image on the wall.
	 * 
	 * @param dist
	 *            The distance to the wall in real world units (in, m, ft, etc).
	 * @return The distance to the wall in real world units entered by the user
	 *         (in, m, ft, etc).
	 * @throws NumberFormatException
	 *             If the user entered a "non-number".
	 */
	public double getDistance() throws NumberFormatException {
		return Double.parseDouble(_DistanceEditor.getText());
	}

	/**
	 * Sets the FOV in degrees for the camera (call
	 * {@link #updateComputedValues()} after done making changes).
	 * 
	 * @param fov
	 *            The camera's total FOV in degrees.
	 */
	public void setFov(String fov) {
		_FovEditor.setText(fov);
	}

	/**
	 * Gets the FOV the user has typed into the editor.
	 * 
	 * @return FOV entered by user in degrees.
	 * @throws NumberFormatException
	 *             If user has not entered a valid number.
	 */
	public double getFov() throws NumberFormatException {
		return Double.parseDouble(_FovEditor.getText());
	}

	/**
	 * Creates a {@link FovCalculator} based on the current settings in the
	 * editor.
	 * 
	 * @return A {@link FovCalculator} you can use for making calculations
	 *         related to the image.
	 * 
	 * @throws NumberFormatException
	 *             If the user entered a "non-number".
	 */
	public FovCalculator createFovCalculator() throws NumberFormatException {
		return new FovCalculator(getFov(), getLengthPx(), getDistance());
	}

	/**
	 * Creates a standard {@link Properties} object containing the editor's
	 * current settings.
	 * 
	 * @return A set of properties that you can store and the load later via
	 *         {@link #setProperties(Properties)}.
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.setProperty(COMMENT_KEY, _CommentEditor.getText());
		props.setProperty(FOV_KEY, _FovEditor.getText());
		props.setProperty(DISTANCE_KEY, _DistanceEditor.getText());
		props.setProperty(LENGTH_PX_KEY, _LengthPxEditor.getText());
		return props;
	}

	/**
	 * Loads the editor with values contained in a properties object.
	 * 
	 * @param props
	 *            The properties object containing the values to load (typically
	 *            created from a prior {@link #getProperties()} call).
	 * @throws IllegalArgumentException
	 *             If the properties were missing one or more expected settings.
	 */
	public void setProperties(Properties props) throws IllegalArgumentException {
		String comment = getProperty(props, COMMENT_KEY);
		String fov = getProperty(props, FOV_KEY);
		String distance = getProperty(props, DISTANCE_KEY);
		String lengthPx = getProperty(props, LENGTH_PX_KEY);

		_CommentEditor.setText(comment);
		_FovEditor.setText(fov);
		_DistanceEditor.setText(distance);
		_LengthPxEditor.setText(lengthPx);
		updateComputedValues();
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
			throw new IOException("Directory " + pdir
					+ " does not exist and we were unable to create it");
		}
		try (FileOutputStream out = new FileOutputStream(f)) {
			Properties props = getProperties();
			props.storeToXML(out, "FOV settings");
			out.close();
		}
	}

	/**
	 * Loads editor settings from contents of file (typically from previous
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
	public void loadSettings(File f) throws FileNotFoundException, IOException,
			IllegalArgumentException {
		try (FileInputStream in = new FileInputStream(f)) {
			Properties props = new Properties();
			props.loadFromXML(in);
			in.close();
			setProperties(props);
		}
	}

	/**
	 * Gets the property setting for a specific key.
	 * 
	 * @param props
	 *            The properties to look the setting up from.
	 * @param key
	 *            The key to use to look up the setting.
	 * @return The value associated with the key (never null).
	 * @throws IllegalArgumentException
	 *             If the properties did not contain a value for the key.
	 */
	private String getProperty(Properties props, String key)
			throws IllegalArgumentException {
		String prop = props.getProperty(key);
		if (prop == null) {
			throw new IllegalArgumentException("Properties are missing the \""
					+ key + "\" property");
		}
		return prop;
	}

	public void setComment(String comment) {
		_CommentEditor.setText(comment);
	}

	/**
	 * Updates the computed fields displayed in the editor (things that the user
	 * can't change as they are derived from other fields).
	 */
	public void updateComputedValues() {
		try {
			double fov = Double.parseDouble(_FovEditor.getText());
			int lengthPx = Integer.parseInt(_LengthPxEditor.getText());
			double dist = Double.parseDouble(_DistanceEditor.getText());

			FovCalculator calc = new FovCalculator(fov, lengthPx, dist);
			String length = String.format("%.2f", calc.getLength());
			_LengthViewer.setText(length);

			String distPx = Integer.toString((int) Math.round(calc
					.getDistancePx()));
			_DistancePxViewer.setText(distPx);
		} catch (Exception e) {
			_LengthViewer.setText("");
			_DistancePxViewer.setText("");
		}
	}

	/**
	 * Helper method to layout all of the widgets making up the editor.
	 */
	private void layoutComponents() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		int row = 0;
		int col = 0;
		GridBagConstraints gbc = createGridBagConstraints(row, col++);
		add(new JLabel("Comment"), gbc);

		gbc = createGridBagConstraints(row, col++);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(_CommentEditor, gbc);

		row++;
		col = 0;
		gbc = createGridBagConstraints(row, col++);
		add(new JLabel("FOV"), gbc);
		gbc = createGridBagConstraints(row, col++);
		add(_FovEditor, gbc);
		gbc = createGridBagConstraints(row, col++);
		add(new JLabel("Distance (real)"), gbc);
		gbc = createGridBagConstraints(row, col++);
		add(_DistanceEditor, gbc);
		gbc = createGridBagConstraints(row, col++);
		add(new JLabel("Length (real)"), gbc);
		gbc = createGridBagConstraints(row, col++);
		add(_LengthViewer, gbc);

		row++;
		col = 2;
		gbc = createGridBagConstraints(row, col++);
		add(new JLabel("Distance (px)"), gbc);
		gbc = createGridBagConstraints(row, col++);
		add(_DistancePxViewer, gbc);
		gbc = createGridBagConstraints(row, col++);
		add(new JLabel("Length (px)"), gbc);
		gbc = createGridBagConstraints(row, col++);
		add(_LengthPxEditor, gbc);

		JPanel buttons = new JPanel();
		new BoxLayout(buttons, BoxLayout.X_AXIS);
		row++;
		col = 0;
		gbc = createGridBagConstraints(row, col++);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.CENTER;
		add(buttons, gbc);

		final JButton openButton = new JButton("Open");
		final JButton saveAsButton = new JButton("Save As");
		final JButton saveButton = new JButton("Save");
		saveButton.setEnabled(_LastFile != null);
		final JButton saveAsDefaults = new JButton("Save Defaults");
		final JButton loadDefaults = new JButton("Open Defaults");

		buttons.add(openButton);
		buttons.add(saveButton);
		buttons.add(saveAsButton);
		buttons.add(saveAsDefaults);
		buttons.add(loadDefaults);

		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				open();
				saveButton.setEnabled(_LastFile != null);
			}
		});

		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				saveAs();
				saveButton.setEnabled(_LastFile != null);
			}
		});

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (_LastFile == null) {
					setEnabled(false);
				} else {
					save();
				}
			}
		});

		saveAsDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_LastFile = getDefaultFile();
				save();
			}
		});

		loadDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open(getDefaultFile());
			}
		});

	}

	/**
	 * Saves the current settings in the editor to the last file location.
	 * 
	 * @return true if file was saved, false if failure or last file location
	 *         has not been set yet (NOTE: Displays error message if save
	 *         fails).
	 */
	public boolean save() {
		if (_LastFile != null) {
			try {
				saveSettings(_LastFile);
				return true;
			} catch (Exception e) {
				String msg = "We were unable to save FOV settings to "
						+ _LastFile + " (" + e.getLocalizedMessage() + ")";
				String title = "FOV Settings Save Failure";
				showMessageDialog(null, msg, title, ERROR_MESSAGE);
				_LastFile = null;
			}
		}
		return false;
	}

	/**
	 * Displays a file chooser to allow user to enter a file name to save the
	 * settings to.
	 * 
	 * @return true if file was saved, false if failure or user canceled the
	 *         action.
	 */
	public boolean saveAs() {
		JFileChooser chooser = createChooser();
		if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			_LastFile = chooser.getSelectedFile();
			return save();
		}
		return false;
	}

	/**
	 * Helper method used to create a consistent file chooser for open/save
	 * operations.
	 * 
	 * @return A file chooser to allow user to select a file from.
	 */
	private JFileChooser createChooser() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String[] exts = { "xml" };
		FileFilter filter = new FileNameExtensionFilter("FOV Configuration", exts);
		chooser.setFileFilter(filter);
		chooser.addChoosableFileFilter(filter);
		if (_LastFile != null) {
			chooser.setSelectedFile(_LastFile);
		} else {
			chooser.setSelectedFile(getDefaultFile());
		}
		return chooser;
	}

	/**
	 * Returns the "default file" location (what applications might try to load
	 * at start up).
	 * 
	 * @return A file pointing at the default file location (we will try to
	 *         create any missing directories).
	 */
	public File getDefaultFile() {
		File defaultDir = new File(System.getProperty("user.home"), ".etc");
		defaultDir = new File(defaultDir, "fov");
		if (!defaultDir.isDirectory()) {
			defaultDir.mkdirs();
		}
		return new File(defaultDir, "default.fov.xml");
	}

	/**
	 * Displays a file chooser to allow user to select a file to load settings
	 * from.
	 * 
	 * @return true if file was selected and we were able to load settings,
	 *         false if not.
	 */
	public boolean open() {
		JFileChooser chooser = createChooser();
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return open(chooser.getSelectedFile());
		}
		return false;
	}

	/**
	 * Loads settings from a specific file (displays error message if failure).
	 * 
	 * @param f
	 *            File to load settings from.
	 * @return true if file was loaded false if not.
	 */
	public boolean open(File f) {
		try {
			loadSettings(f);
			_LastFile = f;
			return true;
		} catch (Exception e) {
			String msg = "We were unable to load FOV settings from " + f + " ("
					+ e.getLocalizedMessage() + ")";
			String title = "FOV Settings Load Failure";
			showMessageDialog(null, msg, title, ERROR_MESSAGE);
		}
		return false;
	}

	/**
	 * Helper method used when laying out components.
	 * 
	 * @param row
	 *            Row in the editor the widget should appear at.
	 * @param col
	 *            Column in the editor the widget should appear at.
	 * @return Constraints object used to place the widget in a JPanel with a
	 *         {@link GridBagLayout}.
	 */
	private GridBagConstraints createGridBagConstraints(int row, int col) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = col;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}

	/**
	 * Helper method to construct all the individual widgets on the panel.
	 */
	private void createComponents() {
		_CommentEditor = new JTextField();

		FocusListener fl = new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				updateComputedValues();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		};

		_FovEditor = createEditor("45.0",
				"Enter the camera's total field of view", fl);
		_DistanceEditor = createEditor("100.0",
				"Enter the distance from camera to wall (real world units)", fl);
		_LengthPxEditor = createEditor("640",
				"The number of pixels the camera has across the FOV", fl);

		_LengthViewer = createViewer("The visible length spanned by the pixels (real world units)");
		_DistancePxViewer = createViewer("The distance from camera to wall (in pixels)");
	}

	/**
	 * Helper method to construct a "viewer" widget that displays a calculated
	 * value.
	 * 
	 * @param toolTip
	 *            Tool tip to associate with the widget.
	 * @return A widget that can be added to the editor.
	 */
	private JTextField createViewer(String toolTip) {
		JTextField viewer = new JTextField(5);
		viewer.setToolTipText(toolTip);
		viewer.setEditable(false);
		viewer.setHorizontalAlignment(JTextField.RIGHT);
		return viewer;
	}

	/**
	 * Constructs a text field widget the user can modify.
	 * 
	 * @param text
	 *            Initial text value to display.
	 * @param toolTip
	 *            Tool tip to show when user hovers over.
	 * @param fl
	 *            Handler to invoke when user moves focus away from widget (or
	 *            null if none).
	 * @return A widget that can be added to the editor.
	 */
	private JTextField createEditor(String text, String toolTip,
			FocusListener fl) {
		JTextField editor = new JTextField(text, 5);
		editor.setToolTipText(toolTip);
		if (fl != null) {
			editor.addFocusListener(fl);
		}
		editor.setHorizontalAlignment(JTextField.RIGHT);
		return editor;
	}

	/**
	 * Test application to verify the editor works (creates pop-up frame showing
	 * the editor).
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("FOV Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new FovEditor());
		frame.setVisible(true);
		frame.pack();
	}

}
