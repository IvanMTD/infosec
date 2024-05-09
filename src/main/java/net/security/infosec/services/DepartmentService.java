package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.DepartmentDTO;
import net.security.infosec.models.Department;
import net.security.infosec.models.Division;
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
            l = l.stream().sorted(Comparator.comparing(Department::getTitle)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Department> create(Department department) {
        return departmentRepository.save(department);
    }

    public Mono<Long> count() {
        return departmentRepository.count();
    }

    public Mono<Department> addDivision(Division saved) {
        return departmentRepository.findById(saved.getDepartmentId()).flatMap(department -> {
            department.getDivisionIds().add(saved.getId());
            return departmentRepository.save(department);
        });
    }

    public Mono<Department> getBy(long id) {
        return departmentRepository.findById(id);
    }
}
