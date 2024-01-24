package net.security.infosec.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ChartDTO {
    private LocalDate localDate;
    private List<Task> tasks = new ArrayList<>();

    public void addTask(Task task){
        tasks.add(task);
    }
}
