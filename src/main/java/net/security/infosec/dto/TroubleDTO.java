package net.security.infosec.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TroubleDTO {
    private int id;
    private String title;
    private String description;
    private List<TaskDTO> tasks = new ArrayList<>();

    public void addTask(TaskDTO task){
        tasks.add(task);
    }
}
