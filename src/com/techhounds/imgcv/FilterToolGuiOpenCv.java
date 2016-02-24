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

import com.techhounds.imgcv.filters.BlackWhite;
import com.techhounds.imgcv.filters.Blur;
import com.techhounds.imgcv.filters.ColorRange;
import com.techhounds.imgcv.filters.ColorSpace;
import com.techhounds.imgcv.filters.Contours;
import com.techhounds.imgcv.filters.ContrastBrightness;
import com.techhounds.imgcv.filters.CrossHair;
import com.techhounds.imgcv.filters.Dilate;
import com.techhounds.imgcv.filters.Erode;
import com.techhounds.imgcv.filters.FillChannel;
import com.techhounds.imgcv.filters.FovOverlay;
import com.techhounds.imgcv.filters.GrayScale;
import com.techhounds.imgcv.filters.MatFilter;
import com.techhounds.imgcv.filters.Negative;
import com.techhounds.imgcv.filters.Sequence;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

/**
 * A base class that allows you to quickly build a test tool for the purpose of
 * testing your custom image filters.
 *
 * <p>
 * This class provides the foundation for creating a GUI that allows you to
 * focus on the creation of image filters without having to deal with a lot of
 * the overhead of setting up your own test tool. It works in the following
 * manner:
 * </p>
 *
 * <ul>
 * <li>You extend this class and provide a constructor.</li>
 * <li>At a minimum you override the {@link #addControls()} method and/or
 * {@link #addMenuItems()} to add the {@link ImageFilter} implementations you
 * want to test.</li>
 * <li>You construct a instance of your new class and then invoke the
 * {@link #main} method to show the tool.</li>
 * </ul>
 *
 * <p>
 * The {@link com.techhounds.imgcv.frc2013.FilterTool2013} class provides a
 * example of extending this class.
 * </p>
 *
 * @author pkb
 */
public class FilterToolGuiOpenCv {

	/**
	 * Current image loaded and displayed.
	 */
	private Mat _Image = null;

	/**
	 * Previous image loaded and displayed (or null) - for Undo/Redo operation.
	 */
	private Mat _UndoImage = null;

	/**
	 * Last image loaded or grabbed - for Revert operation.
	 */
	private Mat _LastLoadedImage = null;

	/**
	 * File last opened (or null).
	 */
	private File lastFile;

	/**
	 * Last URL that was opened.
	 */
	private String _Url = "http://10.8.68.11/jpg/1/image.jpg";

	/**
	 * Used to save/retrieve preferences.
	 */
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
	 * Used for loading and saving image files.
	 */
	private final JFileChooser fileChooser = new JFileChooser();

	/**
	 * Used to display the image.
	 */
	private final JLabel imageView = new JLabel();

	/**
	 * Used to hold "quick access" controls to left of image.
	 */
	private final JPanel sidePanel;

	/**
	 * Used for information area at bottom of screen.
	 */
	private final JPanel botPanel;

	/**
	 * Used to add controls to the side panel.
	 */
	private final GridBagConstraints sidePanelConstraints;

	/**
	 * Used for top menu bar.
	 */
	private JMenuBar _Menu;

	/**
	 * Used to show preferences.
	 */
	private JDialog _Dialog;

	/**
	 * Used for dynamic color range tool.
	 */
	private ColorRange _ColorRange;

	/**
	 * Holds base image when user pulls up the color range tool.
	 */
	private Mat _ColorRangeImage;

	/** Label to display information about the image. */
	private JLabel _ImageInfo;

	/** Width of last image displayed. */
	private int _LastWidth;

	/** Height of last image displayed. */
	private int _LastHeight;

	/** Number of channels in last image displayed. */
	private int _LastChannel;

	/** Label for cursor X coordinate (position of mouse in image). */
	private JLabel _PointerX;

	/** Label for cursor Y coordinate (position of mouse in image). */
	private JLabel _PointerY;

	/** Where the image is displayed. */
	private JScrollPane imageScrollPane;

	/**
	 * Used to detect when some other action has been applied while the color
	 * range tool is up (used to let us know when to reload the base image for
	 * the color range tool).
	 */
	private Mat _LastColorRangeImage;

	/**
	 * Reference to last filter applied/set.
	 */
	protected MatFilter _LastFilter;

