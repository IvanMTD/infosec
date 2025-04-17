package net.security.infosec.controllers.security;

import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@ControllerAdvice
@RequiredArgsConstructor
public class SecurityControllerAdvice {
    @ModelAttribute
    Mono<CsrfToken> csrfToken(ServerWebExchange exchange) {
        Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
        return csrfToken.doOnSuccess(token -> exchange.getAttributes().put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token));
    }

    @ModelAttribute(name = "userName")
    public Mono<String> userName(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            return Mono.just(implementer.getFullName());
        }else{
            return Mono.empty();
        }
    }

    @ModelAttribute(name = "auth")
    public Boolean isAuthenticate(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            return true;
        }else{
            return false;
        }
    }

    @ModelAttribute(name = "admin")
    public Mono<Boolean> isAdmin(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            if(implementer.getRole().equals(Role.ADMIN)){
                return Mono.just(true);
            }else{
                return Mono.just(false);
            }
        }else{
            return Mono.just(false);
        }
    }

    @ModelAttribute(name = "director")
    public Mono<Boolean> isDirector(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            if(implementer.getRole().equals(Role.DIRECTOR)){
                return Mono.just(true);
            }else{
                return Mono.just(false);
            }
        }else{
            return Mono.just(false);
        }
    }

    @ModelAttribute(name = "manager")
    public Mono<Boolean> isManager(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            if(implementer.getRole().equals(Role.MANAGER)){
                return Mono.just(true);
            }else{
                return Mono.just(false);
            }
        }else{
            return Mono.just(false);
        }
    }

    @ModelAttribute(name = "worker")
    public Mono<Boolean> isWorker(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            if(implementer.getRole().equals(Role.WORKER)){
                return Mono.just(true);
            }else{
                return Mono.just(false);
            }
        }else{
            return Mono.just(false);
        }
    }

    @ModelAttribute(name = "gAdmin")
    public Mono<Boolean> isGuideAdmin(@AuthenticationPrincipal Implementer implementer){
        if(implementer != null){
            if(implementer.getRole().equals(Role.GUIDE_ADMIN)){
                return Mono.just(true);
            }else{
                return Mono.just(false);
            }
        }else{
            return Mono.just(false);
        }
    }

    @ModelAttribute(name = "current_year")
    public Mono<Integer> year(){
        return Mono.just(LocalDate.now().getYear());
    }
}
