package com.facerecproject.facerecognition;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FaceDetectionService {
    // Inspiration, literature: https://docs.opencv.org/3.4/db/d28/tutorial_cascade_classifier.html

    //Needs to be set via the IntelliJ settings! - see yt
    // Since OpenCV was originally made in C++, first it has to load in the native C++ libraries
    static {
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        nu.pattern.OpenCV.loadLocally();
    }

    public static void detectFaces(MultipartFile file) throws IOException {
        //CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
        // String haarcascadePath = FaceDetectionService.class.getClassLoader().getResource("haarcascade_frontalface_alt.xml").getPath();

        System.out.println(Core.VERSION); //Proba, hogy az opencv jol lett inicializalva, es marpedig jol lett

        CascadeClassifier faceDetector = new CascadeClassifier();
        faceDetector.load("C:\\Users\\csegz\\Desktop\\Egyetem\\2024-2025 II. felev\\Onlab\\facerecproject\\facerecproject_backend\\haarcascade_frontalface_alt.xml");
        byte[] bytes = file.getBytes();
        Mat image = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_COLOR);

        MatOfRect faceDetections = new MatOfRect(); //Stores multiple rectangles, easy to turn into an array
        faceDetector.detectMultiScale(image, faceDetections); // Puts the detected faces into 'faceDetections'

        System.out.println("Talált arcok száma: " + faceDetections.toArray().length);
    }
}
