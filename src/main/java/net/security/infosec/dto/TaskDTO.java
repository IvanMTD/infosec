package net.security.infosec.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Task;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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

    public TaskDTO(Task task) {
        setId(task.getId());
        setTroubleId(task.getTroubleId());
        setTitle(task.getTitle());
        setContent(task.getDescription());
        setPlacedAt(task.getExecuteDate());
        setImplementerId(task.getImplementerId());
    }

    public String getDate(){
        Locale russian = Locale.forLanguageTag("ru-RU");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return placedAt.getDayOfWeek().getDisplayName(TextStyle.FULL, russian) + " " + formatter.format(placedAt);
    }
}
