package net.security.infosec.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Task;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@RequiredArgsConstructor
public class ChartDTO {
    private LocalDate localDate;
    private String date;
    private List<Task> tasks = new ArrayList<>();
    private List<Integer> taskOnTrouble = new ArrayList<>();

    private int cId;
    private String title;
    private String status;
    private int taskCount;
    private String description;

    public void addTask(Task task){
        tasks.add(task);
    }
    public void addTaskOnTrouble(int size){
        taskOnTrouble.add(size);
    }
    public String getStringDate(LocalDate localDate){
        Locale russian = Locale.forLanguageTag("ru-RU");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return formatter.format(localDate) + "\n" + localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, russian);
    }

    public void setLocalDate(LocalDate localDate){
        this.localDate = localDate;
        date = getStringDate(this.localDate);
    }
}
