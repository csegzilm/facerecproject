package com.facerecproject.facerecognition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class PythonScriptHandler {
    private static Process emotionProcess, ageProcess, genderProcess;

    public static void startPythonScripts() {
        try {
            String path = "C:/Users/lorik/Desktop/Egyetem/6_2024_25_II_felev/Onlab/python proba/.venv/Scripts/python.exe";
            ProcessBuilder pbEmotion = new ProcessBuilder(path, "-u", "src/main/resources/pythonScripts/script_with_websocket.py"); //TODO: Még nincsenek ezek a fájlok megadva, létrehozva
            //Az "-u" arra van, hogy a python kimenete azonnal kiíródjon (unbuffered mód), mert másképp bufferelődik és nem írja ki azonnal
            //ezzel debugolható a működés, tudjuk, hogy helyesen működik most

            // ProcessBuilder pbAge = new ProcessBuilder(path, "scriptAge.py");
            // ProcessBuilder pbGender = new ProcessBuilder(path, "scriptGender.py");
            pbEmotion.redirectErrorStream(true); // stdout és stderr összevonása (csak a szebb kimenet miatt)

            // Ha relatív az útvonal, beállíthatjuk a working directoryt - majd a tisztításkor
            //pb.directory(new java.io.File("src/main/resources/pythonScripts"));

            emotionProcess = pbEmotion.start();
            // ageProcess = pbAge.start();
            // genderProcess = pbGender.start();


            // Kimenet kiolvasása külön szálban - ez is inkább csak debug miatt kell
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(emotionProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON] " + line);
                    }
                } catch (IOException e) {
                    System.err.println("❌ Hiba a Python kimenet olvasásakor: " + e.getMessage());
                }
            }).start();

            System.out.println("[JAVA] startPythonScripts: Python WebSocket szerver elindult.");

        } catch (IOException e) {
            System.err.println("Hiba a Python script indításakor: " + e.getMessage());
        }
    }

    public static void stopPythonScripts() {
        if (emotionProcess != null && emotionProcess.isAlive() ||
                ageProcess != null && ageProcess.isAlive() ||
                genderProcess != null && genderProcess.isAlive()) {
            emotionProcess.destroy();
            ageProcess.destroy();
            genderProcess.destroy();
        }
    }
}
