package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.Department;
import net.security.infosec.models.Division;
import net.security.infosec.models.Employee;
import net.security.infosec.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public Flux<Department> getAll() {
        return departmentRepository.findAll().collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Department::getNumber)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Department> create(Department department) {
        return departmentRepository.save(department);
    }

    public Mono<Long> count() {
        return departmentRepository.count();
    }

    public Mono<Department> addDivision(Division division) {
        return departmentRepository.findById(division.getDepartmentId()).flatMap(department -> {
            department.getDivisionIds().add(division.getId());
            return departmentRepository.save(department);
        });
    }

    public Mono<Department> getBy(long id) {
        return departmentRepository.findById(id);
    }

    public Mono<Department> update(Department department) {
        return departmentRepository.findById(department.getId()).flatMap(original -> {
            original.setTitle(department.getTitle());
            original.setDescription(department.getDescription());
            original.setNumber(department.getNumber());
            return departmentRepository.save(original);
        });
    }

    public Mono<Department> resetDivision(Division original, Division division) {
        return departmentRepository.findById(original.getDepartmentId()).flatMap(before -> {
            before.getDivisionIds().remove(original.getId());
            return departmentRepository.save(before).flatMap(previousDepartment -> {
                log.info("division has been removed from department [{}]",previousDepartment);
                return departmentRepository.findById(division.getDepartmentId());
            });
        }).flatMap(after -> {
            after.getDivisionIds().add(division.getId());
            return departmentRepository.save(after).flatMap(departmentNext -> {
                log.info("division has been added in department [{}]", departmentNext);
                return Mono.just(departmentNext);
            });
        });
    }

    public Mono<Department> getByIn(long divisionId) {
        return departmentRepository.findByDivisionId(divisionId);
    }

    public Mono<Department> removeDivision(Division deleted) {
        return departmentRepository.findById(deleted.getDepartmentId()).flatMap(department -> {
            department.getDivisionIds().remove(deleted.getId());
            return departmentRepository.save(department);
        }).switchIfEmpty(Mono.just(new Department()));
    }

    public Mono<Department> deleteBy(long id) {
        return departmentRepository.findById(id).flatMap(department -> departmentRepository.delete(department).then(Mono.just(department)));
    }
}
