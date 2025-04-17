package net.security.infosec.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.DepartmentDTO;
import net.security.infosec.dto.DivisionDTO;
import net.security.infosec.dto.EmployeeDTO;
import net.security.infosec.dto.GuideDTO;
import net.security.infosec.models.Department;
import net.security.infosec.models.Division;
import net.security.infosec.models.Employee;
import net.security.infosec.services.DepartmentService;
import net.security.infosec.services.DivisionService;
import net.security.infosec.services.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
                        .modelAttribute("guide",createGuide())
                        .build()
        );
    }

    @GetMapping("/employee/add/form")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
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

    @GetMapping("/employee/edit/page")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> editEmployeePage(@RequestParam(name = "employee") long id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Employee edit")
                        .modelAttribute("index","employee-edit-page")
                        .modelAttribute("employee", getCompleteEmployee(id))
                        .modelAttribute("departmentList", getCompleteDepartments())
                        .build()
        );
    }

    @PostMapping("/employee/edit")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> editEmployee(@ModelAttribute(name = "employee") @Valid Employee employee, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Employee edit")
                            .modelAttribute("index","employee-edit-page")
                            .modelAttribute("employee", employee)
                            .modelAttribute("departmentList", getCompleteDepartments())
                            .build()
            );
        }else{
            log.info("try saved [{}]", employee);
            return employeeService.update(employee).flatMap(updated -> {
                log.info("employee has been updated [{}]", updated);
                return Mono.just(Rendering.redirectTo("/admin/employees").build());
            });
        }
    }

    @GetMapping("/employee/delete")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> deleteEmployee(@RequestParam(name = "employee") long id){
        return employeeService.deleteBy(id).flatMap(deleted -> {
            log.info("employee has been deleted from db [{}]", deleted);
            return Mono.just(Rendering.redirectTo("/admin/employees").build());
        });
    }

    @GetMapping("/division/add/form")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
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

    @GetMapping("/division/edit/page")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> editDivisionPage(@RequestParam(name = "division") long id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Division edit")
                        .modelAttribute("index","division-edit-page")
                        .modelAttribute("division",divisionService.getBy(id))
                        .modelAttribute("departmentList", departmentService.getAll())
                        .build()
        );
    }

    @PostMapping("/division/edit")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> editDivision(@ModelAttribute(name = "division") @Valid Division division, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Division edit")
                            .modelAttribute("index","division-edit-page")
                            .modelAttribute("division",division)
                            .modelAttribute("departmentList", departmentService.getAll())
                            .build()
            );
        }else{
            return divisionService.getBy(division.getId()).flatMap(original -> {
                log.info("found original division [{}]",original);
                return divisionService.update(division).flatMap(divisionUpdated -> {
                    log.info("division updated [{}]",divisionUpdated);
                    return departmentService.resetDivision(original,division).flatMap(departmentUpdated -> {
                        log.info("new department updated [{}]",departmentUpdated);
                        return Mono.just(Rendering.redirectTo("/admin/divisions").build());
                    });
                });
            });
        }
    }

    @GetMapping("/division/delete")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> deleteDivision(@RequestParam(name = "division") long id){
        return removeDivision(id).flatMap(deleted -> {
            return departmentService.removeDivision(deleted).flatMap(department -> {
                if(department.getId() != 0) {
                    log.info("department has updated [{}]", department);
                }
                return Mono.just(Rendering.redirectTo("/admin/divisions").build());
            });
        });
    }

    @GetMapping("/department/add/form")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
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

    @GetMapping("/department/edit/page")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> editDepartmentPage(@RequestParam(name = "department") long id){
        return Mono.just(
                Rendering.view("template")
                        .modelAttribute("title","Structure edit")
                        .modelAttribute("index","structure-edit-page")
                        .modelAttribute("department",departmentService.getBy(id))
                        .build()
        );
    }

    @PostMapping("/department/edit")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> editDepartment(@ModelAttribute(name = "department") @Valid Department department, Errors errors){
        if(errors.hasErrors()){
            return Mono.just(
                    Rendering.view("template")
                            .modelAttribute("title","Structure edit")
                            .modelAttribute("index","structure-edit-page")
                            .modelAttribute("department",department)
                            .build()
            );
        }else{
            return departmentService.update(department).flatMap(updated -> {
                log.info("structure updated [{}]",updated);
                return Mono.just(Rendering.redirectTo("/admin/departments").build());
            });
        }
    }

    @GetMapping("/department/delete")
    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    public Mono<Rendering> deleteDepartment(@RequestParam(name = "department") long id){
        return departmentService.deleteBy(id).flatMap(deleted -> {
            log.info("department deleted from db [{}]",deleted);
            return employeeService.removeDepartment(deleted).collectList().flatMap(l -> {
                if(l.size() != 0){
                    for(Employee employee : l){
                        log.info("employer has updated [{}]",employee);
                    }
                }
                return Mono.just(deleted);
            });
        }).flatMap(deleted -> {
            return removeDivisions(deleted.getDivisionIds()).collectList().flatMap(l -> {
                if(l.size() != 0){
                    for(Division division : l){
                        log.info("division has been deleted [{}]", division);
                    }
                }
                return Mono.just(Rendering.redirectTo("/admin/departments").build());
            });
        });
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
                l = l.stream().sorted(Comparator.comparing(DivisionDTO::getNumber)).collect(Collectors.toList());
                departmentDTO.setDivisions(l);
                return Mono.just(departmentDTO);
            });
        });
    }

    private Mono<Employee> getCompleteEmployee(long id){
        return employeeService.getBy(id).flatMap(original -> {
            Employee employee = new Employee(original);
            if(original.getDivisionId() != 0){
                return departmentService.getByIn(original.getDivisionId()).flatMap(department -> {
                    employee.setDepartmentId(department.getId());
                    return Mono.just(employee);
                });
            }else{
                return Mono.just(employee);
            }
        });
    }

    private Flux<Division> removeDivisions(Set<Long> ids){
        return divisionService.getAllBy(ids).flatMap(division -> removeDivision(division.getId())).switchIfEmpty(Flux.fromIterable(new ArrayList<>()));
    }

    private Mono<Division> removeDivision(long id){
        return divisionService.deleteBy(id).flatMap(deleted -> {
            log.info("division has been deleted [{}]",deleted);
            return employeeService.removeDivision(deleted).collectList().flatMap(l -> {
                if(l.size() != 0){
                    for(Employee employee : l){
                        log.info("employee has updated [{}]",employee);
                    }
                }
                return Mono.just(deleted);
            });
        });
    }

    private Mono<GuideDTO> createGuide(){
        return Mono.just(new GuideDTO()).flatMap(guide -> {
            return departmentService.getAll().flatMap(department -> {
                DepartmentDTO departmentDTO = new DepartmentDTO(department);
                return employeeService.getAllByDepartmentId(department.getId()).collectList().flatMap(l -> {
                    List<EmployeeDTO> employeeDTOList = new ArrayList<>();
                    for(Employee employee : l){
                        employeeDTOList.add(new EmployeeDTO(employee));
                    }
                    departmentDTO.setEmployees(employeeDTOList);
                    return Mono.just(departmentDTO);
                });
            }).flatMap(departmentDTO -> {
                return divisionService.getAllBy(departmentDTO.getDivisionIds()).flatMap(division -> {
                    DivisionDTO divisionDTO = new DivisionDTO(division);
                    return employeeService.getAllByDivisionId(division.getId()).collectList().flatMap(l -> {
                        List<EmployeeDTO> employeeDTOList = new ArrayList<>();
                        for(Employee employee : l){
                            employeeDTOList.add(new EmployeeDTO(employee));
                        }
                        divisionDTO.setEmployees(employeeDTOList);
                        return Mono.just(divisionDTO);
                    });
                }).collectList().flatMap(divisionDTOS -> {
                    divisionDTOS = divisionDTOS.stream().sorted(Comparator.comparing(DivisionDTO::getNumber)).collect(Collectors.toList());
                    departmentDTO.setDivisions(divisionDTOS);
                    return Mono.just(departmentDTO);
                });
            }).collectList().flatMap(l -> {
                l = l.stream().sorted(Comparator.comparing(DepartmentDTO::getNumber)).collect(Collectors.toList());
                guide.setDepartments(l);
                return Mono.just(guide);
            });
        });
    }
}
