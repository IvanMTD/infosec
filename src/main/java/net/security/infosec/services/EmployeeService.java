package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.Department;
import net.security.infosec.models.Division;
import net.security.infosec.models.Employee;
import net.security.infosec.repositories.EmployeeRepository;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Flux<Employee> getAll() {
        return employeeRepository.findAll().collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Employee::getFullName)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Employee> create(Employee employee) {
        return Mono.just(employee).flatMap(e -> {
            long department = e.getDepartmentId();
            long division = e.getDivisionId();
            if(division != 0){
                department = 0;
            }else{
                division = 0;
            }
            e.setDepartmentId(department);
            e.setDivisionId(division);
            return employeeRepository.save(e);
        });
    }

    public Mono<Employee> getBy(long id) {
        return employeeRepository.findById(id);
    }

    public Mono<Employee> update(Employee employee) {
        return employeeRepository.findById(employee.getId()).flatMap(original -> {
            original.update(employee);
            return employeeRepository.save(original);
        });
    }

    public Mono<Employee> deleteBy(long id) {
        return employeeRepository.findById(id).flatMap(employee -> employeeRepository.delete(employee).then(Mono.just(employee)));
    }

    public Flux<Employee> removeDivision(Division division) {
        return employeeRepository.findByDivisionId(division.getId()).flatMap(employee -> {
            employee.setDivisionId(0);
            return employeeRepository.save(employee);
        }).switchIfEmpty(Flux.fromIterable(new ArrayList<>()));
    }

    public Flux<Employee> removeDepartment(Department deleted) {
        return employeeRepository.findByDepartmentId(deleted.getId()).flatMap(employee -> {
            employee.setDepartmentId(0);
            return employeeRepository.save(employee);
        }).switchIfEmpty(Flux.fromIterable(new ArrayList<>()));
    }

    public Flux<Employee> getAllByDepartmentId(long id) {
        return employeeRepository.findByDepartmentId(id).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Employee::getFullName)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Employee> getAllByDivisionId(long id) {
        return employeeRepository.findByDivisionId(id).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Employee::getFullName)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }
}
