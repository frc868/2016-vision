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
