package com.facerecproject.facerecognition;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.ByteBuffer;

public class ReactWebSocketHandlerImpl extends TextWebSocketHandler {

    // private final PythonWebsocketClient pythonClient = new PythonWebsocketClient("ws://localhost:8765");

    private final PythonWebsocketClient pythonClientAge = new PythonWebsocketClient("ws://localhost:8766", "scriptAge.py");
    private final PythonWebsocketClient pythonClientEmotion = new PythonWebsocketClient("ws://localhost:8767", "scriptEmotion.py");
    private final PythonWebsocketClient pythonClientGender = new PythonWebsocketClient("ws://localhost:8768", "scriptGender.py");

    private boolean isEmotionBusy = false;
    private boolean isAgeBusy = false;
    private boolean isGenderBusy = false;

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
            pythonClientAge.sendImage(img, (String response) -> {
                try {
                    session.sendMessage(new TextMessage(response));
                } catch (Exception e) {
                    System.err.println("[JAVA] Nem működött a válasz a frontendnek: " + e.getMessage());
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
                System.err.println("[JAVA] Nem működött a válasz a frontendnek: " + e.getMessage());
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

            System.out.println("[JAVA] Bináris üzenetet érkezett: méret = " + bytes.length + " byte");

            //itt még nem küldjük tovább, csak válaszolunk
            // session.sendMessage(new TextMessage("{\"status\": \"ok\", \"info\": \"binary received\"}"));

            //továbbküldés a pythonnak
            if (!isAgeBusy) {
                isAgeBusy = true;
                pythonClientAge.sendBinary(bytes, (String response) -> {
                    System.out.println("[JAVA] Válasz jött a pythonClientAgetől.");
                    isAgeBusy = false;
                    try {
                        session.sendMessage(new TextMessage(response));
                    } catch (Exception e) {
                        System.err.println("[JAVA] Nem működött a válasz a frontendnek: " + e.getMessage());
                    }
                });
            }

            if (!isEmotionBusy) {
                isEmotionBusy = true;
                pythonClientEmotion.sendBinary(bytes, (String response) -> {
                    System.out.println("[JAVA] Válasz jött a pythonClientEmotiontől.");
                    isEmotionBusy = false;
                    try {
                        session.sendMessage(new TextMessage(response));
                    } catch (Exception e) {
                        System.err.println("[JAVA] Nem működött a válasz a frontendnek: " + e.getMessage());
                    }
                });
            }

            if (!isGenderBusy) {
                isGenderBusy = true;
                pythonClientGender.sendBinary(bytes, (String response) -> {
                    System.out.println("[JAVA] Válasz jött a pythonClientGendertől.");
                    isGenderBusy = false;
                    try {
                        session.sendMessage(new TextMessage(response));
                    } catch (Exception e) {
                        System.err.println("[JAVA] Nem működött a válasz a frontendnek: " + e.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("[JAVA] Hiba bináris üzenet kezelés közben: " + e.getMessage());
        }
    }

}
