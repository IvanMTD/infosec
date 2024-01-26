package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.ImplementerDataTransferObject;
import net.security.infosec.dto.RoleDataTransferObject;
import net.security.infosec.dto.TicketDataTransferObject;
import net.security.infosec.models.Role;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.services.RoleService;
import net.security.infosec.services.TroubleTicketService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final RoleService roleService;
    private final ImplementerService implementerService;
    private final TroubleTicketService troubleTicketService;

    @GetMapping()
    public Mono<Rendering> adminPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Admin page")
                        .modelAttribute("index","admin-page")
                        .modelAttribute("showTrouble", troubleTicketService.getAllCategories().collectList().flatMap(l -> {
                            if(l.size() > 0){
                                return Mono.just(true);
                            }
                            return Mono.just(false);
                        }))
                        .build()
        );
    }

    @GetMapping("/roles")
    public Mono<Rendering> rolesPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Roles page")
                        .modelAttribute("index","roles-page")
                        .modelAttribute("roles", roleService.getAll())
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

    @GetMapping("/role/edit")
    public Mono<Rendering> roleEdit(@RequestParam(name = "select") int id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Role edit page")
                        .modelAttribute("index","role-edit-page")
                        .modelAttribute("role", roleService.getRoleDTOById(id))
                        .modelAttribute("authorities", Role.Authority.values())
                        .build()
        );
    }

    @PostMapping("/role/update/{id}")
    public Mono<Rendering> roleUpdate(@ModelAttribute(name = "role") @Valid RoleDataTransferObject role, Errors errors, @PathVariable(name = "id") int id){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Role edit page")
                            .modelAttribute("index","role-edit-page")
                            .modelAttribute("role", role)
                            .modelAttribute("authorities", Role.Authority.values())
                            .build()
            );
        }
        return roleService.updateRole(role, id).flatMap(r -> {
            log.info("updated role data: " + r.toString());
            return Mono.just(Rendering.redirectTo("/admin/roles").build());
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

    @GetMapping("/category/reg")
    public Mono<Rendering> categoryReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Category registration")
                        .modelAttribute("index","category-reg-page")
                        .modelAttribute("ticket", new TicketDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/category/add")
    public Mono<Rendering> categoryAdd(@ModelAttribute(name = "ticket") @Valid TicketDataTransferObject ticket, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Category registration")
                            .modelAttribute("index","category-reg-page")
                            .modelAttribute("ticket", ticket)
                            .build()
            );
        }
        return troubleTicketService.saveCategory(ticket).flatMap(category -> {
            log.info("Category saved: " + category.toString());
            return Mono.just(Rendering.redirectTo("/admin").build());
        });
    }

    @GetMapping("/trouble/reg")
    public Mono<Rendering> troubleReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Trouble registration")
                        .modelAttribute("index","trouble-reg-page")
                        .modelAttribute("categories", troubleTicketService.getAllCategories())
                        .modelAttribute("ticket", new TicketDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/trouble/add")
    public Mono<Rendering> troubleAdd(@ModelAttribute(name = "ticket") @Valid TicketDataTransferObject ticket, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Trouble registration")
                            .modelAttribute("index","trouble-reg-page")
                            .modelAttribute("categories", troubleTicketService.getAllCategories())
                            .modelAttribute("ticket", ticket)
                            .build()
            );
        }
        return troubleTicketService.saveTroubleInCategory(ticket).flatMap(category -> {
            log.info("Trouble added to category " + category.getName() + " and now: " + category);
            return Mono.just(Rendering.redirectTo("/admin").build());
        });
    }
}
