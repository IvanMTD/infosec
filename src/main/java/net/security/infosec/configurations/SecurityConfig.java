package net.security.infosec.configurations;

import lombok.RequiredArgsConstructor;
import net.security.infosec.component.JwtAuthenticationConverter;
import net.security.infosec.component.JwtAuthenticationManager;
import net.security.infosec.component.JwtAuthenticationSuccessHandler;
import net.security.infosec.component.JwtLogoutSuccessHandler;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.utils.JWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private final ImplementerService implementerService;
    private final JWT jwt;
    private final PasswordEncoder passwordEncoder;
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
                                .pathMatchers("/favicon.ico","/img/**","/css/**","/js/**","/fw/**","/contacts.xml","/contacts/group/list.xml").permitAll()
                                .pathMatchers("/login","/guide/list","/api/**").permitAll()
                                .anyExchange().authenticated()
                )
                .formLogin(loginSpec -> loginSpec.loginPage("/login").authenticationSuccessHandler(authenticationSuccessHandler()))
                .logout(logoutSpec -> logoutSpec.logoutSuccessHandler(logoutSuccessHandler()))
                .build();
    }

    @Bean
    public JwtAuthenticationManager authenticationManager() {
        return new JwtAuthenticationManager(jwt, implementerService, passwordEncoder);
    }

    @Bean
    public JwtAuthenticationSuccessHandler authenticationSuccessHandler(){
        return new JwtAuthenticationSuccessHandler(jwt);
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
}
