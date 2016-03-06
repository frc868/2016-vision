package com.techhounds.imgcv.widgets;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.techhounds.imgcv.utils.ColorRangeValues;

/**
 * A GUI widget that allows you to edit and save color range values (
 * {@link ColorRangeValues}).
 */
public class ColorRangeEditor extends JPanel {

	/**
	 * Prevents Swing warnings
	 */
	private static final long serialVersionUID = -1299067685590688564L;

	/** Last file opened or saved (or null if none yet). */
	private File lastFile;

	/**
	 * GUI widgets used to select whether you want in or out of range.
	 */
	private JCheckBox[] keepCheckboxes;

	/**
	 * GUI widgets used to adjust min values.
	 */
	private JSlider[] minSliders;

	/**
	 * GUI widgets used to adjust max values.
	 */
	private JSlider[] maxSliders;

	/**
	 * List of listeners to notify if values are changed in GUI editor.
	 */
	private final ArrayList<ChangeListener> listeners;

	/**
	 * The name of the default configuration for this editor.
	 */
	private String defCfgName;

	/**
	 * Construct an instance of the editor with default values.
	 */
	public ColorRangeEditor() {
		this(new ColorRangeValues());
	}

	/**
	 * Construct an instance of the editor with initial values.
	 * 
	 * @param crv
	 *            Color range values to initialize the editor with.
	 */
	public ColorRangeEditor(ColorRangeValues cvr) {
		listeners = new ArrayList<>();
		keepCheckboxes = new JCheckBox[0];
		minSliders = new JSlider[0];
		maxSliders = new JSlider[0];
		setValues(cvr);
		defCfgName = "default";
	}

	/**
	 * Sets the values shown in the editor.
	 * 
	 * @param cvr
	 *            The values to set (must not be null).
	 */
	public void setValues(ColorRangeValues cvr) {
		int n = cvr.size();
		if (n != keepCheckboxes.length) {
			createComponents(n);
		}
		for (int i = 0; i < n; i++) {
			keepCheckboxes[i].setSelected(cvr.getKeepInRange(i));
			minSliders[i].setValue(cvr.getMin(i));
			maxSliders[i].setValue(cvr.getMax(i));
		}
	}

	/**
	 * Gets the values shown in the editor.
	 * 
	 * @param cvr
	 *            The values to get (must not be null and should have the same
	 *            number of channels as what is shown in the editor - we get the
	 *            lesser number of channels if they don't match).
	 */
	public void getValues(ColorRangeValues cvr) {
		// Number of channels we can transfer
		int n = Math.min(keepCheckboxes.length, cvr.size());
		for (int i = 0; i < n; i++) {
			cvr.setKeepInRange(i, keepCheckboxes[i].isSelected());
			cvr.setMin(i, minSliders[i].getValue());
			cvr.setMax(i, maxSliders[i].getValue());
		}
	}

	/**
	 * Gets the values shown in the editor.
	 * 
	 * @return A new color range object with the values shown in the editor.
	 */
	public ColorRangeValues getValues() {
		ColorRangeValues cvr = new ColorRangeValues(keepCheckboxes.length);
		getValues(cvr);
		return cvr;
	}

	/**
	 * Set the default configuration name for this editor (when user presses
	 * save defaults or load defaults).
	 * 
	 * @param name
	 *            Simple name (like "pink") to use as the default configuration
	 *            name.
	 */
	public void setDefaultCfgName(String name) {
		defCfgName = name;
	}

	/**
	 * Register a listener to be notified if values are changed on the GUI
	 * widget.
	 * 
	 * @param l
	 *            The change listener that should be added.
	 */
	public void addListener(ChangeListener l) {
		listeners.add(l);
	}

	/**
	 * Unregister a listener to be notified if values are changed on the GUI
	 * widget.
	 * 
	 * @param l
	 *            The change listener that should no longer be notified.
	 */
	public void removeListener(ChangeListener l) {
		listeners.remove(l);
	}

