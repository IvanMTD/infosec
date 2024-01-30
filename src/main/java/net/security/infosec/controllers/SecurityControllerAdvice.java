package net.security.infosec.controllers;

import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Implementer;
import net.security.infosec.services.RoleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
@RequiredArgsConstructor
public class SecurityControllerAdvice {
    private final RoleService roleService;
    @ModelAttribute
    Mono<CsrfToken> csrfToken(ServerWebExchange exchange) {
        Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
        return csrfToken.doOnSuccess(token -> exchange.getAttributes().put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token));
    }

    @ModelAttribute(name = "userName")
    Mono<String> userName(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            return Mono.just(implementer.getFullName());
        }else{
            return Mono.empty();
        }
    }

    @ModelAttribute(name = "admin")
    Mono<Boolean> isAdmin(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            return roleService.getRoleById(implementer.getId()).flatMap(role -> {
                if(role.getAuthorities().size() == 6){
                    return Mono.just(true);
                }else{
                    return Mono.just(false);
                }
            });
        }else{
            return Mono.empty();
        }
    }
}
