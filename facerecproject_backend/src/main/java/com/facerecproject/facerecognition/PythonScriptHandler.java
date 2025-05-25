package com.facerecproject.facerecognition;

import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonScriptHandler {
    private static Process process;

    public static Process startPythonScript(String pythonScript) {
        try {
            String path = ".venv/Scripts/python.exe"; //Nem hardcodeolt, ez így a jó
            //Az "-u" arra van, hogy a python kimenete azonnal kiíródjon (unbuffered mód), mert másképp bufferelődik és nem írja ki azonnal
            //ezzel debugolható a működés, tudjuk, hogy helyesen működik most

            ProcessBuilder pbProcess = new ProcessBuilder(path, "-u", "src/main/resources/pythonScripts/" + pythonScript);
            pbProcess.redirectErrorStream(true);
            process = pbProcess.start();

            // Kimenet kiolvasása külön szálban - ez is inkább csak debug miatt kell
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON] " + line);
                    }
                } catch (IOException e) {
                    System.err.println("❌ Hiba a Python kimenet olvasásakor: " + e.getMessage());
                }
            }).start();

            System.out.println("[JAVA] startPythonScript: Python WebSocket szerver inicializáció elindult.");
        } catch (IOException e) {
            System.err.println("Hiba a Python script indításakor: " + e.getMessage());
        }
        return process;

    }

    @PreDestroy
    public static void stopPythonScripts() {
         if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
