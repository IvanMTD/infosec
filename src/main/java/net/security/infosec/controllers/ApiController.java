package net.security.infosec.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.EmployeeDTO;
import net.security.infosec.dto.PersonDTO;
import net.security.infosec.services.EmployeeService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final EmployeeService employeeService;

    @PostMapping("/add/employee")
    public Mono<EmployeeDTO> addEmployee(@RequestBody PersonDTO personDTO){
        log.info("controller request on \"/api/add/employee\" with data [{}]",personDTO);
        return employeeService.create(personDTO).flatMap(saved -> {
            log.info("data saved [{}]",saved);
            return Mono.just(new EmployeeDTO(saved));
        });
    }

    @GetMapping("/search/free/employees")
    public Flux<EmployeeDTO> searchFreeEmployees(@RequestParam(name = "query") String query){
        return employeeService.searchFreeEmployees(query).flatMap(employee -> Mono.just(new EmployeeDTO(employee)));
    }
}
