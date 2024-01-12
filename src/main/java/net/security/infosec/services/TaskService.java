package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.TaskDataTransferObject;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.repositories.TaskRepository;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public Mono<Task> saveTask(TaskDataTransferObject dto){
        return taskRepository.save(new Task(dto));
    }

    public Flux<Task> getAll() {
        return taskRepository.findAll();
    }

    public Flux<Task> getImplementerTasks(Implementer implementer) {
        return taskRepository.findAllByIdIn(implementer.getTaskIds());
    }
}
