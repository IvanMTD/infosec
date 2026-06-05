package net.security.infosec.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmployeeBindingDTO {
    private long employeeId;
    private String lastname;
    private String name;
    private String middleName;
    private String position;
    private String email;
    private String phone;
    private String personalPhone;
    private String status;
    private LocalDate connectDate;
    private LocalDate disconnectDate;
    private String mchd;
    private String roleInSystem;
    private String foundation;
    private String mchdBasis;
    private LocalDate mchdExpiry;
    private String accesses;
    private String departmentTitle;
    private String divisionTitle;
}
