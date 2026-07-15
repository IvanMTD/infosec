package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.dto.DateDTO;
import net.security.infosec.models.dto.KpiEntry;
import net.security.infosec.models.dto.TaskDTO;
import net.security.infosec.models.dto.TaskDataTransferObject;
import net.security.infosec.models.entity.Implementer;
import net.security.infosec.models.entity.Task;
import net.security.infosec.repositories.ImplementerRepository;
import net.security.infosec.repositories.TaskRepository;
import net.security.infosec.repositories.TroubleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private static final long WORK_MINUTES_PER_DAY = 7 * 60; // 8-часовой день минус 1 час обеда

    private final TaskRepository taskRepository;
    private final ImplementerRepository implementerRepository;
    private final TroubleRepository troubleRepository;

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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getFromDate(DateDTO dateDTO) {
        return taskRepository.findAllByExecuteDateBetween(dateDTO.getBegin(), dateDTO.getEnd()).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Flux<Task> getImplementerTasks(Implementer implementer) {
        return taskRepository.findAllByImplementerId(implementer.getId()).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
            return Flux.fromIterable(l);
        }).flatMapSequential(Mono::just);
    }

    public Mono<Task> getTaskById(Integer taskId) {
        return taskRepository.findById(taskId);
    }

    public Flux<Task> getTasksByLocalDate(LocalDate localDate) {
        return taskRepository.findTasksByExecuteDate(localDate).collectList().flatMapMany(l -> {
            l = l.stream().sorted(Comparator.comparing(Task::getExecuteDate).reversed()).collect(Collectors.toList());
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

    /* ==================== KPI ==================== */
    public Flux<KpiEntry> getKpiData(String departmentRole, int employeeId, LocalDate from, LocalDate to) {
        return taskRepository.findAll()
            .filter(t -> t.getStartTime() != null && t.getEndTime() != null
                && t.getStartTime().isBefore(t.getEndTime())
                && !t.getStartTime().toLocalDate().isAfter(to)
                && !t.getEndTime().toLocalDate().isBefore(from))
            .filter(t -> {
                if (employeeId > 0) return t.getImplementerId() == employeeId;
                return true;
            })
            .flatMap(task -> {
                LocalDate startDate = task.getStartTime().toLocalDate();
                LocalDate endDate = task.getEndTime().toLocalDate();
                LocalTime startTime = task.getStartTime().toLocalTime();
                LocalTime endTime = task.getEndTime().toLocalTime();
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

                return implementerRepository.findById(task.getImplementerId())
                    .flatMapMany(implementer -> {
                        // фильтр по departmentRole
                        if (departmentRole != null && !"ALL".equals(departmentRole)) {
                            if (!departmentRole.equals(implementer.getDepartmentRole().name())) {
                                return Flux.empty();
                            }
                        }
                        LocalTime workDayEnd = implementer.getWorkDayEnd() != null
                            ? implementer.getWorkDayEnd()
                            : LocalTime.of(18, 0);
                        LocalTime workDayStart = workDayEnd.minusHours(7);

                        return troubleRepository.findById(task.getTroubleId())
                            .defaultIfEmpty(new net.security.infosec.models.entity.Trouble())
                            .flatMapMany(trouble -> {
                                List<KpiEntry> entries = new ArrayList<>();
                                String troubleName = trouble.getName() != null ? trouble.getName() : "";
                                for (long i = 0; i < days; i++) {
                                    LocalDate date = startDate.plusDays(i);
                                    if (date.isBefore(from) || date.isAfter(to)) continue;

                                    long dayMinutes;
                                    if (days == 1) {
                                        dayMinutes = overlapMinutes(startTime, endTime, workDayStart, workDayEnd);
                                    } else if (i == 0) {
                                        dayMinutes = overlapMinutes(startTime, workDayEnd, workDayStart, workDayEnd);
                                    } else if (i == days - 1) {
                                        dayMinutes = overlapMinutes(workDayStart, endTime, workDayStart, workDayEnd);
                                    } else {
                                        dayMinutes = WORK_MINUTES_PER_DAY;
                                    }

                                    if (dayMinutes > 0) {
                                        entries.add(new KpiEntry(
                                            task.getImplementerId(),
                                            implementer.getFullName(),
                                            date,
                                            task.getTroubleId(),
                                            troubleName,
                                            "",
                                            dayMinutes
                                        ));
                                    }
                                }
                                return Flux.fromIterable(entries);
                            });
                    });
            });
    }

    private long overlapMinutes(LocalTime from, LocalTime to, LocalTime workStart, LocalTime workEnd) {
        LocalTime effectiveFrom = from.isAfter(workStart) ? from : workStart;
        LocalTime effectiveTo = to.isBefore(workEnd) ? to : workEnd;
        if (!effectiveFrom.isBefore(effectiveTo)) return 0;
        return ChronoUnit.MINUTES.between(effectiveFrom, effectiveTo);
    }
}
