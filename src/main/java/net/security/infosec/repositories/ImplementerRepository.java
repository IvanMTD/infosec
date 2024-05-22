package net.security.infosec.repositories;

import net.security.infosec.models.Implementer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface ImplementerRepository extends ReactiveCrudRepository<Implementer,Integer> {
    //Mono<Implementer> findByEmail(String email);
    Mono<UserDetails> findByEmail(String email);
}
