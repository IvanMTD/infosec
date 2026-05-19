package net.security.infosec.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SystemReportDTO {
    private JobSystemDTO system;
    private List<EmployeeBindingDTO> employees;
}
