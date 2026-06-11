package net.security.infosec.models.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.dto.TaskDTO;
import net.security.infosec.models.dto.TaskDataTransferObject;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.data.relational.core.mapping.Table;

@Data
@RequiredArgsConstructor
@Table("task")
public class Task {
    @Id
    private int id;
    private String title;
    private String description;
    private LocalDate executeDate;
    private int troubleId;
    private int implementerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Task(TaskDataTransferObject dto) {
        setTitle(dto.getTitle());
        setDescription(dto.getDescription());
        setTroubleId(dto.getTroubleId());
        setImplementerId(dto.getImplementerId());
        setStartTime(dto.getStartTime());
        setEndTime(dto.getEndTime());
        if(dto.getExecuteDate() != null){
            setExecuteDate(dto.getExecuteDate());
        }else if(dto.getStartTime() != null){
            setExecuteDate(dto.getStartTime().toLocalDate());
        }else{
            setExecuteDate(LocalDate.now());
        }
    }

    public void addTrouble(Trouble trouble){
        troubleId = trouble.getId();
    }

    public void addImplementer(Implementer implementer){
        implementerId = implementer.getId();
    }

    public String getDate(){
        Locale russian = Locale.forLanguageTag("ru-RU");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return executeDate.getDayOfWeek().getDisplayName(TextStyle.FULL, russian) + " " + formatter.format(executeDate);
    }

    public void update(TaskDTO taskDTO) {
        setTitle(taskDTO.getTitle());
        setDescription(taskDTO.getContent());
        if(taskDTO.getPlacedAt() != null){
            setExecuteDate(taskDTO.getPlacedAt());
        }else if(taskDTO.getStartTime() != null){
            setExecuteDate(taskDTO.getStartTime().toLocalDate());
        }
        setTroubleId(taskDTO.getTroubleId());
        setImplementerId(taskDTO.getImplementerId());
        setStartTime(taskDTO.getStartTime());
        setEndTime(taskDTO.getEndTime());
    }
}
