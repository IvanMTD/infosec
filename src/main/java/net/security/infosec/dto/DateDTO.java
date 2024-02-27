package net.security.infosec.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DateDTO {
    private int userId;
    private LocalDate begin;
    private LocalDate end;
}
