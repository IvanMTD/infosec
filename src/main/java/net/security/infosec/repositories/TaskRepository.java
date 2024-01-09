package net.security.infosec.repositories;

import net.security.infosec.models.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TaskRepository extends ReactiveCrudRepository<Task,Integer> {

}
