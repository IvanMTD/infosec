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

import java.time.DayOfWeek;
import java.time.LocalDate;

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

    public Flux<Task> getWeek() {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            System.out.println(localDate.getDayOfWeek());
            if(localDate.getDayOfWeek() == DayOfWeek.SUNDAY){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        return taskRepository.findTasksByExecuteDateAfter(localDate);
    }

    public Flux<Task> getImplementerTasks(Implementer implementer) {
        return taskRepository.findAllByIdIn(implementer.getTaskIds());
    }

    public Mono<Task> getTaskById(Integer taskId) {
        return taskRepository.findById(taskId);
    }
}
