package net.security.infosec.controllers.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.EmployeeDTO;
import net.security.infosec.dto.PersonDTO;
import net.security.infosec.models.Employee;
import net.security.infosec.services.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final EmployeeService employeeService;

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
}
