package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.RoleDataTransferObject;
import net.security.infosec.models.Role;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/reg/role")
    public Mono<Rendering> roleRegPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Role registration")
                        .modelAttribute("index","admin-reg-role-page")
                        .modelAttribute("authorities", Role.Authority.values())
                        .modelAttribute("role", new RoleDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/add/role")
    public Mono<Rendering> addRole(@ModelAttribute(name = "role") @Valid RoleDataTransferObject role, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Role registration")
                            .modelAttribute("index","admin-reg-role-page")
                            .modelAttribute("authorities", Role.Authority.values())
                            .modelAttribute("role", role)
                            .build()
            );
        }
        log.info("request param: " + role.toString());

        return Mono.just(
                Rendering.redirectTo("/")
                        .build()
        );
    }
}
