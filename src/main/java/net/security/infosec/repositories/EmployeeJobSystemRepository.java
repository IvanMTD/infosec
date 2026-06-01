package net.security.infosec.repositories;

import net.security.infosec.models.entity.EmployeeJobSystem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface EmployeeJobSystemRepository extends ReactiveCrudRepository<EmployeeJobSystem, Void> {

    Flux<EmployeeJobSystem> findAllByJobSystemUuid(UUID jobSystemUuid);
}
