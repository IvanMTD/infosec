package net.security.infosec.repositories;

import net.security.infosec.models.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Set;

public interface TaskRepository extends ReactiveCrudRepository<Task,Integer> {
    Flux<Task> findAllByIdIn(Set<Integer> ids);
    Flux<Task> findTasksByExecuteDateAfter(LocalDate date);
    Flux<Task> findTasksByExecuteDate(LocalDate date);
}
