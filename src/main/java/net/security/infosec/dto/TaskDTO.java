package net.security.infosec.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Data
public class TaskDTO {
    private String title;
    private String content;
    private String username;
    private LocalDate placedAt;

    public String getDate(){
        Locale russian = Locale.forLanguageTag("ru-RU");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return placedAt.getDayOfWeek().getDisplayName(TextStyle.FULL, russian) + " " + formatter.format(placedAt);
    }
}
