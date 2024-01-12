package net.security.infosec.repositories;

import net.security.infosec.models.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Set;

public interface TaskRepository extends ReactiveCrudRepository<Task,Integer> {
    Flux<Task> findAllByIdIn(Set<Integer> ids);
}
