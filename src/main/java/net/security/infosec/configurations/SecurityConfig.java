package net.security.infosec.configurations;

import lombok.RequiredArgsConstructor;
import net.security.infosec.component.JwtAuthenticationConverter;
import net.security.infosec.component.JwtAuthenticationManager;
import net.security.infosec.component.JwtAuthenticationSuccessHandler;
import net.security.infosec.component.JwtLogoutSuccessHandler;
import net.security.infosec.repositories.ImplementerRepository;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.utils.JWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private final ImplementerRepository implementerRepository;
    private final JWT jwt;
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http){
        ServerCsrfTokenRequestAttributeHandler requestHandler = new ServerCsrfTokenRequestAttributeHandler();
        requestHandler.setTokenFromMultipartDataEnabled(true);

        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager());
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter());

        return http
                .csrf(csrf -> csrf.csrfTokenRequestHandler(requestHandler))
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(
                        auth -> auth
                                .pathMatchers("/login").permitAll()
                                .pathMatchers("/guide/list").permitAll()
                                .pathMatchers("/favicon.ico").permitAll()
                                .anyExchange().authenticated()
                )
                .formLogin(loginSpec -> loginSpec.loginPage("/login").authenticationSuccessHandler(authenticationSuccessHandler()))
                .logout(logoutSpec -> logoutSpec.logoutSuccessHandler(logoutSuccessHandler()))
                .requestCache(requestCacheSpec -> requestCacheSpec.requestCache(serverRequestCache()))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ServerRequestCache serverRequestCache() {
        return new WebSessionServerRequestCache();
    }

    @Bean
    public JwtAuthenticationManager authenticationManager() {
        return new JwtAuthenticationManager(jwt, implementerService(), passwordEncoder());
    }

    @Bean
    public JwtAuthenticationSuccessHandler authenticationSuccessHandler(){
        return new JwtAuthenticationSuccessHandler(jwt,serverRequestCache());
    }

    @Bean
    public JwtLogoutSuccessHandler logoutSuccessHandler(){
        JwtLogoutSuccessHandler logoutSuccessHandler = new JwtLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/"));
        return logoutSuccessHandler;
    }

    @Bean
    public JwtAuthenticationConverter authenticationConverter(){
        return new JwtAuthenticationConverter();
    }

    @Bean
    public ImplementerService implementerService(){
        return new ImplementerService(implementerRepository,passwordEncoder());
    }
}
