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

        //Csak proba:
        try {
            // Az adat, amit át akarunk adni a Python scriptnek
            String inputData = "5"; // Példa adat

            // A ProcessBuilder létrehozása a Python script meghívásához
            ProcessBuilder processBuilder = new ProcessBuilder("C:/Users/csegz/Desktop/Egyetem/2024-2025 II. felev/Onlab/PythonSample/venv/Scripts/python.exe", "src/main/resources/pythonScripts/script.py");

            // A Python script futtatása
            Process process = processBuilder.start();
//
//            OutputStream outputStream = process.getOutputStream();
//            outputStream.write(bytes); // Byte adat küldése a Python szkriptnek
//            outputStream.flush(); // Az adat tényleges küldése
            // outputStream.close(); // Az output stream bezárása


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
                    if (offset == 3) {
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

        List<Map<String, Object>> facesCoordinates = new ArrayList<>();

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
                        "height", ymax - ymin)
                );
        }

//        for (Detection detection : detections) {
//                System.out.println("Detected: " + detection); // csak logolas
//                facesCoordinates.add(Map.of(
//                        "x", (int)detection.getBbox()[0],
//                        "y", (int)detection.getBbox()[1],
//                        "width", (int)detection.getBbox()[2],
//                        "height", (int)detection.getBbox()[3])
//                );
//            }


//        //Pythonos rész:
//        ProcessBuilder processBuilder = new ProcessBuilder("python", "C:/Users/csegz/Desktop/Egyetem/2024-2025 II. felev/Onlab/facerecproject/facerecproject_backend/src/main/resources/pythonScripts/__init__.py");
//        Process process = processBuilder.start();

//        OutputStream os = process.getOutputStream();
//        // Először küldjük a kép méretét (4 bájt)
//        os.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
//
//        // Majd küldjük a tényleges kép bájtjait
//        os.write(bytes);
//        os.flush();
//        os.close();





//        ImageClassificationTranslator translatorNew =
//                ImageClassificationTranslator.builder()
//                        .addTransform(new Resize(224, 224))
//                        .addTransform(new ToTensor())
//                        .build();
//
//        Translator<Image, DetectedObjects> translatorNew2 = YoloV8Translator.builder()
//                .addTransform(new Resize(224, 224))
//                .addTransform(new ToTensor())
//                .build();
//
//        Criteria<Image, DetectedObjects> criteria =
//                Criteria.builder()
//                        .setTypes (Image.class, DetectedObjects.class)
//                        .optModelPath(Paths.get("C:/Users/csegz/Desktop/Egyetem/2024-2025 II. felev/Onlab/facerecproject/facerecproject_backend/YOLOv8-Face-Detection/"))
//                        .optModelName("model.pt")
//                        //.optEngine("OnnxRuntime")
//                        .optEngine("PyTorch")
//                        .optTranslator(translatorNew2)
//                        .optProgress (new ProgressBar())
//                        .build();
//
//
//
//        try (ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);
//             Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
//            DetectedObjects result = predictor.predict(img);
//            System.out.println(result);
//    }




//
//        System.out.println(file.getOriginalFilename());
//        System.out.println(file.getContentType());
//        String format = file.getContentType();
//
//        // Modell betöltése
//        Path modelPath = Paths.get("src/main/resources/models/yolov8_face_detection_model_compatible.onnx");  // Az ONNX modell fájlja
//        Model model = Model.newInstance(modelPath.toString());
//
//        // Kép betöltése
//        //Image img = ImageFactory.getInstance().fromFile(Paths.get("/path/to/image.jpg"));  // Betöltés
//
//        Image img = null;
//
//        byte[] bytes = file.getBytes();
//        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
//            // Kép betöltése ByteArrayInputStream segítségével
//            img = ImageFactory.getInstance().fromInputStream(byteArrayInputStream);
//            System.out.println("Kép sikeresen betöltve.");
//        } catch (IOException e) {
//            System.err.println("Hiba a fájl beolvasásakor: " + e.getMessage());
//        }
//
////        try (InputStream inputStream = file.getInputStream()) {
////            BufferedImage bufferedImage = ImageIO.read(inputStream);
////            if (bufferedImage == null) {
////                System.out.println("Image is null");
////                throw new RuntimeException();
////            }
////            // Betöltjük a képet az InputStream-ból
////            img =  ImageFactory.getInstance().fromInputStream(inputStream);
////        }
//        NDManager manager = NDManager.newBaseManager();
//        NDArray arrayFromImg = Objects.requireNonNull(img).toNDArray(manager);
//
//        // Translator létrehozása (ez konvertálja a bemeneti adatokat és kimenetet)
//        Translator<Image, List<Detection>> translator = new DetectionTranslator();
//
//        // Prediktor létrehozása
//        try (Predictor<Image, List<Detection>> predictor = model.newPredictor(translator)) {
//            // Predikció végrehajtása
//            List<Detection> detections = predictor.predict(img);
//
//            //Az eddigiekhez illeszkedo visszateresi ertek megkonstrulasa
//
//            // Detektált objektumok feldolgozása
//            for (Detection detection : detections) {
//                System.out.println("Detected: " + detection); // csak logolas
//                facesCoordinates.add(Map.of(
//                        "x", (int)detection.getBbox()[0],
//                        "y", (int)detection.getBbox()[1],
//                        "width", (int)detection.getBbox()[2],
//                        "height", (int)detection.getBbox()[3])
//                );
//            }
//        }

        return facesCoordinates;
    }
}
