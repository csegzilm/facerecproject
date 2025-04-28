package com.facerecproject.facerecognition;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.modality.cv.translator.YoloV8Translator;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import ai.djl.translate.Translator;

import ai.djl.ndarray.NDArray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.util.*;

public class Yolov8FaceDetectionService {

    private static final Logger logger = LogManager.getLogger(Yolov8FaceDetectionService.class);

    public static List<Map<String, Object>> detectFaces(MultipartFile file) throws IOException, ModelException, TranslateException
    {
        byte[] bytes = file.getBytes();
        Image img = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            // Kép betöltése ByteArrayInputStream segítségével
            img = ImageFactory.getInstance().fromInputStream(byteArrayInputStream);
            System.out.println("Kép sikeresen betöltve.");
        } catch (IOException e) {
            System.err.println("Hiba a fájl beolvasásakor: " + e.getMessage());
        }

        StringBuilder scriptOutput = new StringBuilder();

        try {
            // Az adat, amit át akarunk adni a Python scriptnek
            // String inputData = "5"; // Példa adat

            // A ProcessBuilder létrehozása a Python script meghívásához
            ProcessBuilder processBuilder = new ProcessBuilder("C:/Users/lorik/Desktop/Egyetem/6_2024_25_II_felev/Onlab/python proba/.venv/Scripts/python.exe", "src/main/resources/pythonScripts/script.py");

            // A Python script futtatása
            Process process = processBuilder.start();

            try (OutputStream os = process.getOutputStream()) {
                os.write(bytes); // imageData egy byte tömb
                os.flush();  // Fontos! Ezzel biztosítod, hogy az adatok tényleg átmennek
            }

            // Az eredmény kiolvasása a standard kimenetről (stdout)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                int offset = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python output: " + line);
                    if (offset == 4) {
                        scriptOutput.append(line);
                    }
                    offset++;
                }

                while ((line = errorReader.readLine()) != null) {
                    System.err.println("Python error: " + line);
                }
            }
            // Várakozás a process befejezésére
            int exitCode = process.waitFor();
            System.out.println("Python script befejeződött, kilépési kód: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(scriptOutput.toString());
        JSONArray boundingBoxes = jsonObject.getJSONArray("bounding_boxes");
        JSONArray genders = jsonObject.getJSONArray("genders");
        JSONArray emotions = jsonObject.getJSONArray("emotions");
        JSONArray races = jsonObject.getJSONArray("races");



        List<Map<String, Object>> facesCoordinates = new ArrayList<>(); // amúgy mostmár nem csak a faces

        for (int i = 0; i < boundingBoxes.length(); i++) {
            JSONArray box = boundingBoxes.getJSONArray(i);
            int xmin = box.getInt(0);
            int ymin = box.getInt(1);
            int xmax = box.getInt(2);
            int ymax = box.getInt(3);

            facesCoordinates.add(Map.of(
                        "x", xmin,
                        "y", ymin,
                        "width", xmax - xmin,
                        "height", ymax - ymin,
                        "gender", genders.getString(i),
                        "emotion", emotions.getString(i),
                        "race", races.getString(i))
                );
        }

        return facesCoordinates;
    }
}
