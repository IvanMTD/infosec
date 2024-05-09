package net.security.infosec.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.DepartmentDTO;
import net.security.infosec.dto.DivisionDTO;
import net.security.infosec.models.Department;
import net.security.infosec.models.Division;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

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
                        .modelAttribute("departmentList", getCompleteDepartments())
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
                            .modelAttribute("departmentList", getCompleteDepartments())
                            .build()
            );
        }else{
            return employeeService.create(employee).flatMap(saved -> {
                log.info("employee has been saved [{}]", saved);
                return Mono.just(Rendering.redirectTo("/admin/employees").build());
            });
        }
    }

    private Flux<DepartmentDTO> getCompleteDepartments(){
        return departmentService.getAll().flatMap(department -> getCompleteDepartment(department.getId()));
    }

    private Mono<DepartmentDTO> getCompleteDepartment(long id){
        return departmentService.getBy(id).flatMap(department -> {
            DepartmentDTO departmentDTO = new DepartmentDTO(department);
            return divisionService.getAllBy(department.getDivisionIds()).flatMap(division -> {
                DivisionDTO divisionDTO = new DivisionDTO(division);
                return Mono.just(divisionDTO);
            }).collectList().flatMap(l -> {
                l = l.stream().sorted(Comparator.comparing(DivisionDTO::getTitle)).collect(Collectors.toList());
                departmentDTO.setDivisions(l);
                return Mono.just(departmentDTO);
            });
        });
    }

    @GetMapping("/division/add/form")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> addDivisionPage(){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Add division")
                        .modelAttribute("index","division-add-page")
                        .modelAttribute("departmentList", departmentService.getAll())
                        .modelAttribute("division",new Division())
                        .build()
        );
    }

    @PostMapping("/division/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> addDivision(@ModelAttribute(name = "division") @Valid Division division, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Add division")
                            .modelAttribute("index","division-add-page")
                            .modelAttribute("departmentList", departmentService.getAll())
                            .modelAttribute("division", division)
                            .build()
            );
        }else{
            return divisionService.create(division).flatMap(saved -> {
                log.info("sub-structure saved [{}]",saved);
                return departmentService.addDivision(saved).flatMap(updated -> {
                    log.info("department updated [{}]",updated);
                    return Mono.just(Rendering.redirectTo("/admin/divisions").build());
                });
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

    @PostMapping("/department/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> addDepartment(@ModelAttribute(name = "department") @Valid Department department, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Add structure")
                            .modelAttribute("index","structure-add-page")
                            .modelAttribute("department",department)
                            .build()
            );
        }else{
            return departmentService.create(department).flatMap(saved -> {
                log.info("structure saved [{}]",saved);
                return Mono.just(Rendering.redirectTo("/admin/departments").build());
            });
        }
    }
}
