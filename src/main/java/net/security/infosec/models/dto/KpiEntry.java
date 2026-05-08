package net.security.infosec.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiEntry {
    private int employeeId;
    private String employeeName;
    private LocalDate date;
    private int troubleId;
    private String troubleName;
    private String categoryName;
    private long totalMinutes;
}
