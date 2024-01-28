package net.security.infosec.configurations;

import net.security.infosec.services.WebFluxWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class WebFluxConfig {
    private final WebFluxWebSocketHandler handler;

    public WebFluxConfig(WebFluxWebSocketHandler handler) {
        this.handler = handler;
    }

    @Bean
    public HandlerMapping handlerMapping(){
        Map<String, WebFluxWebSocketHandler> handlerMap = Map.of("/web-socket",handler);
        return new SimpleUrlHandlerMapping(handlerMap,1);
    }
}
