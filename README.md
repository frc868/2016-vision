This project is designed to allow one to quickly create, test and adjust image processing filters based on OpenCV operations.

* Create a new MatFilter implementation (typically composed of several other filters).
* Create a new Tool to test your filter on an individual frame basis.
* Create a new View to test your filter on a stream of images.

To run under Eclipse, you will need to define a User Library named OpenCV-2 and add the OpenCV JAR file to it (the native OpenCV libraries also need to be available).

To run under NetBeans, you need to define an OpenCv library.

Several example filters (like FindStanchion2015) can be found under the com.techhounds.imgcv.filters package. These custom filters are designed to look for certain objects (if your test images don't contain the objects the results are often black or identical to the source).

Several tools that are useful to examine/process images step by step are available in the classes under the com.techhounds.imgcv.tools package. Try StanchionTool2015 as a starting point.

When building custom filters, to locate objects, try starting with:

* Crop (reduce pixels to process)
* Blur (smooth out colors)/BGR->HSV (work in HSV color space)
* Select by color range (reds are tricky)
* Convert to gray scale
* Convert to black and white
* Dialate and/or Erode
* Search for contours
* Convert contours to polygons
* Apply constraints to your polygons (bounding box, aspect ratio, min/max size, number of edges, etc)


IP Camera Video Feeds

Getting the live streams working can be tricky. Even if your still image grabbing is working fine, you may find that getting a live stream from a IP camera to work to be difficult. Here are some of the common things that you will run across:

* Not knowing the required form of the URL required by your IP camera to get a live feed. We have a list of a couple of cameras below. You will typically need to refer to your IP camera's documentation or web interface (and typically Google) to determine this URL. The VLC program can be useful in verifying that you've determined the correct URL (if you can't connect to it with VLC, it is unlikely you will be able to do it here).

* Not having the OpenCV "bin" directory in your PATH. This typically shows up on Windows systems. You need to find: Advanced System Settings from your System settings. This bings up the "System Properties" page to the "Advanced" tab. From there, you need to select the "Environment Variables" button and add (or edit if it exists) a PATH variable to the User Variables list. Set the value of PATH to something like the following (you will need to change the directory based on where you installed OpenCV):

%PATH%;C:\Users\YOUR_LOGIN\java-libs\opencv\build\x64\vc12\bin

Following URLs have been successfully used (note factory default passwords are shown):

Axis M1103
  rtsp://10.8.68.11:554/axis-media/media.amp?videocodec=h264

Edimax IC-3100W
  http://192.168.1.25/mjpg/video.mjpg (less lag than h264)
  rtsp://admin:1234@192.168.1.25/ipcam_h264.sdp?tcp
  
Shield (Rosewell?)	SmartCam HD & Pan
  rtsp://admin:123456@cayenne.linux.bogus/
  

Image (not video feed) URLs

Axis M1103
  http://10.8.68.11/jpg/1/image.jpg (may need to configure)

Edimax IC-3100W
  http://192.168.1.25/image.jpg (set under Network|LoginFree menu)
  
Shield (Rosewell?)	SmartCam HD & Pan
  rtsp://admin:123456@cayenne.linux.bogus/

