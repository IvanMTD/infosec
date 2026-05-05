package net.security.infosec.controllers.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.dto.*;
import net.security.infosec.models.entity.*;
import net.security.infosec.services.DepartmentService;
import net.security.infosec.services.DivisionService;
import net.security.infosec.services.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final DivisionService divisionService;

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/add/employee")
    public Mono<EmployeeDTO> addEmployee(@RequestBody PersonDTO personDTO){
        log.info("controller request on \"/api/add/employee\" with data [{}]",personDTO);
        return employeeService.setup(personDTO).flatMap(saved -> {
            log.info("data saved [{}]",saved);
            return Mono.just(new EmployeeDTO(saved));
        });
    }

    @GetMapping("/search/free/employees")
    public Flux<EmployeeDTO> searchFreeEmployees(@RequestParam(name = "query") String query){
        return employeeService.searchFreeEmployees(query).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Employee::getFullName)).collect(Collectors.toList());
            if(l.size() > 10){
                return Flux.fromIterable(l).take(10);
            }else{
                return Flux.fromIterable(l);
            }
        }).flatMapSequential(employee -> Mono.just(new EmployeeDTO(employee)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @GetMapping("/pinout/employee")
    public Mono<EmployeeDTO> pinOut(@RequestParam(name = "employeeId") long id){
        return employeeService.pinOut(id).flatMap(employee -> {
            log.info("employee pin out [{}]",employee);
            return Mono.just(new EmployeeDTO(employee));
        });
    }

    @GetMapping("/search/employees")
    public Flux<EmployeeDTO> searchEmployees(@RequestParam(name = "search") String search){
        return employeeService.findBySearchData(search).flatMap(employee -> {
           if(employee.getDepartmentId() == 0 && employee.getDivisionId() == 0){
               return Mono.empty();
           }else{
               return Mono.just(new EmployeeDTO(employee));
           }
        });
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @GetMapping("/auth/employee/update")
    public Mono<EmployeeDTO> updateEmployee(@ModelAttribute(name = "employee") EmployeeDTO employeeDTO){
        return employeeService.getBy(employeeDTO.getId()).flatMap(employee -> {
            employee.setNumber(employeeDTO.getNumber());
            employee.setLastname(employeeDTO.getLastname());
            employee.setName(employeeDTO.getName());
            employee.setMiddleName(employeeDTO.getMiddleName());
            employee.setAddress(employeeDTO.getAddress());
            employee.setCabinet(employeeDTO.getCabinet());
            employee.setPhone(employeeDTO.getPhone());
            employee.setPersonalPhone(employeeDTO.getPersonalPhone());
            employee.setPosition(employeeDTO.getPosition());
            employee.setEmail(employeeDTO.getEmail());
            return employeeService.save(employee).flatMap(e -> Mono.just(new EmployeeDTO(e)));
        });
    }

    @GetMapping("/guide/pdf")
    public Mono<String> guidePDF(@ModelAttribute List<DivisionNode> list){
        log.info("incoming {}: ",list);
        return Mono.just("PDF");
    }

    // ==================== DEPARTMENT CRUD ====================

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/department/add")
    public Mono<DepartmentDTO> addDepartment(@RequestBody Department department){
        log.info("add department [{}]", department);
        return departmentService.create(department).flatMap(saved ->
            Mono.just(new DepartmentDTO(saved))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/department/edit")
    public Mono<DepartmentDTO> editDepartment(@RequestBody Department department){
        log.info("edit department [{}]", department);
        return departmentService.update(department).flatMap(updated ->
            Mono.just(new DepartmentDTO(updated))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @DeleteMapping("/department/{id}")
    public Mono<String> deleteDepartment(@PathVariable long id){
        log.info("delete department id=[{}]", id);
        return departmentService.getBy(id).flatMap(department -> {
            // удаляем все отделы департамента
            return divisionService.getAllByDepartmentId(department.getId()).flatMap(division ->
                employeeService.removeDivision(division).collectList().flatMap(employees -> {
                    log.info("unpinned {} employees from division [{}]", employees.size(), division.getId());
                    return divisionService.deleteBy(division.getId());
                })
            ).collectList().flatMap(divisions -> {
                log.info("deleted {} divisions", divisions.size());
                // отвязываем сотрудников департамента
                return employeeService.removeDepartment(department).collectList().flatMap(employees -> {
                    log.info("unpinned {} employees from department", employees.size());
                    return departmentService.deleteBy(department.getId());
                });
            });
        }).flatMap(deleted -> Mono.just("OK"));
    }

    // ==================== DIVISION CRUD ====================

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/division/add")
    public Mono<DivisionDTO> addDivision(@RequestBody Division division){
        log.info("add division [{}]", division);
        return divisionService.create(division).flatMap(saved ->
            departmentService.addDivision(saved).flatMap(department ->
                Mono.just(new DivisionDTO(saved))
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/division/edit")
    public Mono<DivisionDTO> editDivision(@RequestBody Division division){
        log.info("edit division [{}]", division);
        return divisionService.getBy(division.getId()).flatMap(original ->
            divisionService.update(division).flatMap(updated ->
                departmentService.resetDivision(original, updated).flatMap(dep ->
                    Mono.just(new DivisionDTO(updated))
                )
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @DeleteMapping("/division/{id}")
    public Mono<String> deleteDivision(@PathVariable long id){
        log.info("delete division id=[{}]", id);
        return divisionService.getBy(id).flatMap(division ->
            employeeService.removeDivision(division).collectList().flatMap(employees -> {
                log.info("unpinned {} employees from division", employees.size());
                return divisionService.deleteBy(division.getId()).flatMap(deleted ->
                    departmentService.removeDivision(deleted).flatMap(dep -> {
                        log.info("division removed from department [{}]", dep.getId());
                        return Mono.just("OK");
                    })
                );
            })
        );
    }

    // ==================== REORDER ====================

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/department/reorder")
    public Mono<String> reorderDepartments(@RequestBody List<Department> departments){
        return Flux.fromIterable(departments).flatMap(d ->
            departmentService.getBy(d.getId()).flatMap(original -> {
                original.setNumber(d.getNumber());
                return departmentService.create(original);
            })
        ).collectList().flatMap(l -> Mono.just("OK"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/division/reorder")
    public Mono<String> reorderDivisions(@RequestBody List<Division> divisions){
        return Flux.fromIterable(divisions).flatMap(d ->
            divisionService.getBy(d.getId()).flatMap(original -> {
                original.setNumber(d.getNumber());
                return divisionService.create(original);
            })
        ).collectList().flatMap(l -> Mono.just("OK"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GUIDE_ADMIN')")
    @PostMapping("/employee/reorder")
    public Mono<String> reorderEmployees(@RequestBody List<Employee> employees){
        return Flux.fromIterable(employees).flatMap(e ->
            employeeService.getBy(e.getId()).flatMap(original -> {
                original.setNumber(e.getNumber());
                return employeeService.save(original);
            })
        ).collectList().flatMap(l -> Mono.just("OK"));
    }
}
