package net.security.infosec.controllers;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.ChartDTO;
import net.security.infosec.models.Task;
import net.security.infosec.models.Trouble;
import net.security.infosec.services.TaskService;
import net.security.infosec.services.TroubleTicketService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TroubleTicketService troubleTicketService;
    private final TaskService taskService;
    @GetMapping("/")
    public Mono<Rendering> homePage(){
        Flux<ChartDTO> chartTroubleFlux = taskService.getYear().collectList().flatMapMany(tasks -> {
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
                        .modelAttribute("index","home-page")
                        .modelAttribute("title","Home Page")
                        .modelAttribute("troubleTitle", troubleTicketService.getAllTrouble())
                        .modelAttribute("troubleChart", chartTroubleFlux)
                        .modelAttribute("year", LocalDate.now().getYear())
                        .build()
        );
    }

    @GetMapping("/login")
    public Mono<Rendering> loginPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Login page")
                        .modelAttribute("index","login-page")
                        .build()
        );
    }
}
