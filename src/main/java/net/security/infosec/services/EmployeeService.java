package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.EmployeeDTO;
import net.security.infosec.dto.PersonDTO;
import net.security.infosec.models.Department;
import net.security.infosec.models.Division;
import net.security.infosec.models.Employee;
import net.security.infosec.repositories.EmployeeRepository;
import net.security.infosec.utils.Checker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Flux<Employee> getAll() {
        return employeeRepository.findAll().collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Employee::getNumber)).collect(Collectors.toList());
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

    public Mono<Employee> setup(PersonDTO person){
        return Mono.just(new Employee(person)).flatMap(employeeRepository::save);
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
            l = l.stream().sorted(Comparator.comparing(Employee::getNumber)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Employee> getAllByDivisionId(long id) {
        return employeeRepository.findByDivisionId(id).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Employee::getNumber)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Employee> searchFreeEmployees(String query) {
        String[] searchParts = query.split(" ");
        List<Flux<Employee>> fluxes = new ArrayList<>();
        for(String part : searchParts){
            if(!part.equals("")){
                String searchData = "%" + part + "%";
                Flux<Employee> lastnameFlux = employeeRepository.findAllByLastnameLikeIgnoreCaseAndDepartmentIdAndDivisionId(searchData,0,0).switchIfEmpty(Flux.empty());
                Flux<Employee> nameFlux = employeeRepository.findAllByNameLikeIgnoreCaseAndDepartmentIdAndDivisionId(searchData,0,0).switchIfEmpty(Flux.empty());
                Flux<Employee> middleNameFlux = employeeRepository.findAllByMiddleNameLikeIgnoreCaseAndDepartmentIdAndDivisionId(searchData,0,0).switchIfEmpty(Flux.empty());
                fluxes.addAll(Arrays.asList(lastnameFlux,nameFlux,middleNameFlux));
            }
        }

        return filterEmployees(fluxes,searchParts);
    }

    private Flux<Employee> filterEmployees(List<Flux<Employee>> fluxes, String[] searchParts){
        return Flux.merge(fluxes).distinct().flatMap(employee -> {
            int check = 0;
            for(String part : searchParts){
                if (employee.getLastname().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getName().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getMiddleName().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
             }
            if(check >= searchParts.length){
                return Mono.just(employee);
            }else{
                return Mono.empty();
            }
        });
    }

    public Mono<Employee> pinOut(long id) {
        return employeeRepository.findById(id).flatMap(employee -> {
            employee.setDepartmentId(0);
            employee.setDivisionId(0);
            return employeeRepository.save(employee);
        });
    }

    public Flux<Employee> findBySearchData(String search) {
        String parts[] = search.split(" ");
        List<Flux<Employee>> fluxes = new ArrayList<>();
        for(String part : parts){
            String searchData = "%" + part + "%";
            fluxes.add(employeeRepository.findAllByLastnameLikeIgnoreCase(searchData));
            fluxes.add(employeeRepository.findAllByNameLikeIgnoreCase(searchData));
            fluxes.add(employeeRepository.findAllByMiddleNameLikeIgnoreCase(searchData));
            fluxes.add(employeeRepository.findAllByPositionLikeIgnoreCase(searchData));
            fluxes.add(employeeRepository.findAllByEmailLikeIgnoreCase(searchData));
            fluxes.add(employeeRepository.findAllByPhoneLikeIgnoreCase(searchData));
        }

        return globalFilterEmployees(fluxes,parts);
    }

    private Flux<Employee> globalFilterEmployees(List<Flux<Employee>> fluxes, String[] searchParts){
        return Flux.merge(fluxes).distinct().flatMap(employee -> {
            int check = 0;
            for(String part : searchParts){
                if (employee.getLastname().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getName().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getMiddleName().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getPosition().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getEmail().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
                if (employee.getPhone().toLowerCase().contains(part.toLowerCase())){
                    check++;
                }
            }
            if(check >= searchParts.length){
                return Mono.just(employee);
            }else{
                return Mono.empty();
            }
        });
    }
}
