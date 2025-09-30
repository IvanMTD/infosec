package net.security.infosec.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.ChartDTO;
import net.security.infosec.dto.ConstructDTO;
import net.security.infosec.dto.DateDTO;
import net.security.infosec.dto.PasswordDTO;
import net.security.infosec.models.Category;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.models.Trouble;
import net.security.infosec.services.ImplementerService;
import net.security.infosec.services.TaskService;
import net.security.infosec.services.TroubleTicketService;
import net.security.infosec.utils.MonthConverter;
import net.security.infosec.validations.PasswordValidation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TroubleTicketService troubleTicketService;
    private final TaskService taskService;
    private final ImplementerService implementerService;

    private final PasswordValidation passwordValidation;
    @GetMapping("/")
    public Mono<Rendering> homePage(@AuthenticationPrincipal Implementer user){
        log.info("user is : [{}]", user);
        /**
         * CATEGORY DAY
         */
        Flux<ChartDTO> chartCategoryFluxDay = taskService.getYear().flatMap(task -> {
            return troubleTicketService.getTroubleById(task.getTroubleId()).flatMap(trouble -> {
                if(trouble.getDepartmentRole() == user.getDepartmentRole()){
                    return Mono.just(task);
                }else {
                    return Mono.empty();
                }
            });
        }).collectList().flatMapMany(tasks -> {
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
            return troubleTicketService.getAllCategories().flatMap(c -> {
                if(c.getDepartmentRole() == user.getDepartmentRole()){
                    return Mono.just(c);
                }else{
                    return Mono.empty();
                }
            }).collectList().flatMap(categories -> taskService.getTasksByLocalDate(localDate).collectList().flatMap(tasks -> {
                List<Category> list = categories.stream().sorted(Comparator.comparing(Category::getId)).collect(Collectors.toList());
                for(Category category : list){
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

        /**
         * CATEGORY WEEK
         */

        int year = LocalDate.now().getYear();
        LocalDate earlyYear = LocalDate.of(year,1,1);
        LocalDate startWeek = earlyYear;
        Map<Integer, DateDTO> weeks = new HashMap<>();
        int weekCount = 1;
        while (!earlyYear.equals(LocalDate.now())){
            earlyYear = earlyYear.plusDays(1);
            if(earlyYear.getDayOfWeek() == DayOfWeek.SUNDAY){
                DateDTO dateDTO = new DateDTO();
                dateDTO.setBegin(startWeek);
                dateDTO.setEnd(earlyYear);
                weeks.put(weekCount,dateDTO);
                startWeek = earlyYear.plusDays(1);
                weekCount++;
            }
        }

        if(!startWeek.equals(LocalDate.now())){
            DateDTO dateDTO = new DateDTO();
            dateDTO.setBegin(startWeek);
            dateDTO.setEnd(LocalDate.now());
            weeks.put(weekCount,dateDTO);
        }

        Flux<ConstructDTO> chartCategoryFluxWeek = Flux.empty();
        for(Integer key : weeks.keySet()){
            Flux<ConstructDTO> chartFluxWeek = troubleTicketService.getAllCategories().flatMap(category -> {
                if(category.getDepartmentRole() == user.getDepartmentRole()){
                    ChartDTO chart = new ChartDTO();
                    chart.setTitle(category.getName());
                    chart.setCId(category.getId());
                    chart.setStatus("Неделя " + key);
                    chart.setDescription(category.getDescription());
                    return taskService.getFromDate(weeks.get(key)).collectList().flatMap(dateTasks -> {
                        int count = 0;
                        for (Task task : dateTasks) {
                            if (category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())) {
                                count++;
                            }
                        }
                        chart.setTaskCount(count);
                        return Mono.just(chart);
                    });
                }else{
                    return Mono.empty();
                }
            }).collectList().flatMapMany(l -> {
                l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
                return Flux.fromIterable(l);
            }).flatMapSequential(Mono::just).collectList().flatMapMany(l -> {
                ConstructDTO constructDTO = new ConstructDTO();
                constructDTO.setCid(key);
                constructDTO.setTitle(weeks.get(key).getBegin().getDayOfMonth() + "-" + weeks.get(key).getEnd().getDayOfMonth() + " " + MonthConverter.convert(weeks.get(key).getEnd().getMonth()));
                constructDTO.setCharts(l);
                return Mono.just(constructDTO);
            });
            chartCategoryFluxWeek = Flux.merge(chartCategoryFluxWeek,chartFluxWeek);
        }

        /**
         * CATEGORY MONTH
         */

        int year2 = LocalDate.now().getYear();
        LocalDate earlyYear2 = LocalDate.of(year2,1,1);
        LocalDate startMonth2 = earlyYear2;
        Map<Integer, DateDTO> months = new HashMap<>();
        int monthCount = 1;
        while (!earlyYear2.equals(LocalDate.now())){
            earlyYear2 = earlyYear2.plusDays(1);
            if(earlyYear2.getDayOfMonth() == 1){
                DateDTO dateDTO = new DateDTO();
                dateDTO.setBegin(startMonth2);
                dateDTO.setEnd(earlyYear2.minusDays(1));
                months.put(monthCount,dateDTO);
                startMonth2 = earlyYear2;
                monthCount++;
            }
        }
        if(!startMonth2.equals(LocalDate.now())){
            DateDTO dateDTO = new DateDTO();
            dateDTO.setBegin(startMonth2);
            dateDTO.setEnd(LocalDate.now());
            months.put(monthCount,dateDTO);
        }

        Flux<ConstructDTO> chartCategoryFluxMonth = Flux.empty();
        for(Integer key : months.keySet()){
            Flux<ConstructDTO> chartFluxMonth = troubleTicketService.getAllCategories().flatMap(category -> {
                if(category.getDepartmentRole() == user.getDepartmentRole()){
                    ChartDTO chart = new ChartDTO();
                    chart.setTitle(category.getName());
                    chart.setCId(category.getId());
                    chart.setStatus("Неделя " + key);
                    chart.setDescription(category.getDescription());
                    return taskService.getFromDate(months.get(key)).collectList().flatMap(dateTasks -> {
                        int count = 0;
                        for (Task task : dateTasks) {
                            if (category.getTroubleIds().stream().anyMatch(troubleId -> troubleId == task.getTroubleId())) {
                                count++;
                            }
                        }
                        chart.setTaskCount(count);
                        return Mono.just(chart);
                    });
                }else{
                    return Mono.empty();
                }
            }).collectList().flatMapMany(l -> {
                l = l.stream().sorted(Comparator.comparing(ChartDTO::getCId)).collect(Collectors.toList());
                return Flux.fromIterable(l);
            }).flatMapSequential(Mono::just).collectList().flatMapMany(l -> {
                ConstructDTO constructDTO = new ConstructDTO();
                constructDTO.setCid(key);
                constructDTO.setTitle(MonthConverter.convert(months.get(key).getBegin().getMonth()));
                constructDTO.setCharts(l);
                return Mono.just(constructDTO);
            });
            chartCategoryFluxMonth = Flux.merge(chartCategoryFluxMonth,chartFluxMonth);
        }

        /**
         * TROUBLES CHART - NOT WORK NOW!
         */

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
                        .modelAttribute("categoryTitle", troubleTicketService.getAllCategories().collectList().flatMap(l -> {
                            l = l.stream().sorted(Comparator.comparing(Category::getId)).collect(Collectors.toList());
                            l = l.stream().filter(c -> c.getDepartmentRole() == user.getDepartmentRole()).toList();
                            return Mono.just(l);
                        }))
                        .modelAttribute("categoryChartDay", chartCategoryFluxDay)
                        .modelAttribute("categoryChartWeek", chartCategoryFluxWeek.collectList().flatMap(l -> {
                            l = l.stream().sorted(Comparator.comparing(ConstructDTO::getCid)).collect(Collectors.toList());
                            return Mono.just(l);
                        }))
                        .modelAttribute("categoryChartMonth", chartCategoryFluxMonth.collectList().flatMap(l -> {
                            l = l.stream().sorted(Comparator.comparing(ConstructDTO::getCid)).collect(Collectors.toList());
                            return Mono.just(l);
                        }))
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

    @GetMapping("/profile")
    public Mono<Rendering> profilePage(@AuthenticationPrincipal Implementer implementer){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Profile page")
                        .modelAttribute("index","profile-page")
                        .modelAttribute("user", implementer)
                        .modelAttribute("pass", new PasswordDTO())
                        .build()
        );
    }

    @PostMapping("/password/change")
    public Mono<Rendering> changePassword(@AuthenticationPrincipal Implementer implementer, @ModelAttribute(name = "pass") @Valid PasswordDTO passwordDTO, Errors errors){
        passwordValidation.checkOldPassword(passwordDTO,errors,implementer);
        passwordValidation.validate(passwordDTO,errors);
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Profile page")
                            .modelAttribute("index","profile-page")
                            .modelAttribute("user", implementer)
                            .modelAttribute("pass", passwordDTO)
                            .build()
            );
        }
        return implementerService.updateUserPassword(implementer.getId(), passwordDTO).flatMap(impl -> {
            log.info("user password updated! " + impl.getPassword());
            return Mono.just(Rendering.redirectTo("/profile").build());
        });
    }
}
