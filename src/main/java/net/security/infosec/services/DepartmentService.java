package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.entity.Department;
import net.security.infosec.models.entity.Division;
import net.security.infosec.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;
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
        return departmentRepository.findById(division.getDepartmentId());
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
        return departmentRepository.findById(division.getDepartmentId());
    }

    public Mono<Department> getByIn(long divisionId) {
        return departmentRepository.findByDivisionId(divisionId);
    }

    public Mono<Department> removeDivision(Division deleted) {
        return departmentRepository.findById(deleted.getDepartmentId()).switchIfEmpty(Mono.just(new Department()));
    }

    public Mono<Department> deleteBy(long id) {
        return departmentRepository.findById(id).flatMap(department -> departmentRepository.delete(department).then(Mono.just(department)));
    }
}
