package net.security.infosec.configurations;

import net.security.infosec.services.WebFluxWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

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

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("session-info-security");
        //resolver.setCookieMaxAge(Duration.ofHours(12));
        return resolver;
    }
}
