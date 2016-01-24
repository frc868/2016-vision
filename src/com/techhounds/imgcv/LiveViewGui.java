/*
 * Copyright (c) 2013, Paul Blankenbaker
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

import com.techhounds.imgcv.filters.ColorSpace;
import com.techhounds.imgcv.filters.DoNothingFilter;
import com.techhounds.imgcv.filters.GrayScale;
import com.techhounds.imgcv.filters.MatFilter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * A base class that allows you to quickly build a test tool to exercise a
 * single filter by continually feeding data to the filter.
 *
 * <p>
 * This class provides the foundation for creating a GUI that allows you to test
 * your image filter without having to deal with a lot of the overhead of
 * setting up your own test tool. It works in the following manner:
 * </p>
 *
 * <ul>
 * <li>You extend (or create a instance) of this class.</li>
 * <li>At a minimum you invoke the {@link #setFilter()} method to set the
 * {@link MatFilter} implementation you want to test.</li>
 * <li>You construct a instance of your new class and then invoke the
 * {@link #main} method to show the tool.</li>
 * </ul>
 *
 * <p>
 * NOTE: You can run this class by itself (in which case it displays the
 * original image un-filtered).
 * </p>
 * 
 * <p>
 * The {@link com.techhounds.imgcv.tools.LiveView2013} class provides a example
 * of extending this class.
 * </p>
 *
 * @author pkb
 */
public class LiveViewGui {

	/**
	 * Current image loaded and displayed.
	 */
	private Mat _Image = null;

	/**
	 * Last URL that was opened.
	 */
	// private String _Url = "http://10.8.68.11/jpg/1/image.jpg";
	// private String _Url = "http://admin:1234@192.168.1.25/mjpg/video.mjpg";
	private String _Url = "rtsp://10.8.68.11:554/axis-media/media.amp?videocodec=h264";

	/** ID of video device to use on the system. */
	private int _DeviceId = 0;

	/** Desired dimensions of video feed (set to 0 to take defaults). */
	private int _FrameWidth = 0;
	private int _FrameHeight = 0;

	/** Whether to use the web cam or a URL fetch of our video feed. */
	private boolean _UseUrl = false;

	/** Access to configurable values and set up. */
	private final Configuration _Config;

	/**
	 * Main frame of the tool.
	 */
	private JFrame _JFrame;

	/**
	 * Title to apply to the main frame.
	 */
	private final String title;

	/**
	 * Used to display the image.
	 */
	private final JMat _ImageViewer = new JMat();

	/**
	 * Used for top menu bar.
	 */
	private JMenuBar _Menu;

	/**
	 * Used to show preferences.
	 */
	private JDialog _Dialog;

	/**
	 * Background thread to grab images from camera
	 */
	private FrameGrabber _FrameGrabber;

	/**
	 * Timer used for live video feed handling.
	 */
	private final Timer _CaptureTimer;

	/**
	 * The filter to apply.
	 */
	private MatFilter _Filter;

	/** Displays the FPS of images coming in from the source. */
	private JLabel _CameraFps;

	/** Displays the estimated maximum FPS of the filter code. */
	private JLabel _FilterFps;

	/** Number of images processed. */
	protected int _FilteredCount;

	/** Total number of nanoseconds spent processing images. */
	protected long _FilteredDur;

	/**
	 * Constructs a new instance with a given title - you will override.
	 *
	 * @param title
	 *            Title to appear on frame title bar.
	 *
	 * @throws HeadlessException
	 *             If attempt to run on non-GUI based system.
	 */
	public LiveViewGui(String title) throws HeadlessException {
		this.title = title;
		_Config = new Configuration();
		_Config.loadOpenCvLibrary();

		// A do nothing filter
		_Filter = new DoNothingFilter();

		_FrameGrabber = new FrameGrabber();

		_CaptureTimer = new Timer(10, new ActionListener() {
			long lastFrame = 0;

			@Override
			public void actionPerformed(ActionEvent e) {
				long frame = _FrameGrabber.getFrameCount();
				if (frame != lastFrame) {
					Mat img = _FrameGrabber.getLastImage();
					if (img != null) {
						_Image = img;
						MatFilter filter = _Filter;

						long start = System.nanoTime();
						Mat results = filter.process(img);
						long end = System.nanoTime();

						_FilteredCount++;
						_FilteredDur += (end - start);
						if (_FilteredCount % 20 == 0) {
							_FilterFps.setText("" + getFilterFps());
						}

						_ImageViewer.setMat(results);
					}
					if (lastFrame == 0) {
						_ImageViewer.setSize(_ImageViewer.getPreferredSize());
						_JFrame.pack();
					}
					lastFrame = frame;
					if (frame % 10 == 0) {
						_CameraFps.setText(Integer.toString(_FrameGrabber
								.getFps()));
					}
				}
			}
		});
	}

