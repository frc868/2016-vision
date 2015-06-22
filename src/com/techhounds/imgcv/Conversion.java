/*
 * Copyright (c) 2013, pkb
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * A collection of static methods to facilitate working with opencv's matrix
 * image representation and Java's image representation.
 *
 * @author pkb
 */
public final class Conversion {

    /**
     * Prevent creating instances of this class (this class is only intended to
     * provide static methods).
     */
    private Conversion() {

    }

    /**
     * Converts a Java {@link BufferedImage} object into a opencv {@link Mat}
     * object.
     *
     * @param bimg The Java image object to be converted.
     * @return The opencv {@link Mat} representation of the image.
     * @throws IOException if a internal problem occurs when storing image to
     * memory.
     */
    public static Mat bufferedImageToMat(BufferedImage bimg) throws IOException {
        // Surely there has to be a better way to do this!
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String ext = "bmp";
        ImageIO.write(bimg, ext, baos);
        baos.flush();
        MatOfByte data = new MatOfByte(baos.toByteArray());
        baos.close();
        return Highgui.imdecode(data, Highgui.CV_LOAD_IMAGE_COLOR);
    }

    /**
     * Converts a gray scale (1 channel) opencv {@link Mat} image representation
     * to a Java {@link BufferedImage} representation.
     *
     * @param cvimg The opencv {@link Mat} image to be converted (must be a 1
     * channel image).
     * @return A {@link BufferedImage} version of the original image passed in.
     */
    public static BufferedImage cvGrayToBufferedImage(Mat cvimg) {
        int rows = cvimg.rows();
        int cols = cvimg.cols();
        int channels = cvimg.channels();

        if ((channels != 1) || (cvimg.type() != CvType.CV_8U)) {
            throw new IllegalArgumentException("Image must be 1 channel 8 bits per value (gray scale)");
        }

        byte[] data = new byte[rows * cols * channels];
        cvimg.get(0, 0, data);

        BufferedImage bimg = new BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY);
        bimg.getRaster().setDataElements(0, 0, cols, rows, data);
        return bimg;
    }

    /**
     * Converts a BGR (3 channel) opencv {@link Mat} image representation to a
     * Java {@link BufferedImage} representation.
     *
     * @param cvimg The opencv {@link Mat} image to be converted (must be a 3
     * channel image).
     * @return A {@link BufferedImage} version of the original image passed in.
     */
    public static BufferedImage cvBgrToBufferedImage(Mat cvimg) {
        int rows = cvimg.rows();
        int cols = cvimg.cols();
        int channels = cvimg.channels();

        if ((channels != 3) || (cvimg.type() != CvType.CV_8UC3)) {
            throw new IllegalArgumentException("Image must be 3 channel 8 bits per value (BGR)");
        }

        byte[] data = new byte[rows * cols * channels];
        cvimg.get(0, 0, data);

        BufferedImage bimg = new BufferedImage(cols, rows, BufferedImage.TYPE_3BYTE_BGR);
        bimg.getRaster().setDataElements(0, 0, cols, rows, data);
        return bimg;
    }

    /**
     * Converts a RGB (3 channel) opencv {@link Mat} image representation to a
     * Java {@link BufferedImage} representation.
     *
     * @param cvimg The opencv {@link Mat} image to be converted (must be a 3
     * channel image).
     * @return A {@link BufferedImage} version of the original image passed in.
     */
    public static BufferedImage cvRgbToBufferedImage(Mat cvimg) {
        int rows = cvimg.rows();
        int cols = cvimg.cols();
        int channels = cvimg.channels();

        if ((channels != 3) || (cvimg.type() != CvType.CV_8UC3)) {
            throw new IllegalArgumentException("Image must be 3 channel 8 bits per value (RGB)");
        }

        Mat bgr = cvimg.clone();
        Imgproc.cvtColor(cvimg, bgr, Imgproc.COLOR_RGB2BGR);
        byte[] data = new byte[rows * cols * channels];
        bgr.get(0, 0, data);
        BufferedImage bimg = new BufferedImage(cols, rows, BufferedImage.TYPE_3BYTE_BGR);
        bimg.getRaster().setDataElements(0, 0, cols, rows, data);
        return bimg;
    }

    /**
     * Attempts to read in a image as a {@link BufferedImage} from an URL.
     *
     * @param url The URL (like: "http://10.8.68.11/jpg/1/image.jpg") to read the
     * image from.
     * @return The image as a {@link BufferedImage} object.
     * @throws IOException If we are unable to retrieve the image from the URL.
     */
    public static BufferedImage urlToBufferedImage(String url) throws IOException {
        return ImageIO.read(new URL(url));
    }

    /**
     * Attempts to read in a image as an opencv {@link Mat} from an URL.
     *
     * @param url The URL (like: "http://10.8.68.11/jpg/1/image.jpg") to read the
     * image from.
     * @return The image as a {@link Mat} object.
     * @throws IOException If we are unable to retrieve the image from the URL.
     */
    public static Mat urlToMat(String url) throws IOException {
        BufferedImage bimg = urlToBufferedImage(url);
        return bufferedImageToMat(bimg);
    }

}
