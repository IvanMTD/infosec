package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.StatDataTransferObject;
import net.security.infosec.dto.TaskDataTransferObject;
import net.security.infosec.dto.TaskStatDTO;
import net.security.infosec.models.Implementer;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.services.TaskService;
import net.security.infosec.services.TroubleTicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/task")
public class TaskController {
    private final ImplementerService implementerService;
    private final TroubleTicketService troubleTicketService;
    private final TaskService taskService;
    @GetMapping()
    public Mono<Rendering> taskPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task page")
                        .modelAttribute("index","task-page")
                        .build()
        );
    }

    @GetMapping("/stat")
    public Mono<Rendering> taskStatPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic page")
                        .modelAttribute("index","task-graph-page")
                        .modelAttribute("implementers", implementerService.getAll())
                        .build()
        );
    }

    @GetMapping("/stat/all")
    public Mono<Rendering> taskStatAll(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("stats",
                                taskService.getAll().flatMap(task -> {
                                    StatDataTransferObject stat = new StatDataTransferObject();
                                    stat.setTask(task);
                                    return implementerService.getUserById(stat.getTask().getImplementerId()).flatMap(implementer -> {
                                        stat.setImplementer(implementer);
                                        return troubleTicketService.getTaskTrouble(stat.getTask()).flatMap(trouble -> {
                                            stat.setTrouble(trouble);
                                            return troubleTicketService.getTroubleCategory(stat.getTrouble()).flatMap(category -> {
                                                stat.setCategory(category);
                                                return Mono.just(stat);
                                            });
                                        });
                                    });
                                })
                        )
                        .build()
        );
    }

    @GetMapping("/stat/week")
    public Mono<Rendering> taskStatWeek(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("stats",
                                taskService.getWeek().flatMap(task -> {
                                    StatDataTransferObject stat = new StatDataTransferObject();
                                    stat.setTask(task);
                                    return implementerService.getUserById(stat.getTask().getImplementerId()).flatMap(implementer -> {
                                        stat.setImplementer(implementer);
                                        return troubleTicketService.getTaskTrouble(stat.getTask()).flatMap(trouble -> {
                                            stat.setTrouble(trouble);
                                            return troubleTicketService.getTroubleCategory(stat.getTrouble()).flatMap(category -> {
                                                stat.setCategory(category);
                                                return Mono.just(stat);
                                            });
                                        });
                                    });
                                })
                        )
                        .build()
        );
    }

    @GetMapping("/new/reg")
    public Mono<Rendering> taskReg(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task completed page")
                        .modelAttribute("index","task-completed-page")
                        .modelAttribute("troubleTickets", troubleTicketService.getTroubleTickets())
                        .modelAttribute("task", new TaskDataTransferObject())
                        .build()
        );
    }

    @PostMapping("/new/add")
    public Mono<Rendering> taskAdd(@AuthenticationPrincipal Implementer implementer, @ModelAttribute(name = "task") @Valid TaskDataTransferObject task, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Task completed page")
                            .modelAttribute("index","task-completed-page")
                            .modelAttribute("troubleTickets", troubleTicketService.getTroubleTickets())
                            .modelAttribute("task", task)
                            .build()
            );
        }
        task.setImplementerId(implementer.getId());
        return taskService.saveTask(task).flatMap(t -> {
            log.info("task saved: " + t.toString());
            return implementerService.updateImplementerTask(implementer, t);
        }).flatMap(i -> {
            log.info("Implementer updated: " + i.toString());
            return Mono.just(Rendering.redirectTo("/task").build());
        });
    }

}
