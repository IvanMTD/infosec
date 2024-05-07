package net.security.infosec.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.services.EmployeeService;
import net.security.infosec.services.ServiceForService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/guide")
public class GuideController {

    private final ServiceForService service;
    private final EmployeeService employeeService;

    @GetMapping("/list")
    public Mono<Rendering> showGuide(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Guide page")
                        .modelAttribute("index","guide-page")
                        .build()
        );
    }
}
