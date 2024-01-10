package net.security.infosec.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    @GetMapping()
    public Mono<Rendering> taskPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Task page")
                        .modelAttribute("index","task-page")
                        .build()
        );
    }
}