	/**
	 * Helper method to notify all registered listeners that a value has
	 * changed.
	 */
	private void notifyListeners() {
		for (ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * Helper method to create and layout the components necessary for the
	 * number of channels specified.
	 * 
	 * @param n
	 *            Number of channels to provide sliders and checkboxes for.
	 */
	private void createComponents(int n) {
		this.removeAll();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		ChangeListener notifyChange = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				notifyListeners();
			}
		};

		keepCheckboxes = new JCheckBox[n];
		minSliders = new JSlider[n];
		maxSliders = new JSlider[n];

		int row = 0;

		for (int i = 0; i < n; i++) {
			JCheckBox keep = new JCheckBox("Keep", true);
			JSlider min = createSlider(0);
			JSlider max = createSlider(255);

			keep.addChangeListener(notifyChange);
			min.addChangeListener(notifyChange);
			max.addChangeListener(notifyChange);

			int col = 0;
			GridBagConstraints gbc = createGridBagConstraints(row, col);
			gbc.insets = new Insets(10, 2, 2, 2);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			add(new JLabel("Channel " + i), gbc);
			row++;

			col = 0;
			// gbc = createGridBagConstraints(row, col++);
			// add(new JLabel("In Range"), gbc);
			gbc = createGridBagConstraints(row, col++);
			add(keep, gbc);
			row++;

			col = 0;
			// gbc = createGridBagConstraints(row, col++);
			// add(new JLabel("Min Value"), gbc);
			gbc = createGridBagConstraints(row, col++);
			add(min, gbc);
			row++;

			col = 0;
			// gbc = createGridBagConstraints(row, col++);
			// add(new JLabel("Max Value"), gbc);
			gbc = createGridBagConstraints(row, col++);
			add(max, gbc);
			row++;

			keepCheckboxes[i] = keep;
			minSliders[i] = min;
			maxSliders[i] = max;
		}

		JPanel buttons = new JPanel();
		new BoxLayout(buttons, BoxLayout.X_AXIS);
		row++;
		int col = 0;
		GridBagConstraints gbc = createGridBagConstraints(row, col++);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(10, 2, 2, 2);
		gbc.anchor = GridBagConstraints.CENTER;
		add(buttons, gbc);

		final JButton openButton = new JButton("Open");
		final JButton saveAsButton = new JButton("Save As");
		final JButton saveButton = new JButton("Save");
		saveButton.setEnabled(lastFile != null);
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
				saveButton.setEnabled(lastFile != null);
			}
		});

		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				saveAs();
				saveButton.setEnabled(lastFile != null);
			}
		});

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (lastFile == null) {
					setEnabled(false);
				} else {
					save();
				}
			}
		});

		saveAsDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lastFile = ColorRangeValues.getFile(defCfgName);
				save();
			}
		});

		loadDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open(ColorRangeValues.getFile(defCfgName));
			}
		});

	}

	/**
	 * Helper method to create a color slider widget.
	 * 
	 * @param val
	 *            Initial value for slider (we force to range of [0, 255]).
	 * @return A GUI widget to add to your panel.
	 */
	private JSlider createSlider(int val) {
		int minVal = 0;
		int maxVal = 255;
		JSlider slider = new JSlider(minVal, maxVal, Math.min(maxVal, Math.max(minVal, val)));
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		return slider;
	}

	/**
	 * Saves the current settings in the editor to the last file location.
	 * 
	 * @return true if file was saved, false if failure or last file location
	 *         has not been set yet (NOTE: Displays error message if save
	 *         fails).
	 */
	public boolean save() {
		if (lastFile != null) {
			try {
				ColorRangeValues cvr = getValues();
				cvr.saveSettings(lastFile);
				return true;
			} catch (Exception e) {
				String msg = "We were unable to save the color range settings to " + lastFile + " ("
						+ e.getLocalizedMessage() + ")";
				String title = "Color Range Settings Save Failure";
				showMessageDialog(null, msg, title, ERROR_MESSAGE);
				lastFile = null;
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
			lastFile = chooser.getSelectedFile();
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
		FileFilter filter = new FileNameExtensionFilter("Color Range Configuration", exts);
		chooser.setFileFilter(filter);
		chooser.addChoosableFileFilter(filter);
		if (lastFile != null) {
			chooser.setSelectedFile(lastFile);
		} else {
			chooser.setSelectedFile(ColorRangeValues.getFile(defCfgName));
		}
		return chooser;
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
			ColorRangeValues crv = getValues();
			crv.loadSettings(f);
			lastFile = f;
			return true;
		} catch (Exception e) {
			String msg = "We were unable to load FOV settings from " + f + " (" + e.getLocalizedMessage() + ")";
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
		gbc.insets = new Insets(2, 20, 2, 2);
		return gbc;
	}

	/**
	 * Test application to verify the editor works (creates pop-up frame showing
	 * the editor).
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Color Range Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ColorRangeEditor());
		frame.setVisible(true);
		frame.pack();
	}

}
