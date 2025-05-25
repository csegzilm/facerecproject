package com.facerecproject.facerecognition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class ReactWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket végpont beállítása
        registry.addHandler(new ReactWebSocketHandlerImpl(), "/ws").setAllowedOrigins("*");
    }

    @Bean
    @Profile("!test") //csak akkor regisztrálódik, ha NEM a test profil aktív
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    @Profile("!test")
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(500_000);   // max 500 KB szöveg
        container.setMaxBinaryMessageBufferSize(500_000); // max 500 KB bináris - ezt külön át kellett állítani
        return container;
    }
}

