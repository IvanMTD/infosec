package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class Trouble {
    @Id
    private int id;
    /**
     * Описание модели
     */
    private String name;
    private String description;
    /**
     * Связные модели
     */
    private Set<Integer> taskIds = new HashSet<>();

    public void addTask(Task task){
        taskIds.add(task.getId());
    }
}
