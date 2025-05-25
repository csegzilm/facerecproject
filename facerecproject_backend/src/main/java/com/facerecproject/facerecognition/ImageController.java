package com.facerecproject.facerecognition;

import ai.djl.ModelException;
import ai.djl.translate.TranslateException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.facerecproject.facerecognition.Yolov8FaceDetectionService.detectFaces;

//import static com.facerecproject.facerecognition.FaceDetectionService.detectFaces;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    @PostMapping("/upload")     //the uploading of the image happens at the /api/images/upload endpoint
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException, ModelException, TranslateException {
        if (file.isEmpty()) {
            Map<String, Object> errorResponse = Map.of("error", "File cannot be empty.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        InputStream is = file.getInputStream();
        List<Map<String, Object>> facesCoordinates = detectFaces(file);

        return ResponseEntity.ok(Map.of(
                "message", "Picture uploaded: " + file.getOriginalFilename(),
                "faceCount", facesCoordinates.size(),
                "facesCoordinates", facesCoordinates));
    }
}
