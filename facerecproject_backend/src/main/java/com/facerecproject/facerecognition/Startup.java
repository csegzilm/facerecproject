/*package com.facerecproject.facerecognition;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        new Thread(PythonScriptHandler::startPythonScript).start();
    }


    @PreDestroy
    public void onShutdown() {
        PythonScriptHandler.stopPythonScripts();
    }
}
*/


