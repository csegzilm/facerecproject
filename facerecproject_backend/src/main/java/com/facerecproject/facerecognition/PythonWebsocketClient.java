package com.facerecproject.facerecognition;

import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static com.facerecproject.facerecognition.PythonScriptHandler.startPythonScript;

@ClientEndpoint
public class PythonWebsocketClient {

    private Session session;
    private Consumer<String> callback;
    Process scriptProcess;

    public PythonWebsocketClient(String uri, String pythonScriptName) {
        scriptProcess = startPythonScript(pythonScriptName);
        connectWithRetry(uri, 10, 1000); // max 10 próbálkozás, 1s várakozás
    }

    private void connectWithRetry(String uri, int maxRetries, int delayMs) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, URI.create(uri));
                System.out.println("[JAVA] SIKER! Kapcsolódva a Python szerverhez.");
                return;
            } catch (Exception e) {
                attempts++;
                System.out.println("[JAVA] --- Python szerver még nem elérhető... próbálkozás: " + attempts);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.err.println("[JAVA] Nem sikerült kapcsolódni a Python WebSocket szerverhez.");
    }

    public void sendImage(String base64Image, Consumer<String> onResponse) {
        this.callback = onResponse;
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(base64Image);
        } else {
            System.err.println("[JAVA] WebSocket kapcsolat nincs nyitva a Python felé.");
        }
    }

    public void sendBinary(byte[] bytes, Consumer<String> onResponse) {
        this.callback = onResponse;
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendBinary(ByteBuffer.wrap(bytes));
        } else {
            System.err.println("[JAVA] WebSocket nincs nyitva a Python felé.");
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (callback != null) callback.accept(message);
        System.out.println("Received message with length: " + message.length());
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("[JAVA] Java kapcsolat létrejött a Python WebSocket szerverrel.");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[JAVA] Python WebSocket hiba: " + throwable.getMessage());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("[JAVA] Python WebSocket kapcsolat lezárva: " + closeReason);
    }

    @PreDestroy
    public void preDestroy() {
        if (scriptProcess != null && scriptProcess.isAlive()) {
            scriptProcess.destroy();
        }
    }


}

