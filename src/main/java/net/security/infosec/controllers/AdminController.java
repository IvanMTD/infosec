package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.ImplementerDataTransferObject;
import net.security.infosec.dto.RoleDataTransferObject;
import net.security.infosec.dto.TroubleDataTransferObject;
import net.security.infosec.models.Role;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.services.RoleService;
import net.security.infosec.services.TroubleService;
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
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final RoleService roleService;
    private final ImplementerService implementerService;
    private final TroubleService troubleService;

    @GetMapping()
    public Mono<Rendering> adminPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Admin page")
                        .modelAttribute("index","admin-page")
                        .build()
        );
    }

    @GetMapping("/role/reg")
    public Mono<Rendering> roleReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Role registration")
                        .modelAttribute("index","role-reg-page")
                        .modelAttribute("authorities", Role.Authority.values())
                        .modelAttribute("role", new RoleDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/role/add")
    public Mono<Rendering> roleAdd(@ModelAttribute(name = "role") @Valid RoleDataTransferObject role, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Role registration")
                            .modelAttribute("index","role-reg-page")
                            .modelAttribute("authorities", Role.Authority.values())
                            .modelAttribute("role", role)
                            .build()
            );
        }

        return roleService.saveRole(role).flatMap(r -> {
            log.info("saved role data: " + r.toString());
            return Mono.just(Rendering.redirectTo("/admin").build());
        });
    }

    @GetMapping("/user/reg")
    public Mono<Rendering> userReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","User registration")
                        .modelAttribute("index","user-reg-page")
                        .modelAttribute("roles", roleService.getAll())
                        .modelAttribute("implementer", new ImplementerDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/user/add")
    public Mono<Rendering> userAdd(@ModelAttribute(name = "implementer") @Valid ImplementerDataTransferObject implementer, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","User registration")
                            .modelAttribute("index","user-reg-page")
                            .modelAttribute("roles", roleService.getAll())
                            .modelAttribute("implementer", implementer)
                            .build()
            );
        }

        return implementerService.saveImplementer(implementer).flatMap(impl -> {
            log.info("user saved: " + impl.toString());
            return Mono.just(Rendering.redirectTo("/admin").build());
        });
    }

    @GetMapping("/trouble/reg")
    public Mono<Rendering> troubleReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Trouble registration")
                        .modelAttribute("index","trouble-reg-page")
                        .modelAttribute("trouble", new TroubleDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/trouble/add")
    public Mono<Rendering> troubleAdd(@ModelAttribute(name = "trouble") TroubleDataTransferObject trouble){
        return troubleService.saveTrouble(trouble).flatMap(t -> {
            log.info("trouble saved: " + t.toString());
            return Mono.just(Rendering.redirectTo("/admin").build());
        });
    }
}
