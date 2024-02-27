package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.*;
import net.security.infosec.models.Category;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.models.Trouble;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.services.TaskService;
import net.security.infosec.services.TroubleTicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/info/week")
    @PreAuthorize("hasAnyRole('WORKER','MANAGER','ADMIN')")
    public Mono<Rendering> getInfo(@AuthenticationPrincipal Implementer implementer){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("неделю");
            return implementerService.getUserById(implementer.getId()).flatMap(impl -> taskService.getImplementerTasksForWeek(impl).collectList().flatMap(tasks -> {
                int count = 0;
                for(Task task : tasks){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            }));
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getImplementerTasksForWeek(implementer).collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setUsername(implementer.getFullName());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            });
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-info-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getImplementerTasksForWeek(implementer))
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @GetMapping("/info/month")
    @PreAuthorize("hasAnyRole('WORKER','MANAGER','ADMIN')")
    public Mono<Rendering> getMonth(@AuthenticationPrincipal Implementer implementer){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("месяц");
            return implementerService.getUserById(implementer.getId()).flatMap(impl -> taskService.getImplementerTasksForMonth(impl).collectList().flatMap(tasks -> {
                int count = 0;
                for(Task task : tasks){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            }));
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getImplementerTasksForMonth(implementer).collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setUsername(implementer.getFullName());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            });
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-info-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getImplementerTasksForMonth(implementer))
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @GetMapping("/info/year")
    @PreAuthorize("hasAnyRole('WORKER','MANAGER','ADMIN')")
    public Mono<Rendering> getYear(@AuthenticationPrincipal Implementer implementer){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("год");
            return implementerService.getUserById(implementer.getId()).flatMap(impl -> taskService.getImplementerTasksForYear(impl).collectList().flatMap(tasks -> {
                int count = 0;
                for(Task task : tasks){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            }));
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getImplementerTasksForYear(implementer).collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setUsername(implementer.getFullName());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            });
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-info-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getImplementerTasksForYear(implementer))
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @PostMapping("/info/date")
    @PreAuthorize("hasAnyRole('WORKER','MANAGER','ADMIN')")
    public Mono<Rendering> getMainFromDate(@AuthenticationPrincipal Implementer implementer, @ModelAttribute(name = "dates") DateDTO dateDTO) {
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("период с " + dateDTO.getBegin() + " по " + dateDTO.getEnd());
            return implementerService.getUserById(implementer.getId()).flatMap(impl -> taskService.getImplementerTasksForDates(impl, dateDTO).collectList().flatMap(tasks -> {
                int count = 0;
                for (Task task : tasks) {
                    if (category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())) {
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            }));
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for (Trouble trouble : troubles) {
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getImplementerTasksForDates(implementer, dateDTO).collectList().flatMap(tasks -> {
                    for (TroubleDTO troubleDTO : categoryDTO.getTroubles()) {
                        for (Task task : tasks) {
                            if (task.getTroubleId() == troubleDTO.getId()) {
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setUsername(implementer.getFullName());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            });
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-info-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getImplementerTasksForDates(implementer,dateDTO))
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @GetMapping("/stat/week")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public Mono<Rendering> taskStatWeek(){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("неделю");
            return taskService.getWeek().collectList().flatMap(week -> {
                int count = 0;
                for(Task task : week){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            });
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return implementerService.getAll().collectList().flatMap(implementers -> troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getWeek().collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                                for(Implementer implementer : implementers){
                                    if(task.getImplementerId() == implementer.getId()){
                                        taskDTO.setUsername(implementer.getFullName());
                                    }
                                }
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            }));
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getWeek())
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @GetMapping("/stat/month")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public Mono<Rendering> taskStatMonth(){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("месяц");
            return taskService.getMonth().collectList().flatMap(month -> {
                int count = 0;
                for(Task task : month){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            });
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return implementerService.getAll().collectList().flatMap(implementers -> troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getMonth().collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                                for(Implementer implementer : implementers){
                                    if(task.getImplementerId() == implementer.getId()){
                                        taskDTO.setUsername(implementer.getFullName());
                                    }
                                }
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            }));
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getMonth())
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @GetMapping("/stat/year")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public Mono<Rendering> taskStatYear(){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("год");
            return taskService.getYear().collectList().flatMap(year -> {
                int count = 0;
                for(Task task : year){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            });
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return implementerService.getAll().collectList().flatMap(implementers -> troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getYear().collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                                for(Implementer implementer : implementers){
                                    if(task.getImplementerId() == implementer.getId()){
                                        taskDTO.setUsername(implementer.getFullName());
                                    }
                                }
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            }));
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getYear())
                        .modelAttribute("dates", new DateDTO())
                        .build()
        );
    }

    @PostMapping("/stat/date")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public Mono<Rendering> viewStatFromDate(@ModelAttribute(name = "dates") DateDTO dateDTO){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("период с " + dateDTO.getBegin() + " по " + dateDTO.getEnd());
            return taskService.getFromDate(dateDTO).collectList().flatMap(dateTasks -> {
                int count = 0;
                for(Task task : dateTasks){
                    if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                        count++;
                    }
                }
                chart.setTaskCount(count);
                return Mono.just(chart);
            });
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);

        Flux<CategoryDTO> statistics = troubleTicketService.getAllCategories().flatMap(category -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setTitle(category.getName());
            return implementerService.getAll().collectList().flatMap(implementers -> troubleTicketService.getTroubleByIds(category.getTroubleIds()).collectList().flatMap(troubles -> {
                for(Trouble trouble : troubles){
                    TroubleDTO troubleDTO = new TroubleDTO();
                    troubleDTO.setTitle(trouble.getName());
                    troubleDTO.setId(trouble.getId());
                    categoryDTO.addTrouble(troubleDTO);
                }
                return taskService.getFromDate(dateDTO).collectList().flatMap(tasks -> {
                    for(TroubleDTO troubleDTO : categoryDTO.getTroubles()){
                        for(Task task : tasks){
                            if(task.getTroubleId() == troubleDTO.getId()){
                                TaskDTO taskDTO = new TaskDTO();
                                taskDTO.setTitle(task.getTitle());
                                taskDTO.setContent(task.getDescription());
                                taskDTO.setPlacedAt(task.getExecuteDate());
                                troubleDTO.addTask(taskDTO);
                                for(Implementer implementer : implementers){
                                    if(task.getImplementerId() == implementer.getId()){
                                        taskDTO.setUsername(implementer.getFullName());
                                    }
                                }
                            }
                        }
                    }
                    return Mono.just(categoryDTO);
                });
            }));
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("statistics",statistics)
                        .modelAttribute("taskList", taskService.getFromDate(dateDTO))
                        .modelAttribute("dates", new DateDTO())
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
            return Mono.just(Rendering.redirectTo("/").build());
        });
    }

}
