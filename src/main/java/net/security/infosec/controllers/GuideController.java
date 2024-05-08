package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.Department;
import net.security.infosec.models.Employee;
import net.security.infosec.services.DepartmentService;
import net.security.infosec.services.DivisionService;
import net.security.infosec.services.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/guide")
public class GuideController {

    private final DepartmentService departmentService;
    private final DivisionService divisionService;
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

    @GetMapping("/employee/add/form")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> addEmployeePage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Add employee")
                        .modelAttribute("index","employee-add-page")
                        .modelAttribute("employee",new Employee())
                        .build()
        );
    }

    @PostMapping("/employee/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> addEmployee(@ModelAttribute(name = "employee") @Valid Employee employee, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Add employee")
                            .modelAttribute("index","employee-add-page")
                            .modelAttribute("employee",employee)
                            .build()
            );
        }else{
            return employeeService.create(employee).flatMap(saved -> {
                log.info("employee has been saved [{}]", saved);
                return Mono.just(Rendering.redirectTo("/admin/employees").build());
            });
        }
    }

    @GetMapping("/department/add/form")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> addDepartmentPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Add structure")
                        .modelAttribute("index","structure-add-page")
                        .modelAttribute("department",new Department())
                        .build()
        );
    }
}
