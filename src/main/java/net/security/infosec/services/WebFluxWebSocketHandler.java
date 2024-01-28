package net.security.infosec.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class WebFluxWebSocketHandler implements WebSocketHandler {

    @Override
    public List<String> getSubProtocols() {
        return WebSocketHandler.super.getSubProtocols();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> stringFlux = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(String::toUpperCase)
                .map(session::textMessage);
        return session.send(stringFlux);
    }
}
