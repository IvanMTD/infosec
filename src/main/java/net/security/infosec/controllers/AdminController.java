package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.ImplementerDataTransferObject;
import net.security.infosec.dto.TicketDataTransferObject;
import net.security.infosec.models.Role;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.services.TroubleTicketService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ImplementerService implementerService;
    private final TroubleTicketService troubleTicketService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> adminPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Admin page")
                        .modelAttribute("index","admin-page")
                        .build()
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> usersPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Users page")
                        .modelAttribute("index","users-page")
                        .modelAttribute("users", implementerService.getAll())
                        .modelAttribute("implementer", new ImplementerDataTransferObject())
                        .modelAttribute("roles",Role.values())
                        .build()
        );
    }

    /*@GetMapping("/user/reg")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> userReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","User registration")
                        .modelAttribute("index","user-reg-page")
                        .modelAttribute("roles", Role.values())
                        .modelAttribute("implementer", new ImplementerDataTransferObject())
                        .build()
        );
    }*/

    @PostMapping("/user/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> userAdd(@ModelAttribute(name = "implementer") @Valid ImplementerDataTransferObject implementer, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Users page")
                            .modelAttribute("index","users-page")
                            .modelAttribute("users", implementerService.getAll())
                            .modelAttribute("implementer", implementer)
                            .modelAttribute("roles",Role.values())
                            .build()
            );
        }

        return implementerService.saveImplementer(implementer).flatMap(impl -> {
            log.info("user saved: " + impl.toString());
            return Mono.just(Rendering.redirectTo("/admin/users").build());
        });
    }

    @GetMapping("/user/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> userEdit(@RequestParam(name = "select") int id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","User edit page")
                        .modelAttribute("index","user-edit-page")
                        .modelAttribute("implementer", implementerService.getUserDtoById(id))
                        .modelAttribute("roles", Role.values())
                        .build()
        );
    }

    @PostMapping("/user/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> userUpdate(@ModelAttribute(name = "implementer") @Valid ImplementerDataTransferObject implementerDTO, Errors errors, @PathVariable(name = "id") int id){
        if(errors.hasFieldErrors("firstname") || errors.hasFieldErrors("middleName") || errors.hasFieldErrors("lastname") || errors.hasFieldErrors("officePosition")){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","User edit page")
                            .modelAttribute("index","user-edit-page")
                            .modelAttribute("implementer", implementerDTO)
                            .modelAttribute("roles", Role.values())
                            .build()
            );
        }
        return implementerService.updateImplementer(implementerDTO,id).flatMap(implementer -> {
            log.info("implementer updated: " + implementer.toString());
            return Mono.just(Rendering.redirectTo("/admin/users").build());
        });
    }

    // При удалении исполнителя нужно решить, удалять ли все задания выполненные этим исполнителем. Так что вопрос пока открытый.
    /*@GetMapping("/user/delete")
    public Mono<Rendering> userDelete(@RequestParam(name = "select") int id){
        return implementerService.deleteImplementerById(id).then(Mono.just(Rendering.redirectTo("/admin/users").build()));
    }*/

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> categoriesPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Categories page")
                        .modelAttribute("index", "categories-page")
                        .modelAttribute("categories", troubleTicketService.getAllCategories())
                        .build()
        );
    }

    @GetMapping("/category/reg")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/category/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> categoryEdit(@RequestParam(name = "select") int id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Category edit page")
                        .modelAttribute("index","category-edit-page")
                        .modelAttribute("ticket", troubleTicketService.getCategoryDTOById(id))
                        .build()
        );
    }

    @PostMapping("/category/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> categoryUpdate(@ModelAttribute(name = "ticket") @Valid TicketDataTransferObject ticketDTO, Errors errors, @PathVariable(name = "id") int id){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Category edit page")
                            .modelAttribute("index","category-edit-page")
                            .modelAttribute("ticket", ticketDTO)
                            .build()
            );
        }
        return troubleTicketService.updateCategory(ticketDTO,id).flatMap(category -> {
            log.info("category updated: " + category.toString());
            return Mono.just(Rendering.redirectTo("/admin/categories").build());
        });
    }

    @GetMapping("/troubles")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> troublesPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Troubles page")
                        .modelAttribute("index","troubles-page")
                        .modelAttribute("categories", troubleTicketService.getAllCategories())
                        .modelAttribute("troubles", troubleTicketService.getAllTrouble())
                        .build()
        );
    }

    @GetMapping("/trouble/reg")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/trouble/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> troubleEdit(@RequestParam(name = "select") int id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Trouble edit page")
                        .modelAttribute("index","trouble-edit-page")
                        .modelAttribute("categories", troubleTicketService.getAllCategories())
                        .modelAttribute("ticket", troubleTicketService.getTroubleDTOById(id))
                        .build()
        );
    }

    @PostMapping("/trouble/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> troubleUpdate(@ModelAttribute(name = "ticket") @Valid TicketDataTransferObject ticketDTO, Errors errors, @PathVariable(name = "id") int id){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Trouble edit page")
                            .modelAttribute("index","trouble-edit-page")
                            .modelAttribute("categories", troubleTicketService.getAllCategories())
                            .modelAttribute("ticket", ticketDTO)
                            .build()
            );
        }
        return troubleTicketService.updateTrouble(ticketDTO,id).flatMap(trouble -> {
            log.info("trouble updated: " + trouble.toString());
            return Mono.just(Rendering.redirectTo("/admin/troubles").build());
        });
    }
}
