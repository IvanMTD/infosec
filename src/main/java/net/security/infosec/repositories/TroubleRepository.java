package net.security.infosec.repositories;

import net.security.infosec.models.Trouble;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Set;

public interface TroubleRepository extends ReactiveCrudRepository<Trouble,Integer> {
    Flux<Trouble> findAllByIdIn(Set<Integer> ids);
}
