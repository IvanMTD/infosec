package net.security.infosec.repositories;

import net.security.infosec.models.Implementer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ImplementerRepository extends ReactiveCrudRepository<Implementer,Integer> {
    Mono<Implementer> findByEmail(String email);
}
