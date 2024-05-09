package net.security.infosec.repositories;

import net.security.infosec.models.Division;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Set;

public interface DivisionRepository extends ReactiveCrudRepository<Division,Long> {
    Flux<Division> findAllByIdIn(Set<Long> divisionIds);
}
