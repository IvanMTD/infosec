package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Data
@RequiredArgsConstructor
public class Implementer {
    @Id
    private int id;
    /**
     * Описание пользователя
     */
    private String email;
    private String password;
    private String firstname;
    private String middleName;
    private String lastname;
    private String officePosition;
    /**
     * Связные модели
     */
    private Set<Integer> taskIds;
    private Set<Integer> rolesIds;

    public void addTask(Task task){
        taskIds.add(task.getId());
    }

    public void addRole(Role role){
        rolesIds.add(role.getId());
    }
}
