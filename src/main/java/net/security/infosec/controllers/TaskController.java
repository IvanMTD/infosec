package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.ChartDTO;
import net.security.infosec.dto.StatDataTransferObject;
import net.security.infosec.dto.TaskDataTransferObject;
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

    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('WORKER','MANAGER','ADMIN')")
    public Mono<Rendering> getInfo(@AuthenticationPrincipal Implementer implementer){
        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("все время");
            return implementerService.getUserById(implementer.getId()).flatMap(impl -> taskService.getImplementerTasks(impl).collectList().flatMap(tasks -> {
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

        Mono<List<StatDataTransferObject>> statsFlux = taskService.getAll().flatMap(task -> {
            StatDataTransferObject stat = new StatDataTransferObject();
            stat.setTask(task);
            return implementerService.getUserById(stat.getTask().getImplementerId()).flatMap(impl -> {
                stat.setImplementer(impl);
                return troubleTicketService.getTaskTrouble(stat.getTask()).flatMap(trouble -> {
                    stat.setTrouble(trouble);
                    return troubleTicketService.getTroubleCategory(stat.getTrouble()).flatMap(category -> {
                        stat.setCategory(category);
                        return Mono.just(stat);
                    });
                });
            });
        }).collectList().flatMap(list -> {
            list = list.stream().sorted(Comparator.comparing(l -> l.getTask().getExecuteDate().getDayOfYear())).collect(Collectors.toList());
            return Mono.just(list);
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","My task info")
                        .modelAttribute("index","task-info-page")
                        .modelAttribute("charts", chartFlux)
                        .modelAttribute("stats", statsFlux)
                        .build()
        );
    }

    @GetMapping("/chart/week")
    public Mono<Rendering> taskChartWeek(){
        Flux<Implementer> impl = implementerService.getAll().flatMap(implementer -> {
            Implementer i = new Implementer();
            i.setLastname(implementer.getLastname());
            i.setFirstname(implementer.getFirstname());
            i.setMiddleName(implementer.getMiddleName());
            return taskService.getWeek().collectList().flatMap(list -> {
                for(Task task : list){
                    if(implementer.getTaskIds().stream().anyMatch(taskIds -> taskIds == task.getId())){
                        i.addTask(task);
                    }
                }
                return Mono.just(i);
            });
        });

        Flux<ChartDTO> chartCategoryFlux = taskService.getWeek().collectList().flatMapMany(tasks -> {
            Set<LocalDate> localDates = new HashSet<>();
            for(Task task : tasks){
                localDates.add(task.getExecuteDate());
            }
            List<LocalDate> dates = new ArrayList<>(localDates);
            dates = dates.stream().sorted(Comparator.comparing(LocalDate::getDayOfYear)).collect(Collectors.toList());
            return Flux.fromIterable(dates);
        }).flatMapSequential(localDate -> {
            ChartDTO chart = new ChartDTO();
            chart.setLocalDate(localDate);
            return troubleTicketService.getAllCategories().collectList().flatMap(categories -> taskService.getTasksByLocalDate(localDate).collectList().flatMap(tasks -> {
                for(Category category : categories){
                    int tCount = 0;
                    for(Task task : tasks){
                        if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                            tCount++;
                        }
                    }
                    chart.addTaskOnTrouble(tCount);
                }
                return Mono.just(chart);
            }));
        });

        Flux<ChartDTO> chartTroubleFlux = taskService.getWeek().collectList().flatMapMany(tasks -> {
            Set<LocalDate> localDates = new HashSet<>();
            for(Task task : tasks){
                localDates.add(task.getExecuteDate());
            }
            List<LocalDate> dates = new ArrayList<>(localDates);
            dates = dates.stream().sorted(Comparator.comparing(LocalDate::getDayOfYear)).collect(Collectors.toList());
            return Flux.fromIterable(dates);
        }).flatMapSequential(localDate -> {
            ChartDTO chart = new ChartDTO();
            chart.setLocalDate(localDate);
            return troubleTicketService.getAllTrouble().collectList().flatMap(troubles -> taskService.getTasksByLocalDate(localDate).collectList().flatMap(tasks -> {
               for(Trouble trouble : troubles){
                   int tCount = 0;
                   for(Task task : tasks){
                       if(task.getTroubleId() == trouble.getId()) {
                           tCount++;
                       }
                   }
                   chart.addTaskOnTrouble(tCount);
               }
               return Mono.just(chart);
            }));
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic page")
                        .modelAttribute("index","task-graph-page")
                        .modelAttribute("implementers", impl)
                        .modelAttribute("categoryTitle", troubleTicketService.getAllCategories())
                        .modelAttribute("categoryChart", chartCategoryFlux)
                        .modelAttribute("troubleTitle", troubleTicketService.getAllTrouble())
                        .modelAttribute("troubleChart", chartTroubleFlux)
                        .build()
        );
    }

    @GetMapping("/chart/month")
    public Mono<Rendering> taskChartMonth(){

        Flux<Implementer> impl = implementerService.getAll().flatMap(implementer -> {
            Implementer i = new Implementer();
            i.setLastname(implementer.getLastname());
            i.setFirstname(implementer.getFirstname());
            i.setMiddleName(implementer.getMiddleName());
            return taskService.getMonth().collectList().flatMap(list -> {
                for(Task task : list){
                    if(implementer.getTaskIds().stream().anyMatch(taskIds -> taskIds == task.getId())){
                        i.addTask(task);
                    }
                }
                return Mono.just(i);
            });
        });

        Flux<ChartDTO> chartCategoryFlux = taskService.getMonth().collectList().flatMapMany(tasks -> {
            Set<LocalDate> localDates = new HashSet<>();
            for(Task task : tasks){
                localDates.add(task.getExecuteDate());
            }
            List<LocalDate> dates = new ArrayList<>(localDates);
            dates = dates.stream().sorted(Comparator.comparing(LocalDate::getDayOfYear)).collect(Collectors.toList());
            return Flux.fromIterable(dates);
        }).flatMapSequential(localDate -> {
            ChartDTO chart = new ChartDTO();
            chart.setLocalDate(localDate);
            return troubleTicketService.getAllCategories().collectList().flatMap(categories -> taskService.getTasksByLocalDate(localDate).collectList().flatMap(tasks -> {
                for(Category category : categories){
                    int tCount = 0;
                    for(Task task : tasks){
                        if(category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())){
                            tCount++;
                        }
                    }
                    chart.addTaskOnTrouble(tCount);
                }
                return Mono.just(chart);
            }));
        });

        Flux<ChartDTO> chartTroubleFlux = taskService.getMonth().collectList().flatMapMany(tasks -> {
            Set<LocalDate> localDates = new HashSet<>();
            for(Task task : tasks){
                localDates.add(task.getExecuteDate());
            }
            List<LocalDate> dates = new ArrayList<>(localDates);
            dates = dates.stream().sorted(Comparator.comparing(LocalDate::getDayOfYear)).collect(Collectors.toList());
            return Flux.fromIterable(dates);
        }).flatMapSequential(localDate -> {
            ChartDTO chart = new ChartDTO();
            chart.setLocalDate(localDate);
            return troubleTicketService.getAllTrouble().collectList().flatMap(troubles -> taskService.getTasksByLocalDate(localDate).collectList().flatMap(tasks -> {
                for(Trouble trouble : troubles){
                    int tCount = 0;
                    for(Task task : tasks){
                        if(task.getTroubleId() == trouble.getId()) {
                            tCount++;
                        }
                    }
                    chart.addTaskOnTrouble(tCount);
                }
                return Mono.just(chart);
            }));
        });

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic page")
                        .modelAttribute("index","task-graph-page")
                        .modelAttribute("implementers", impl)
                        .modelAttribute("categoryTitle", troubleTicketService.getAllCategories())
                        .modelAttribute("categoryChart", chartCategoryFlux)
                        .modelAttribute("troubleTitle", troubleTicketService.getAllTrouble())
                        .modelAttribute("troubleChart", chartTroubleFlux)
                        .build()
        );
    }

    @GetMapping("/stat/week")
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

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts",chartFlux)
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
                                }).collectList().flatMap(list -> {
                                    list = list.stream().sorted(Comparator.comparing(l -> l.getTask().getExecuteDate().getDayOfWeek())).collect(Collectors.toList());
                                    return Mono.just(list);
                                })
                        )
                        .build()
        );
    }

    @GetMapping("/stat/month")
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

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts",chartFlux)
                        .modelAttribute("stats",
                                taskService.getMonth().flatMap(task -> {
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
                                }).collectList().flatMap(list -> {
                                    list = list.stream().sorted(Comparator.comparing(l -> l.getTask().getExecuteDate().getDayOfMonth())).collect(Collectors.toList());
                                    return Mono.just(list);
                                })
                        )
                        .build()
        );
    }

    @GetMapping("/stat/all")
    public Mono<Rendering> taskStatAll(){

        Flux<ChartDTO> chartFlux = troubleTicketService.getAllCategories().flatMap(category -> {
            ChartDTO chart = new ChartDTO();
            chart.setTitle(category.getName());
            chart.setCId(category.getId());
            chart.setStatus("все время");
            return taskService.getAll().collectList().flatMap(all -> {
                int count = 0;
                for(Task task : all){
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

        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task statistic")
                        .modelAttribute("index","task-stat-page")
                        .modelAttribute("charts",chartFlux)
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
                                }).collectList().flatMap(list -> {
                                    list = list.stream().sorted(Comparator.comparing(l -> l.getTask().getExecuteDate().getDayOfYear())).collect(Collectors.toList());
                                    return Mono.just(list);
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
