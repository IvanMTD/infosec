package net.security.infosec.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    @GetMapping("/")
    public Mono<Rendering> homePage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("index","home-page")
                        .modelAttribute("title","Home Page")
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
