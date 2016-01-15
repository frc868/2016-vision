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

import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.Sequence;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

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
	private JFrame frame;

	/**
	 * Title to apply to the main frame.
	 */
	private final String title;

	/**
	 * Used to display the image.
	 */
	private final JLabel imageView = new JLabel();

	/**
	 * Used for top menu bar.
	 */
	private JMenuBar _Menu;

	/**
	 * Used to show preferences.
	 */
	private JDialog _Dialog;

	/**
	 * Timer used to make image requests.
	 */
	//private final Timer _Timer;

	/**
	 * Video capture device to get images from
	 */
	private VideoCapture _CaptureDev;

	/**
	 * Timer used for live video feed handling.
	 */
	private final Timer _CaptureTimer;

	/**
	 * The filter to apply.
	 */
	private MatFilter _Filter;

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
		_Filter = new Sequence();

		_CaptureTimer = new Timer(20, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Mat img = new Mat();
				if (_CaptureDev != null) {
					int cnt = 0;
					if (_CaptureDev.grab()) {
						if (_CaptureDev.retrieve(img)) {
							cnt++;
						}
					}
					if (cnt >= 1) {
						setImage(_Filter.process(img));
					} else {
						System.err.println("Failed to grab image. Open: " + _CaptureDev.isOpened() + "  URL: " + _Url + "  " + _CaptureDev);
					}
				}
			}

		});
	}

	/**
	 * Set the image filter to apply to each received frame.
	 *
	 * @param filter
	 *            The filter to apply - must not be null.
	 */
	public void setFilter(MatFilter filter) {
		_Filter = filter;
	}

	/**
	 * Get a reference to the current image.
	 *
	 * @return A IplImage object (or null if not set).
	 */
	public Mat getImage() {
		return _Image;
	}

	/**
	 * Set the image that tool should display and work on.
	 *
	 * @param img
	 *            The IplImage you want to display/use in the tool.
	 */
	public void setImage(Mat img) {
		_Image = img;
		BufferedImage iconImg = null;
		if (img != null) {
			if (img.type() == CvType.CV_8UC1) {
				iconImg = Conversion.cvGrayToBufferedImage(img);
			} else if (img.type() == CvType.CV_8UC3) {
				iconImg = Conversion.cvRgbToBufferedImage(img);
			}
		}
		if (iconImg != null) {
			imageView.setIcon(new ImageIcon(iconImg));
		} else {
			imageView.setIcon(null);
		}
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				startVideoFeed();
			}
		}));

		addMenuItem(fileMenu, new JMenuItem(new AbstractAction("Stop") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				stopVideoFeed();
			}
		}));

		addMenuItem(fileMenu, new JMenuItem(createPreferencesAction()));
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
		if (frame == null) {
			frame = new JFrame(title);

			// Layout frame contents
			// Menubar at top (if present)
			addMenuItems();
			if (_Menu != null) {
				frame.add(_Menu, BorderLayout.NORTH);
			}

			// Image display in the center
			final JScrollPane imageScrollPane = new JScrollPane(imageView);
			imageScrollPane.setPreferredSize(new Dimension(660, 500));
			frame.add(imageScrollPane, BorderLayout.CENTER);
		}
		return frame;
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
				startVideoFeed();
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
        final JTextField frameWidth = new JTextField(Integer.toString(_FrameWidth));
        final JTextField frameHeight = new JTextField(Integer.toString(_FrameHeight));

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
		};
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

        frameWidth.setToolTipText("Specify the width of the local video feed or leave at 0 for default");
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

        frameHeight.setToolTipText("Specify the Height of the local video feed or leave at 0 for default");
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

	public void startVideoFeed() {
		stopVideoFeed();
		// _CaptureDev = new VideoCapture(0);
		// String url = "http://10.8.68.11/mjpg/video.mjpg";
		// url = "rtsp://10.8.68.11:554/axis-media/media.amp?videocodec=h264";
		_CaptureDev = new VideoCapture();
		if (_UseUrl) {
			_CaptureDev.open(_Url);
			
			if (_CaptureDev.isOpened()) {
				System.err.println("Starting IP camera feed from: " + _Url);
				_CaptureTimer.start();
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to open IP camera video feed: " + _Url, "Failed to Open Video Feed", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			_CaptureDev.open(_DeviceId);

			if (_FrameWidth > 0 && _FrameHeight > 0) {
				_CaptureDev.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, _FrameWidth);
				_CaptureDev.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, _FrameHeight);
			}
			
			if (_CaptureDev.isOpened()) {
				System.err.println("Starting Video Feed using Web Cam: " + _DeviceId);
				_CaptureTimer.start();
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to open web camera: " + _DeviceId, "Failed to Open Web Cam", JOptionPane.ERROR_MESSAGE);
			}			
		}
	}

	public void stopVideoFeed() {
		if (_CaptureDev != null) {
			System.err.println("Stopping Video Feed");
			_CaptureTimer.stop();
			_CaptureDev.release();
			_CaptureDev = null;
		}
	}

}
