package com.facerecproject.facerecognition;

import ai.djl.ndarray.NDArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Detection {
    private float confidence;
    private float[] bbox; // Bounding box [x1, y1, width, height]
    private String label;

    // Konstruktorok, getterek, setterek
    public Detection(float confidence, float[] bbox, String label) {
        this.confidence = confidence;
        this.bbox = bbox;
        this.label = label;
    }

    public static List<Detection> fromNDArray(NDArray array) {
        // Kimenet feldolgozása (például a bounding box-ok és confidencák kiolvasása)
        List<Detection> detections = new ArrayList<>();

        // A kimenet feldolgozása és Detection objektumok készítése
        // Képzeljük el, hogy a kimenet tartalmazza a bounding box-okat és a confidence értékeket

        return detections;
    }

    public float[] getBbox() {
        return bbox;
    }

    @Override
    public String toString() {
        return "Detection{" +
                "confidence=" + confidence +
                ", bbox=" + Arrays.toString(bbox) +
                ", label='" + label + '\'' +
                '}';
    }
}