	/**
	 * Constructs a new instance with a given title - you will override.
	 *
	 * @param title
	 *            Title to appear on frame title bar.
	 *
	 * @throws HeadlessException
	 *             If attempt to run on non-GUI based system.
	 */
	protected FilterToolGuiOpenCv(String title) throws HeadlessException {
		this.title = title;
		_Config = new Configuration();
		_Config.loadOpenCvLibrary();

		// Create vertical buttons panel
		GridBagLayout gbl = new GridBagLayout();
		sidePanelConstraints = new GridBagConstraints();
		sidePanelConstraints.gridx = sidePanelConstraints.gridy = 0;
		sidePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		sidePanelConstraints.weightx = 0.5;
		sidePanelConstraints.insets = new Insets(1, 1, 1, 1);
		sidePanel = new JPanel(gbl);

		botPanel = new JPanel();
		botPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.X_AXIS));
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
		boolean needFit = (_Image == null);
		_UndoImage = _Image;
		_Image = img;
		BufferedImage iconImg = null;
		if (img != null) {

			// Update image information (if changed)
			if (_ImageInfo != null) {
				int width = img.cols();
				int height = img.rows();
				int channel = img.channels();
				if (width != _LastWidth || height != _LastHeight
						|| channel != _LastChannel) {
					_LastWidth = width;
					_LastHeight = height;
					_LastChannel = channel;
					StringBuilder sb = new StringBuilder("Image: ");
					sb.append(width);
					sb.append('x');
					sb.append(height);
					sb.append('x');
					sb.append(channel);
					_ImageInfo.setText(sb.toString());
				}
			}

			if (img.type() == CvType.CV_8UC1) {
				iconImg = Conversion.cvGrayToBufferedImage(img);
			} else if (img.type() == CvType.CV_8UC3) {
				iconImg = Conversion.cvRgbToBufferedImage(img);
			}
		}
		if (iconImg != null) {
			imageView.setIcon(new ImageIcon(iconImg));
			if (needFit) {
				fit();
			}
		} else {
			imageView.setIcon(null);
		}
	}

	/**
	 * Adds a GUI control to the side panel (the quick action area).
	 *
	 * @param control
	 *            Any Java Swing component you'd like (typically buttons, but
	 *            can be input fields, labels, sliders, etc).
	 */
	protected void addControl(JComponent control) {
		sidePanel.add(control, sidePanelConstraints);
		sidePanelConstraints.gridy++;
	}

	/**
	 * Adds a GUI information widget to the bottom panel.
	 * 
	 * @param info
	 *            Information object to add.
	 */
	protected void addInfo(JComponent info) {
		botPanel.add(info);
	}

	/**
	 * Adds a image filter button to the side panel.
	 *
	 * @param name
	 *            ASCII name to appear on the button.
	 * @param processor
	 *            The filter that should be applied to the current image when
	 *            the Action is fired.
	 * @param revert
	 *            Pass true if you want to revert the image prior to applying
	 *            the filter, false if not.
	 * @return The button that was created and added to the side panel.
	 */
	protected JButton addImageProcessingButton(String name,
			final MatFilter processor, boolean revert) {
		JButton button = new JButton(createImageProcessingAction(name,
				processor, revert));
		addControl(button);
		return button;
	}

	/**
	 * Adds a image filter button to the side panel (does not revert prior to
	 * applying filter).
	 *
	 * @param name
	 *            ASCII name to appear on the button.
	 * @param processor
	 *            The filter that should be applied to the current image when
	 *            the Action is fired.
	 * @return The button that was created and added to the side panel.
	 */
	protected JButton addImageProcessingButton(String name,
			final MatFilter processor) {
		return addImageProcessingButton(name, processor, false);
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to trigger the application of your specified
	 * image filter.
	 *
	 * <p>
	 * A quick example:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * Action makeGray = createImageProcessingAction("To Gray", new GrayScale(), true);
	 * JButton grayButton = new JButton(makeGray);
	 * </code>
	 * </pre>
	 *
	 * @param name
	 *            ASCII name to associate with the action.
	 * @param processor
	 *            The filter that should be applied to the current image when
	 *            the Action is fired.
	 * @param revert
	 *            Pass true if you want to revert the image prior to applying
	 *            the filter, false if not.
	 * 
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createImageProcessingAction(String name,
			final MatFilter processor, final boolean revert) {
		final Action processAction = new AbstractAction(name) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					// If revert flag set, then load last original image
					if (revert && (_LastLoadedImage != null)) {
						setImage(_LastLoadedImage);
					}
					// Process and update image display if image is loaded
					if (_Image != null) {
						_LastFilter = processor;
						setImage(processor.process(_Image.clone()));
					} else {
						showMessageDialog(frame,
								frame.getName() + " - failure",
								frame.getTitle(), ERROR_MESSAGE);
					}
				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		imageView.addPropertyChangeListener("icon",
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						processAction.setEnabled(_Image != null);
					}

				});
		processAction.setEnabled(_Image != null);
		return processAction;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to trigger the application of your specified
	 * image filter (does not revert current image prior to applying).
	 *
	 * <p>
	 * A quick example:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * Action makeGray = createImageProcessingAction("To Gray", new GrayScale());
	 * JButton grayButton = new JButton(makeGray);
	 * </code>
	 * </pre>
	 *
	 * @param name
	 *            ASCII name to associate with the action.
	 * @param processor
	 *            The filter that should be applied to the current image when
	 *            the Action is fired.
	 * 
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createImageProcessingAction(String name,
			final MatFilter processor) {
		return createImageProcessingAction(name, processor, false);
	}

	/**
	 * Helper method to create a {@link JMenuItem} when building filter menus
	 * (does not revert first).
	 *
	 * @param name
	 *            The label you want to see in the menu for the filter.
	 * @param imgproc
	 *            The filter to apply when the user selects the item.
	 * @return A new {@link JMenuItem} you can add to a menu.
	 */
	protected JMenuItem createImageProcessingMenuItem(String name,
			MatFilter imgproc) {
		return createImageProcessingMenuItem(name, imgproc, false);
	}

	/**
	 * Helper method to create a {@link JMenuItem} when building filter menus
	 * with the option to revert the image prior to applying the filter.
	 *
	 * @param name
	 *            The label you want to see in the menu for the filter.
	 * @param imgproc
	 *            The filter to apply when the user selects the item.
	 * @param revert
	 *            Pass true if you want to revert the image prior to applying
	 *            the filter.
	 * @return A new {@link JMenuItem} you can add to a menu.
	 */
	protected JMenuItem createImageProcessingMenuItem(String name,
			MatFilter imgproc, final boolean revert) {
		JMenuItem item = new JMenuItem(createImageProcessingAction(name,
				imgproc, revert));
		item.setName(name);
		return item;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to cause a Undo (or Redo) of the last image
	 * operation.
	 *
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createUndoImageAction() {

		// Action performed when "Undo" button is pressed
		final Action undoImageAction = new AbstractAction("Undo") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (_UndoImage != null) {
						setImage(_UndoImage);
					}
				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		return undoImageAction;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to cause the file open dialog to appear (open
	 * file).
	 *
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createOpenImageAction() {

		// Action performed when "Open Image" button is pressed
		final Action openImageAction = new AbstractAction("Open Image") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					openImage();
				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		return openImageAction;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to is used to apply a filter to all of the image
	 * files in a directory.
	 *
	 * @param label
	 *            The label to apply to the batch processing command.
	 * @param filter
	 *            The specific filter to apply OR null if you want to apply the
	 *            last filter used.
	 * 
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createBatchProcessAction(String label, MatFilter filter) {

		// Action performed when "Open Image" button is pressed
		final Action batchAction = new AbstractAction(label) {
			private static final long serialVersionUID = 1L;

			private MatFilter myFilter = filter;

			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					JFileChooser chooser = new JFileChooser();
					// Ask user for the location of the image file
					if (lastFile != null) {
						chooser.setSelectedFile(lastFile);
					}
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					// If user picks directory, process all images in directory
					if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
						File dir = chooser.getSelectedFile();
						lastFile = dir;
						MatFilter filter = (myFilter != null) ? myFilter
								: _LastFilter;
						FilterView.showDirectory(dir, filter);
					}

				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		return batchAction;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to cause the tool to try and grab a new image
	 * from the URL (typically pointing at web cam).
	 *
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createGrabImageAction() {

		// Action performed when "Open Image" button is pressed
		final Action openImageAction;
		openImageAction = new AbstractAction("Grab Image") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					Mat image = Conversion.urlToMat(_Url);
					if (image != null) {
						_LastLoadedImage = image;
						setImage(image);
					}
				} catch (IOException ex) {
					String msg = "Failed to get image from: " + _Url;
					JOptionPane.showMessageDialog(frame, msg,
							"Failed to Grab Image", JOptionPane.ERROR_MESSAGE);
				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		return openImageAction;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to cause the tool to open up the save image
	 * dialog.
	 *
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createSaveImageAction() {

		// Action performed when "Open Image" button is pressed
		final Action saveImageAction = new AbstractAction("Save Image") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					saveImage();
				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		return saveImageAction;
	}

	/**
	 * A helper method to create a Action that can be associated with a GUI
	 * widget (like a JButton) to cause the tool to revert to the last loaded
	 * (or grabbed) image (undo all changes since loading).
	 *
	 * @return A Action object you can then associate with a GUI control (like a
	 *         JButton).
	 */
	protected Action createRevertImageAction() {

		final Action revertImageAction = new AbstractAction("Revert") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (_LastLoadedImage != null) {
						setImage(_LastLoadedImage);
					}
				} finally {
					frame.setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		return revertImageAction;
	}

	/**
	 * Adds a separator (line) to the side panel to separate related actions.
	 */
	protected void addSeparator() {
		addControl(new JSeparator());
	}

	/**
	 * Adds the standard set of controls to the side panel.
	 *
	 * <p>
	 * Typically you will override this method in your implementation. Often you
	 * will still want to include this set of controls. Here is a example of
	 * overriding this method and adding to the standard set of controls:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * protected void addControls() {
	 *     super.addControl(); // Include standard controls first
	 *     addSeparator();
	 *     {@link #addImageProcessingButton(java.lang.String, com.techhounds.imgproc.filters.ImageFilter) addImageProcessingButton("Gray", new {@link GrayScale GrayScale()});
	 * }
	 * </code>
	 * </pre>
	 */
	protected void addControls() {
		addControl(new JButton(createOpenImageAction()));
		addControl(new JButton(createGrabImageAction()));
		addControl(new JButton(createSaveImageAction()));
		addSeparator();
		addControl(new JButton(createUndoImageAction()));
		addControl(new JButton(createRevertImageAction()));
	}

	/**
	 * Adds image information widget to bottom panel area (shows dimensions and
	 * channels of loaded image).
	 */
	protected void addImageInfo() {
		_ImageInfo = new JLabel();
		_LastWidth = -1;
		_LastHeight = -1;
		_LastChannel = -1;
		_ImageInfo.setHorizontalAlignment(SwingConstants.LEFT);
		addInfo(_ImageInfo);
	}

	/**
	 * Adds image information widget to bottom panel showing pixel coordinates.
	 */
	protected void addCursorInfo() {
		_PointerX = new JLabel("0");
		Dimension minSize = new Dimension(40, 0);
		_PointerX.setMinimumSize(minSize);
		_PointerX.setHorizontalAlignment(SwingConstants.RIGHT);
		_PointerY = new JLabel("0");
		_PointerY.setMinimumSize(minSize);
		_PointerY.setHorizontalAlignment(SwingConstants.RIGHT);
		JPanel cursor = new JPanel();
		new BoxLayout(cursor, BoxLayout.X_AXIS);
		JLabel xLabel = new JLabel("x:");
		xLabel.setMinimumSize(minSize);
		xLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		cursor.add(xLabel);
		cursor.add(_PointerX);
		cursor.add(new JLabel("y:"));
		cursor.add(_PointerY);
		addInfo(cursor);

		imageView.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				double xRatio = 0.0;
				double yRatio = 0.0;
				String channelStr = "  []";

				if (_Image != null) {
					int ih = _Image.rows();
					int h = imageView.getHeight();
					if (h > ih) {
						y -= (h - ih) / 2;
					}
					int iw = _Image.cols();

					// If mouse pointer over pixel in image, update ratio and
					// color
					if ((iw > 0) && (ih > 0) && (x < iw) && (y < ih)
							&& (y >= 0) && (x >= 0)) {
						xRatio = x / ((double) iw);
						yRatio = y / ((double) ih);
						double[] channelVals = _Image.get(y, x);
						channelStr = "  "
								+ Conversion.channelsToString(channelVals);
					}
				}
				String fmtStr = "%4d (%.3f)";
				_PointerX.setText(String.format(fmtStr, x, xRatio));
				_PointerY.setText(String.format(fmtStr, y, yRatio) + channelStr);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
	}

	/**
	 * Method which adds information items to the bottom status line.
	 * 
	 * <p>
	 * You can override this method if you want to add your own set of
	 * information items.
	 * </p>
	 */
	protected void addInfoItems() {
		addImageInfo();
		addCursorInfo();
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
	 * Creates a action to exit the application (System.exit(0) invocation).
	 * 
	 * @return Action that will cause program to terminate.
	 */
	protected Action createExitAction() {
		Action action = new AbstractAction("Exit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
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
				fit();
			}

		};
		return action;
	}
	
	/**
	 * Helper method to fix window for image loaded.
	 */
	private void fit() {
		if (_Image != null) {
			// Don't like the "magic guess" at the size adjustment to
			// prevent scroll bars from appearing
			int width = _Image.cols() + 3;
			int height = _Image.rows() + 3;
			Dimension size = new Dimension(width, height);
			imageScrollPane.setPreferredSize(size);
			frame.pack();
		}		
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
		addMenuItem(fileMenu, new JMenuItem(createOpenImageAction()));
		addMenuItem(fileMenu, new JMenuItem(createGrabImageAction()));
		addMenuItem(
				fileMenu,
				new JMenuItem(createBatchProcessAction("Batch Last Filter",
						null)));
		addMenuItem(fileMenu, new JMenuItem(createRevertImageAction()));
		addMenuItem(fileMenu, new JMenuItem(createSaveImageAction()));
		addMenuItem(fileMenu, new JMenuItem(createPreferencesAction()));
		addMenuItem(fileMenu, new JMenuItem(createFitAction()));
		addMenuItem(fileMenu, new JMenuItem(createExitAction()));

		String editName = "Edit";
		addMenuItem(editName, new JMenuItem(createUndoImageAction()));

		String colorSpaceName = "Color Space";
		JMenu colorSpace = new JMenu(colorSpaceName);
		addMenuItem(editName, colorSpace);
		colorSpace.add(createImageProcessingMenuItem("BGR->HSV",
				ColorSpace.createBGRtoHSV()));
		colorSpace.add(createImageProcessingMenuItem("HSV->BGR",
				ColorSpace.createHSVtoBGR()));
		colorSpace.add(createImageProcessingMenuItem("RGB->HSV",
				ColorSpace.createRGBtoHSV()));
		colorSpace.add(createImageProcessingMenuItem("HSV->RGB",
				ColorSpace.createHSVtoRGB()));
		colorSpace.add(createImageProcessingMenuItem("BGR->XYZ",
				ColorSpace.createBGRtoXYZ()));
		colorSpace.add(createImageProcessingMenuItem("XYZ->BGR",
				ColorSpace.createXYZtoBGR()));
		colorSpace.add(createImageProcessingMenuItem("RGB->XYZ",
				ColorSpace.createRGBtoXYZ()));
		colorSpace.add(createImageProcessingMenuItem("XYZ->RGB",
				ColorSpace.createXYZtoRGB()));

		addMenuItem(editName, new JMenuItem(getColorRangeAction()));

		addMenuItem(editName,
				createImageProcessingMenuItem("Negative", new Negative()));

		JMenu contrast = new JMenu("Contrast");
		addMenuItem(editName, contrast);
		double[] contrastVals = { 0.25, 0.5, .75, .9, 1.1, 1.25, 1.5, 2.0, 5.0,
				10.0 };
		for (int i = 0; i < contrastVals.length; i++) {
			double gain = contrastVals[i];
			contrast.add(createImageProcessingMenuItem("" + gain,
					new ContrastBrightness(gain, 0)));
		}

		JMenu brightness = new JMenu("Brightness");
		addMenuItem(editName, brightness);
		double[] brightnessVals = { -50, -25, -10, -5, -1, +1, +5, +10, +25,
				+50 };
		for (int i = 0; i < brightnessVals.length; i++) {
			double bias = brightnessVals[i];
			brightness.add(createImageProcessingMenuItem("" + bias,
					new ContrastBrightness(1.0, bias)));
		}

		colorSpace.add(createImageProcessingMenuItem("BGR->HSV",
				ColorSpace.createBGRtoHSV()));
		colorSpace.add(createImageProcessingMenuItem("HSV->BGR",
				ColorSpace.createHSVtoBGR()));
		colorSpace.add(createImageProcessingMenuItem("RGB->HSV",
				ColorSpace.createRGBtoHSV()));
		colorSpace.add(createImageProcessingMenuItem("HSV->RGB",
				ColorSpace.createHSVtoRGB()));

		String chanRemoveName = "Remove Channels";
		JMenu chanRemove = new JMenu(chanRemoveName);
		addMenuItem(editName, chanRemove);
		for (int i = 0; i < 3; i++) {
			JMenuItem item = createImageProcessingMenuItem("Channel " + i,
					new FillChannel(i));
			chanRemove.add(item);
		}

		JMenu blur = new JMenu("Blur");
		addMenuItem(editName, blur);
		for (int i = 1; i <= 10; i++) {
			String label = "" + i + "x" + i;
			blur.add(createImageProcessingMenuItem(label, new Blur(new Size(i,
					i))));
		}

		addMenuItem(editName,
				createImageProcessingMenuItem("Gray Scale", new GrayScale()));
		addMenuItem(
				editName,
				createImageProcessingMenuItem("Black & White", new BlackWhite()));

		JMenu erode = new JMenu("Erode");
		JMenu dilate = new JMenu("Dilate");
		addMenuItem(editName, dilate);
		addMenuItem(editName, erode);
		for (int i = 2; i <= 10; i++) {
			String label = "" + i + "x" + i;
			erode.add(createImageProcessingAction(label, new Erode(i)));
			dilate.add(createImageProcessingAction(label, new Dilate(i)));
		}

		addMenuItem(editName,
				createImageProcessingMenuItem("Contours", new Contours()));

		addMenuItem(editName,
				createImageProcessingMenuItem("Cross Hair", new CrossHair()));
		
		JMenu fovMenu = new JMenu("FOV");
		String fovPrefs = "defaultFov";
		addMenuItem(editName, fovMenu);

		FovOverlay fov = new FovOverlay(fovPrefs);
		fov.setVerticalVisible(false);
		fov.setHorizontalVisible(true);
		fovMenu.add(createImageProcessingAction("Horizontal (5 deg)", fov));

		fov = new FovOverlay(fovPrefs);
		fov.setDegSpacing(1);
		fov.setLineColor(new Scalar(80, 80, 50));
		fov.setTextColor(null);
		fov.setVerticalVisible(false);
		fov.setHorizontalVisible(true);
		fovMenu.add(createImageProcessingAction("Horizontal (1 deg)", fov));

		fov = new FovOverlay(fovPrefs);
		fov.setVerticalVisible(true);
		fov.setHorizontalVisible(false);
		fovMenu.add(createImageProcessingAction("Vertical (5 deg)", fov));

		fov = new FovOverlay(fovPrefs);
		fov.setDegSpacing(1);
		fov.setLineColor(new Scalar(80, 80, 50));
		fov.setTextColor(null);
		fov.setVerticalVisible(true);
		fov.setHorizontalVisible(false);
		fovMenu.add(createImageProcessingAction("Vertical (1 deg)", fov));
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
	 * Adds a new top level menu item with a set of filter representing each
	 * stage of a sequence.
	 * 
	 * @param label
	 *            Label for menu item to add to menu bar.
	 * @param seqFilter
	 *            A {@link Sequence} filter with at least one stage added.
	 */
	protected void addSequence(String label, Sequence seqFilter) {
		int n = seqFilter.steps();
		for (int i = 0; i <= n; i++) {
			String stepLabel = "Stage " + i;
			MatFilter filter = seqFilter.createStepFilter(i);
			JMenuItem menuItem = createImageProcessingMenuItem(stepLabel, filter, true);
			addMenuItem(label, menuItem);
		}
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

		String fname = _Config.getLastSavedFile(null);
		File f = (fname != null) ? new File(fname) : lastFile;

		// Ask user for the location of the image file
		if (f != null) {
			fileChooser.setSelectedFile(f);
		}
		if (fileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// Load the image
		File imgFile = fileChooser.getSelectedFile();
		String path = imgFile.getAbsolutePath();
		Highgui.imwrite(path, _Image);
		_Config.setLastSavedFile(path);
	}

	/**
	 * Prompts user to select a image file and then loads it in.
	 *
	 * @return The image that was selected by the user and loaded into the tool,
	 *         or {@code null} if the user did not load in a image.
	 */
	private Mat openImage() {

		// Ask user for the location of the image file
		if (lastFile != null) {
			fileChooser.setSelectedFile(lastFile);
		}
		if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		// Load the image
		File imgFile = fileChooser.getSelectedFile();
		return openImage(imgFile);
	}

	/**
	 * Attempts to load in the image from the file specified.
	 *
	 * @param imgFile
	 *            The file you want to load into the tool (must not be
	 *            {@code null}).
	 * @return The image that was loaded into the tool, or {@code null} if
	 *         unable to load the image (missing file, not image file, etc).
	 */
	protected Mat openImage(File imgFile) {
		final String path = imgFile.getAbsolutePath();
		final Mat newImage = Highgui.imread(path);
		if ((newImage != null) && (newImage.width() > 0) && (newImage.height() > 0)) {
			_Config.setLastOpenedFile(path);
			lastFile = imgFile;
			_LastLoadedImage = newImage;
			setImage(newImage);
			return newImage;
		} else {
			showMessageDialog(frame, "Cannot open image file: " + path,
					frame.getTitle(), ERROR_MESSAGE);
			return null;
		}
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
			imageScrollPane = new JScrollPane(imageView);
			imageScrollPane.setPreferredSize(new Dimension(640, 480));
			frame.add(imageScrollPane, BorderLayout.CENTER);

			// Controls on the left
			addControls();
			final JPanel leftPane = new JPanel();
			leftPane.add(sidePanel);
			frame.add(leftPane, BorderLayout.WEST);

			addInfoItems();
			frame.add(botPanel, BorderLayout.SOUTH);
		}
		return frame;
	}

	/**
	 * After you are done initializing your implementation, call this method to
	 * show the GUI tool.
	 */
	public final void main() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

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

				_Url = _Config.getImageUrl(_Url);

				String lastFile = _Config.getLastOpenedFile(null);
				if (lastFile != null) {
					openImage(new File(lastFile));
				}
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
		new FilterToolGuiOpenCv("Test").main();
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
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		options.add(new JLabel("URL of image to grab:"));

		final JTextField url;
		url = new JTextField(_Url);
		url.setToolTipText("Enter the URL of your robot's camera (like: http://10.8.68.11/jpg/1/image.jpg)");
		options.add(url);
		url.getDocument().addDocumentListener(new DocumentListener() {

			private void updateValue(DocumentEvent e) {
				_Url = url.getText();
				_Config.setImageUrl(_Url);
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

		return options;
	}

	/**
	 * Builds a action handler to displays a color range editor to allow the
	 * user to quickly see the impact of adjusting the color ranges.
	 *
	 * @return Action that can be assigned to a button or menu item.
	 */
	private Action getColorRangeAction() { //TODO save values
		if (_ColorRange == null) {
			int[] minVals = { 0, 0, 0 };
			int[] maxVals = { 255, 255, 255 };
			_ColorRange = new ColorRange(minVals, maxVals, true);
			_ColorRange.addListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					// If some other filter applied, then reset our base image
					// for applying
					// color range tweaks
					if (_LastColorRangeImage != _Image) {
						_ColorRangeImage = _Image.clone();
					}
					_LastColorRangeImage = _ColorRange.process(_ColorRangeImage
							.clone());
					setImage(_LastColorRangeImage);
				}
			});
		}

		Action action = new AbstractAction("Color Range") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// When button is pressed, get copy of current image as base
				// and then display GUI tool to dynamically update the color
				// range
				_ColorRangeImage = getImage().clone();
				_LastColorRangeImage = _ColorRange.process(getImage().clone());
				setImage(_LastColorRangeImage);
				JFrame frame = new JFrame("Color Range");
				frame.setMinimumSize(new Dimension(480, 200));
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setContentPane(_ColorRange.createPreferencesPanel());
				frame.pack();
				frame.setVisible(true);
			}
		};
		return action;
	}
	
	protected JFrame getFrame() {
		return frame;
	}
	
	protected ColorRange getColorRange() {
		return _ColorRange;
	}

}