	/**
	 * Returns the estimated FPS rate that the filter is capable of on this
	 * system (maximum images per second you could process).
	 * 
	 * @return Estimated maximum rate of your image filtering routine.
	 */
	public int getFilterFps() {
		if (_FilteredDur > 0) {
			return (int) (_FilteredCount * 1000000000L / _FilteredDur);
		}
		return 0;
	}

	/**
	 * Set the image filter to apply to each received frame.
	 *
	 * @param filter
	 *            The filter to apply - must not be null.
	 */
	public void setFilter(MatFilter filter) {
		_FilteredCount = 0;
		_FilteredDur = 0;
		_Filter = filter;
	}

	/**
	 * Returns a action item that can be used when you want to show the
	 * preferences.
	 *
	 * @return An action item that can be added to a menu or button.
	 */
	protected Action createPreferencesAction() {
		Action action = new AbstractAction("Preferences") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showPreferences();
			}

		};
		return action;
	}

	/**
	 * Creates a action to "fit" the frame based on the current image size.
	 * 
	 * @return Action that will cause program to terminate.
	 */
	protected Action createFitAction() {
		Action action = new AbstractAction("Fit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				_ImageViewer.setSize(_ImageViewer.getPreferredSize());
				_JFrame.pack();
			}

		};
		return action;
	}

	/**
	 * Adds all of the menu items to the main menu bar.
	 *
	 * <p>
	 * Derived classes will want to override this method if they want to
	 * customize the menu bar or sub-menus. If you override this method, you
	 * will typically want to call it from your overriding method as this
	 * implementation adds a lot of stock features.
	 * </p>
	 */
	protected void addMenuItems() {
		String fileMenu = "File";

		addMenuItem(fileMenu, new JMenuItem(new AbstractAction("Start") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				startVideoFeed();
			}
		}));

		addMenuItem(fileMenu, new JMenuItem(new AbstractAction("Stop") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				stopVideoFeed();
			}
		}));

		// Action performed when "Open Image" button is pressed
		addMenuItem(fileMenu, new JMenuItem(new AbstractAction("Save Image") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				_JFrame.setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					saveImage();
				} finally {
					_JFrame.setCursor(Cursor.getDefaultCursor());
				}
			}
		}));

		addMenuItem(fileMenu, new JMenuItem(createPreferencesAction()));

		addMenuItem(fileMenu, new JMenuItem(createFitAction()));

		addMenuItem(fileMenu, new JMenuItem(new AbstractAction("Exit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				_FrameGrabber.stop();
				_CaptureTimer.stop();
				System.exit(0);
			}
		}));

		addFilter("Raw Feed", new DoNothingFilter());
		addFilter("Gray Scale", new GrayScale());
		addFilter("HSV", new ColorSpace(Imgproc.COLOR_BGR2HSV));
	}

	/**
	 * Method used to add filters to the set of choices in the live view window.
	 * 
	 * @param label
	 *            Name to associate with your filter.
	 * @param filter
	 *            The filter to apply when selected.
	 */
	protected void addFilter(String label, MatFilter filter) {
		addMenuItem("Filter", createMenuFilterItem(label, filter));
	}

	/**
	 * Creates a menu item which changes the filter when selected.
	 * 
	 * @param label
	 *            The label for the filter.
	 * @param filter
	 *            The image filter to apply.
	 * @return A new menu item that can be inserted into a menu.
	 */
	private JMenuItem createMenuFilterItem(String label, final MatFilter filter) {
		JMenuItem mi = new JMenuItem(label);
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setFilter(filter);
			}
		});
		return mi;
	}

	/**
	 * Adds a single menu action item to the specified sub-menu.
	 *
	 * @param parentMenu
	 *            The name of the sub-menu to go under (like: "File", "Edit",
	 *            etc). If the sub-menu doesn't yet exist, we will create it and
	 *            add it to the menu bar.
	 * @param item
	 *            The item to add.
	 */
	protected void addMenuItem(String parentMenu, JMenuItem item) {
		if (_Menu == null) {
			_Menu = new JMenuBar();
		}
		JMenuItem subMenu = findCreateSubMenu(parentMenu);
		subMenu.add(item);
	}

	/**
	 * Prompts user to select a file to save the image to and saves it out.
	 *
	 * <p>
	 * The user controls the format of the output file based on the file
	 * extension specified (.png, .jpg, etc) - as supported by the opencv
	 * library. NOTE: If there is not a image currently loaded, this method does
	 * nothing (just returns).
	 * </p>
	 */
	private void saveImage() {
		if (_Image == null) {
			return;
		}
		JFileChooser fileChooser = new JFileChooser();

		String fname = _Config.getLastSavedFile(null);

		// Ask user for the location of the image file
		if (fname != null) {
			fileChooser.setSelectedFile(new File(fname));
		}
		if (fileChooser.showSaveDialog(_JFrame) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// Load the image
		File imgFile = fileChooser.getSelectedFile();
		String path = imgFile.getAbsolutePath();
		Highgui.imwrite(path, _Image);
		_Config.setLastSavedFile(path);
	}

	/**
	 * Finds sub-menu with specified name or creates it.
	 *
	 * @param name
	 *            Name of the sub-menu to find.
	 * @return The menu item having the name specified (will create and add to
	 *         main menu bar if it had not existed).
	 */
	private JMenuItem findCreateSubMenu(String name) {
		int n = _Menu.getMenuCount();
		for (int i = 0; i < n; i++) {
			JMenu menu = _Menu.getMenu(i);
			if (name.equals(menu.getName())) {
				return menu;
			}
			menu = searchMenu(name, menu);
			if (menu != null) {
				return menu;
			}
		}
		JMenu menu = new JMenu(name);
		menu.setName(name);
		_Menu.add(menu);
		return menu;
	}

	/**
	 * Helper method to recursively search for a sub-menu having a specific
	 * name.
	 *
	 * @param name
	 *            The name associated with the sub-menu you want to find.
	 * @param menuItem
	 *            Where to start the search from.
	 * @return A reference to the found JMenu entry (having name specified) or
	 *         null if not found.
	 */
	private JMenu searchMenu(String name, JMenuItem menuItem) {
		if (!(menuItem instanceof JMenu)) {
			return null;
		}
		JMenu menu = (JMenu) menuItem;
		if (name.equals(menu.getName())) {
			return menu;
		}
		int n = menu.getItemCount();
		for (int i = 0; i < n; i++) {
			JMenu subMenu = searchMenu(name, menu.getItem(i));
			if (subMenu != null) {
				return subMenu;
			}
		}
		return null;
	}

	/**
	 * Constructs the main JFrame for the application (if not done so yet).
	 *
	 * <p>
	 * Unfortunately, we can not build the entire GUI at the time of
	 * construction as this would not allow you to override the
	 * {@link #addControls()} method. When you are ready to realize the GUI, you
	 * can call this method. However, you only need to call this method if you
	 * need to customize something. Most often you will simply invoke the
	 * {@link #main()} method.
	 * </p>
	 *
	 * @return Reference to the main JFrame associated with the application.
	 */
	public JFrame getMainFrame() {
		if (_JFrame == null) {
			_JFrame = new JFrame(title);

			// Layout frame contents
			// Menubar at top (if present)
			addMenuItems();
			if (_Menu != null) {
				_JFrame.add(_Menu, BorderLayout.NORTH);
			}

			_JFrame.add(createStatusPanel(), BorderLayout.SOUTH);

			// Image display in the center
			_JFrame.add(_ImageViewer, BorderLayout.CENTER);
		}
		return _JFrame;
	}

	/**
	 * Builds the status bar for bottom of window that is used to show FPS
	 * values.
	 * 
	 * @return The newly created status bar widget.
	 */
	private Component createStatusPanel() {
		JPanel statusPanel = new JPanel(new BorderLayout(4, 0));
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

		JLabel fpsLabel = new JLabel("Camera FPS");

		_CameraFps = new JLabel("-");
		//_CameraFps.setHorizontalAlignment(SwingConstants.RIGHT);
		//_CameraFps.setPreferredSize(new Dimension(24, 8));

		JLabel fpsFilterLabel = new JLabel("Filter FPS");

		_FilterFps = new JLabel("-");
		//_FilterFps.setHorizontalAlignment(SwingConstants.RIGHT);
		//_FilterFps.setPreferredSize(new Dimension(24, 8));

		statusPanel.add(fpsLabel);
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(_CameraFps);
		statusPanel.add(Box.createHorizontalStrut(30));
		statusPanel.add(fpsFilterLabel);
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(_FilterFps);
		statusPanel.add(Box.createHorizontalGlue());

		return statusPanel;
	}

	/**
	 * After you are done initializing your implementation, call this method to
	 * show the GUI tool.
	 */
	public final void main() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = getMainFrame();
				frame.pack();
				// Mark for display in the center of the screen
				frame.setLocationRelativeTo(null);
				// Exit application when frame is closed.
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
				// Load settings from configuration
				_UseUrl = _Config.isVideoFeedFromUrl(_UseUrl);
				_DeviceId = _Config.getDeviceId(_DeviceId);
				_FrameWidth = _Config.getFrameWidth(_FrameWidth);
				_FrameHeight = _Config.getFrameHeight(_FrameWidth);
				_Url = _Config.getVideoUrl(_Url);
			}
		});

	}

	/**
	 * A test method so you can try running just the default base class (for
	 * testing purposes only).
	 *
	 * @param args
	 *            Command line arguments (ignored).
	 */
	public static void main(String[] args) {
		new LiveViewGui("Test").main();
	}

	/**
	 * Show dialog panel with preference settings.
	 */
	public void showPreferences() {
		if (_Dialog == null) {
			_Dialog = new JDialog(getMainFrame(), "Preferences");
			_Dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

			JPanel contentPane = new JPanel(new BorderLayout());
			_Dialog.setContentPane(contentPane);

			JTabbedPane tabs = new JTabbedPane();
			contentPane.add(tabs, BorderLayout.CENTER);
			addPreferenceTabs(tabs);

			Action close = new AbstractAction("Close") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					_Dialog.setVisible(false);
				}
			};
			contentPane.add(new JButton(close), BorderLayout.SOUTH);
		}

		_Dialog.setVisible(true);
		_Dialog.pack();
	}

	/**
	 * Adds preference panels to the tabbed pane in the preference dialog box.
	 *
	 * <p>
	 * Derived classes will want to override this method if they want to
	 * customize (or add to) the preferences dialog box. If you override this
	 * method, you will typically want to call it from your overriding method as
	 * this default implementation implementation adds the stock features.
	 * </p>
	 *
	 * @param tabs
	 *            The tabbed pane component you can add new tabs to.
	 */
	protected void addPreferenceTabs(JTabbedPane tabs) {
		tabs.addTab("Grab", createGrabPreferences());
	}

	/**
	 * Creates a GUI panel which lets the user configure the URL grab
	 * preferences.
	 *
	 * @return A new JPanel that can be added to a GUI dialog box.
	 */
	protected JPanel createGrabPreferences() {
		JPanel options = new JPanel();
		final JTextField devId = new JTextField(Integer.toString(_DeviceId));
		final JTextField url = new JTextField(_Url);
		final JTextField frameWidth = new JTextField(
				Integer.toString(_FrameWidth));
		final JTextField frameHeight = new JTextField(
				Integer.toString(_FrameHeight));

		class UseUrlCheckBox extends JCheckBox {
			private static final long serialVersionUID = 1L;

			UseUrlCheckBox() {
				super("Use URL to get images", _UseUrl);
			}

			public void updateWhatIsEnabled() {
				boolean useDev = !_UseUrl;
				url.setEnabled(_UseUrl);
				devId.setEnabled(useDev);
				frameWidth.setEnabled(useDev);
				frameHeight.setEnabled(useDev);
			}
		}
		;
		final UseUrlCheckBox useUrl = new UseUrlCheckBox();

		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		options.add(new JLabel("URL of video feed:"));

		useUrl.setToolTipText("Select this option to retrieve video stream from remote URL instead of your own web cam");
		useUrl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				_UseUrl = useUrl.isSelected();
				_Config.setVideoFeedFromUrl(_UseUrl);
				useUrl.updateWhatIsEnabled();
			}
		});

		url.setToolTipText("Enter the URL of your robot's camera (like: http://10.8.68.11/jpg/1/image.jpg)");
		url.getDocument().addDocumentListener(new DocumentListener() {

			private void updateValue(DocumentEvent e) {
				_Url = url.getText();
				_Config.setVideoUrl(_Url);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateValue(e);
			}
		});

		devId.setToolTipText("Enter the video device number of the web cam on your laptop (typically 0)");
		devId.getDocument().addDocumentListener(new DocumentListener() {

			private void updateValue(DocumentEvent e) {
				try {
					_DeviceId = Integer.parseInt(devId.getText());
					_Config.setDeviceId(_DeviceId);
				} catch (NumberFormatException nfe) {

				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateValue(e);
			}
		});

		frameWidth
				.setToolTipText("Specify the width of the local video feed or leave at 0 for default");
		frameWidth.getDocument().addDocumentListener(new DocumentListener() {

			private void updateValue(DocumentEvent e) {
				try {
					_FrameWidth = Integer.parseInt(frameWidth.getText());
					_Config.setFrameWidth(_FrameWidth);
				} catch (NumberFormatException nfe) {

				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateValue(e);
			}
		});

		frameHeight
				.setToolTipText("Specify the Height of the local video feed or leave at 0 for default");
		frameHeight.getDocument().addDocumentListener(new DocumentListener() {

			private void updateValue(DocumentEvent e) {
				try {
					_FrameHeight = Integer.parseInt(frameHeight.getText());
					_Config.setFrameHeight(_FrameHeight);
				} catch (NumberFormatException nfe) {

				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateValue(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateValue(e);
			}
		});

		useUrl.setToolTipText("Select this option to retrieve video stream from remote URL instead of your own web cam");
		useUrl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				_UseUrl = useUrl.isSelected();
				_Config.setVideoFeedFromUrl(_UseUrl);
			}
		});

		options.add(useUrl);
		options.add(new JLabel("Device ID"));
		options.add(devId);
		options.add(new JLabel("Desired Video Width"));
		options.add(frameWidth);
		options.add(new JLabel("Desired Video Height"));
		options.add(frameHeight);
		options.add(new JLabel("URL of Robot Video Feed"));
		options.add(url);

		useUrl.updateWhatIsEnabled();

		return options;
	}

	/**
	 * Starts the background thread and timer that read and procees images off
	 * the video stream.
	 */
	public void startVideoFeed() {
		stopVideoFeed();
		// _CaptureDev = new VideoCapture(0);
		// String url = "http://10.8.68.11/mjpg/video.mjpg";
		// url = "rtsp://10.8.68.11:554/axis-media/media.amp?videocodec=h264";
		if (_UseUrl) {
			_FrameGrabber.start(_Url);
			_CaptureTimer.start();
		} else {
			_FrameGrabber.start(_DeviceId, _FrameWidth, _FrameHeight);
			_CaptureTimer.start();
		}
	}

	/**
	 * Stops the background thread and timer used to read and process images off
	 * the video stream.
	 */
	public void stopVideoFeed() {
		_FrameGrabber.stop();
		_CaptureTimer.stop();
	}

}
