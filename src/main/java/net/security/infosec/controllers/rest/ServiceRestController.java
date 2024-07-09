package net.security.infosec.controllers.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/service")
public class ServiceRestController {
    @GetMapping("/get/ip")
    public Mono<String> getIpAddress(ServerWebExchange exchange) {
        String ipAddress = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty()) {
            if (exchange.getRequest().getRemoteAddress() != null) {
                ipAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            } else {
                ipAddress = "IP address not found";
            }
        }

        return Mono.just(ipAddress);
    }
}
