package net.security.infosec.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.entity.Task;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Data
@NoArgsConstructor
public class TaskDTO {
    private int id;
    @NotBlank(message = "Поле не может быть пустым!")
    private String title;
    @NotBlank(message = "Поле не может быть пустым!")
    private String content;
    private String username;
    private int implementerId;
    private int categoryId;
    private CategoryDTO category;
    private int troubleId;
    private TroubleDTO trouble;
    @NotNull(message = "Укажите дату!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate placedAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public TaskDTO(Task task) {
        setId(task.getId());
        setTroubleId(task.getTroubleId());
        setTitle(task.getTitle());
        setContent(task.getDescription());
        setPlacedAt(task.getExecuteDate());
        setImplementerId(task.getImplementerId());
        setStartTime(task.getStartTime());
        setEndTime(task.getEndTime());
    }

    public String getDate(){
        Locale russian = Locale.forLanguageTag("ru-RU");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return placedAt.getDayOfWeek().getDisplayName(TextStyle.FULL, russian) + " " + formatter.format(placedAt);
    }

    public String getTimeInfo(){
        if(startTime == null || endTime == null) return null;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if(totalMinutes < 0) return null;
        long days = totalMinutes / 1440;
        long hours = (totalMinutes % 1440) / 60;
        long mins = totalMinutes % 60;
        StringBuilder sb = new StringBuilder();
        if(days > 0) sb.append(days).append("д. ");
        if(hours > 0 || days > 0) sb.append(hours).append("ч. ");
        sb.append(mins).append("м.");
        return "🕐 " + startTime.format(fmt) + " → " + endTime.format(fmt) + " (" + sb + ")";
    }
}
