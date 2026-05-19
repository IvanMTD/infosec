package net.security.infosec.models.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("employee_job_system")
public class EmployeeJobSystem {
    private long employeeId;
    private UUID jobSystemUuid;
    private LocalDate connectDate;
    private LocalDate disconnectDate;
    private String status;
    private String mchd;
    private String roleInSystem;

    public EmployeeJobSystem(long employeeId, UUID jobSystemUuid) {
        this.employeeId = employeeId;
        this.jobSystemUuid = jobSystemUuid;
        this.connectDate = LocalDate.now();
        this.status = "ACTIVE";
    }
}
