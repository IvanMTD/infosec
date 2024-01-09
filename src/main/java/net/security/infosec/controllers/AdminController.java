package net.security.infosec.controllers;

import net.security.infosec.dto.RoleDataTransferObject;
import net.security.infosec.models.Role;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

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
}
