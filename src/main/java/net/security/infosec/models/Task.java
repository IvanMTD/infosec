package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class Task {
    @Id
    private int id;
    /**
     * Описание модели
     */
    private String title;
    private String description;
    private LocalDate executeDate;
    /**
     * Связные модели
     */
    private int troubleId;
    private int implementerId;

    public void addTrouble(Trouble trouble){
        troubleId = trouble.getId();
    }

    public void addImplementer(Implementer implementer){
        implementerId = implementer.getId();
    }
}
