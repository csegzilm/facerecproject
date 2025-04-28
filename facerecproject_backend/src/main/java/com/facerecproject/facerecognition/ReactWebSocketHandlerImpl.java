package com.facerecproject.facerecognition;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ReactWebSocketHandlerImpl extends TextWebSocketHandler {

    private final PythonWebsocketClient pythonClient = new PythonWebsocketClient("ws://localhost:8765");

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // WebSocket kapcsolat létrejöttekor
        System.out.println("WebSocket kapcsolat létrejött!");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Az üzenet beérkezésekor
        System.out.println("Kapott üzenet: " + message.getPayload());

        // Ha a bejövő üzenet egy base64-es kép
        if (message.getPayload().startsWith("data:image")) {
            String img = message.getPayload();
            pythonClient.sendImage(img, (String response) -> {
                try {
                    session.sendMessage(new TextMessage(response));
                } catch (Exception e) {
                    System.err.println("❌ Nem működött a válasz a frontendnek: " + e.getMessage());
                }
            });
        }

        // Válasz küldése JSON üzenettel
        session.sendMessage(new TextMessage("{\"status\": \"success\", " +
                "                                     \"message\": \"Testszoveg received\"}"));
    }

   /* @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        ByteBuffer buffer = message.getPayload();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        //továbbküldés a pythonnak
        pythonClient.sendBinary(bytes, (String response) -> {
            try {
                session.sendMessage(new TextMessage(response));
            } catch (Exception e) {
                System.err.println("❌ Nem működött a válasz a frontendnek: " + e.getMessage());
            }
        });

        // Válasz küldése JSON üzenettel
        try {
            session.sendMessage(new TextMessage("{\"status\": \"success\", " +
                    "                                     \"message\": \"Testszoveg received\"}"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }*/

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            ByteBuffer buffer = message.getPayload();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            System.out.println("✅ Bináris üzenetet kaptam: méret = " + bytes.length + " byte");

            //itt még nem küldjük tovább, csak válaszolunk
            // session.sendMessage(new TextMessage("{\"status\": \"ok\", \"info\": \"binary received\"}"));

            //továbbküldés a pythonnak
            pythonClient.sendBinary(bytes, (String response) -> {
                try {
                    session.sendMessage(new TextMessage(response));
                } catch (Exception e) {
                    System.err.println("❌ Nem működött a válasz a frontendnek: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Hiba bináris üzenet kezelés közben: " + e.getMessage());
        }
    }

}
