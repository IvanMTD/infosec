package net.security.infosec.component;

import lombok.RequiredArgsConstructor;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.utils.JWT;
import net.security.infosec.utils.NamingUtil;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JWT jwt;
    private final ImplementerService userService;
    private final PasswordEncoder encoder;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String accessToken = authentication.getPrincipal().toString();
        String refreshToken = authentication.getCredentials().toString();

        return Mono.deferContextual(contextView -> {
            ServerWebExchange exchange = contextView.get(ServerWebExchange.class);
            String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

            if(jwt.validateToken(accessToken)){
                // проверяем digital_signature
                String tokenSignature = jwt.getDigitalSignatureFromToken(accessToken);
                if(tokenSignature != null && tokenSignature.equals(userAgent)){
                    return userService.findByUsername(jwt.getUsernameFromToken(accessToken))
                            .flatMap(user -> Mono.just(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())));
                }
                // сигнатура не совпадает — токен украден? fallback к refresh
            }

            if(jwt.validateToken(refreshToken)){
                return userService.findByUsername(jwt.getUsernameFromToken(refreshToken)).flatMap(user -> {
                    String username = user.getUsername();
                    String newAccessToken = jwt.generateAccessToken(username, userAgent);
                    String newRefreshToken = jwt.generateRefreshToken(username, userAgent);
                    ResponseCookie accessCookie = ResponseCookie.from(NamingUtil.getInstance().getAccessName(), newAccessToken)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Strict")
                            .maxAge(Duration.ofHours(1))
                            .path("/")
                            .build();
                    ResponseCookie refreshCookie = ResponseCookie.from(NamingUtil.getInstance().getRefreshName(), newRefreshToken)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Strict")
                            .maxAge(Duration.ofDays(1))
                            .path("/")
                            .build();

                    exchange.getResponse().addCookie(accessCookie);
                    exchange.getResponse().addCookie(refreshCookie);
                    return Mono.just(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                }).switchIfEmpty(Mono.error(new BadCredentialsException("Authentication failed")));
            }

            // fallback: логин/пароль
            String username = authentication.getPrincipal().toString();
            String password = authentication.getCredentials().toString();
            return userService.findByUsername(username).flatMap(user -> {
                if(encoder.matches(password, user.getPassword())){
                    return Mono.just(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                } else{
                    return Mono.error(new BadCredentialsException("Authentication failed"));
                }
            }).cast(Authentication.class).switchIfEmpty(Mono.error(new BadCredentialsException("Authentication failed")));
        });
    }
}
