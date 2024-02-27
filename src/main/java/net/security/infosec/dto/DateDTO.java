package net.security.infosec.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DateDTO {
    private int userId;
    @NotNull(message = "Укажите дату с")
    private LocalDate begin;
    @NotNull(message = "Укажите дату по")
    private LocalDate end;
}
