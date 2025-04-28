package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.DateDTO;
import net.security.infosec.dto.TaskDTO;
import net.security.infosec.dto.TaskDataTransferObject;
import net.security.infosec.models.DepartmentRole;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.repositories.TaskRepository;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Flux<Task> getImplementerTasksForWeek(Implementer implementer) {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            if(localDate.getDayOfWeek() == DayOfWeek.SUNDAY){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        return taskRepository.findTasksByExecuteDateAfter(localDate).flatMap(task -> {
            if(task.getImplementerId() == implementer.getId()){
                return Mono.just(task);
            }else{
                return Mono.empty();
            }
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getImplementerTasksForMonth(Implementer implementer) {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            if(localDate.getDayOfMonth() == 1){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        localDate = localDate.minusDays(1);
        return taskRepository.findTasksByExecuteDateAfter(localDate).flatMap(task -> {
            if(task.getImplementerId() == implementer.getId()){
                return Mono.just(task);
            }else{
                return Mono.empty();
            }
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getImplementerTasksForYear(Implementer implementer) {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            if(localDate.getDayOfYear() == 1){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        localDate = localDate.minusDays(1);
        return taskRepository.findTasksByExecuteDateAfter(localDate).flatMap(task -> {
            if(task.getImplementerId() == implementer.getId()){
                return Mono.just(task);
            }else{
                return Mono.empty();
            }
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getImplementerTasksForDates(Implementer implementer, DateDTO dateDTO) {
        return taskRepository.findAllByExecuteDateBetween(dateDTO.getBegin(), dateDTO.getEnd()).flatMap(task -> {
            if(task.getImplementerId() == implementer.getId()){
                return Mono.just(task);
            }else{
                return Mono.empty();
            }
        }).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getWeek() {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            if(localDate.getDayOfWeek() == DayOfWeek.SUNDAY){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        return taskRepository.findTasksByExecuteDateAfter(localDate).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getMonth() {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            if(localDate.getDayOfMonth() == 1){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        localDate = localDate.minusDays(1);
        return taskRepository.findTasksByExecuteDateAfter(localDate).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getYear() {
        LocalDate localDate = LocalDate.now();
        boolean over = false;
        while (!over){
            if(localDate.getDayOfYear() == 1){
                over = true;
            }else{
                localDate = localDate.minusDays(1);
            }
        }
        localDate = localDate.minusDays(1);
        return taskRepository.findTasksByExecuteDateAfter(localDate).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getFromDate(DateDTO dateDTO) {
        return taskRepository.findAllByExecuteDateBetween(dateDTO.getBegin(), dateDTO.getEnd()).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getImplementerTasks(Implementer implementer) {
        return taskRepository.findAllByIdIn(implementer.getTaskIds()).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Task> getTaskById(Integer taskId) {
        return taskRepository.findById(taskId);
    }

    public Flux<Task> getTasksByLocalDate(LocalDate localDate) {
        return taskRepository.findTasksByExecuteDate(localDate).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate)).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<TaskDTO> getTaskDTO(int tid) {
        return taskRepository.findById(tid).flatMap(task -> {
            TaskDTO taskDTO = new TaskDTO(task);
            return Mono.just(taskDTO);
        });
    }

    public Mono<Task> updateTask(TaskDTO taskDTO) {
        return taskRepository.findById(taskDTO.getId()).flatMap(task -> {
            task.update(taskDTO);
            return taskRepository.save(task);
        });
    }
}
