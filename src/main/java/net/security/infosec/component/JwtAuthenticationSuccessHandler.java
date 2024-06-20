package net.security.infosec.component;

import lombok.RequiredArgsConstructor;
import net.security.infosec.utils.JWT;
import net.security.infosec.utils.NamingUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@RequiredArgsConstructor
public class JwtAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JWT jwt;
    private final ServerRequestCache requestCache;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        String username = authentication.getName();
        String digitalSignature = webFilterExchange.getExchange().getRequest().getHeaders().getFirst("User-Agent");

        String accessToken = jwt.generateAccessToken(username, digitalSignature);
        String refreshToken = jwt.generateRefreshToken(username, digitalSignature);

        // Add tokens to cookies
        ResponseCookie accessCookie = ResponseCookie.from(NamingUtil.getInstance().getAccessName(), accessToken)
                .httpOnly(true)
                .maxAge(Duration.ofHours(1))
                .path("/")
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from(NamingUtil.getInstance().getRefreshName(), refreshToken)
                .httpOnly(true)
                .maxAge(Duration.ofDays(1))
                .path("/")
                .build();

        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return requestCache.getRedirectUri(webFilterExchange.getExchange())
                .defaultIfEmpty(URI.create("/"))
                .flatMap(redirectUri -> {
                    response.setStatusCode(HttpStatus.FOUND);
                    webFilterExchange.getExchange().getResponse().getHeaders().setLocation(redirectUri);
                    return webFilterExchange.getExchange().getResponse().setComplete();
                });
    }
}
