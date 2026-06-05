package net.security.infosec.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.entity.EmployeeJobSystem;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmployeeJobSystemDTO {
    private long employeeId;
    private String jobSystemUuid;
    private LocalDate connectDate;
    private LocalDate disconnectDate;
    private String status;
    private String mchd;
    private String roleInSystem;
    private String foundation;
    private String mchdBasis;
    private LocalDate mchdExpiry;
    private String accesses;

    public EmployeeJobSystemDTO(EmployeeJobSystem entity) {
        setEmployeeId(entity.getEmployeeId());
        setJobSystemUuid(entity.getJobSystemUuid() != null ? entity.getJobSystemUuid().toString() : null);
        setConnectDate(entity.getConnectDate());
        setDisconnectDate(entity.getDisconnectDate());
        setStatus(entity.getStatus());
        setMchd(entity.getMchd());
        setRoleInSystem(entity.getRoleInSystem());
        setFoundation(entity.getFoundation());
        setMchdBasis(entity.getMchdBasis());
        setMchdExpiry(entity.getMchdExpiry());
        setAccesses(entity.getAccesses());
    }
}
