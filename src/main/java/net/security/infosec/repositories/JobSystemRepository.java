package net.security.infosec.repositories;

import net.security.infosec.models.entity.JobSystem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface JobSystemRepository extends ReactiveCrudRepository<JobSystem, UUID> {

    Flux<JobSystem> findAllByOrderByName();
}
