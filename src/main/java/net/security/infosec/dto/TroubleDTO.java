package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.DepartmentRole;
import net.security.infosec.models.Trouble;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TroubleDTO {
    private int id;
    private String title;
    private String description;
    private DepartmentRole departmentRole;
    private List<TaskDTO> tasks = new ArrayList<>();
    private boolean show = true;

    public TroubleDTO(Trouble trouble){
        setId(trouble.getId());
        setTitle(trouble.getName());
        setDescription(trouble.getDescription());
        setDepartmentRole(trouble.getDepartmentRole());
    }

    public void addTask(TaskDTO task){
        tasks.add(task);
    }

    public void sortTasks(){
        tasks = tasks.stream().sorted(Comparator.comparing(TaskDTO::getTitle)).collect(Collectors.toList());
    }
}
