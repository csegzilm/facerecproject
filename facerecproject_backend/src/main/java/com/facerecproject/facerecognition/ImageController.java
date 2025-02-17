package com.facerecproject.facerecognition;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.facerecproject.facerecognition.FaceDetectionService.detectFaces;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    @PostMapping("/upload")     //the uploading of the image happens at the /api/images/upload endpoint
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty.");
        }

        detectFaces(file);
        // System.out.println(file.getOriginalFilename());

        return ResponseEntity.ok("Picture uploaded: " + file.getOriginalFilename());
    }
}
