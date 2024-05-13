package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.DepartmentDTO;
import net.security.infosec.models.Division;
import net.security.infosec.repositories.DivisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DivisionService {
    private final DivisionRepository divisionRepository;

    public Flux<Division> getAll() {
        return divisionRepository.findAll().collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Division::getTitle)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Division> create(Division division) {
        return divisionRepository.save(division);
    }

    public Flux<Division> getAllBy(Set<Long> divisionIds) {
        return divisionRepository.findAllByIdIn(divisionIds);
    }

    public Mono<Division> getBy(long id) {
        return divisionRepository.findById(id);
    }

    public Mono<Division> update(Division division) {
        return divisionRepository.findById(division.getId()).flatMap(original -> {
            original.setDepartmentId(division.getDepartmentId());
            original.setTitle(division.getTitle());
            original.setDescription(division.getDescription());
            return divisionRepository.save(original);
        });
    }

    public Mono<Division> deleteBy(long id) {
        return divisionRepository.findById(id).flatMap(division -> divisionRepository.delete(division).then(Mono.just(division)));
    }
}
