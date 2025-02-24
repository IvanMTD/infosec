package net.security.infosec.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/instruction")
public class InstructionController {
    @GetMapping("/list")
    public Mono<Rendering> instructionList(){
        return Mono.just(
                Rendering.view("template")
                        .build()
        );
    }
}
