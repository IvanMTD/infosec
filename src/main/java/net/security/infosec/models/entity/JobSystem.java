package net.security.infosec.models.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.dto.JobSystemDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@Table("job_system")
public class JobSystem {
    @Id
    private UUID uuid;
    private String name;
    private String shortDescription;
    private String detailedDescription;
    private String url;
    private SystemType systemType;

    public JobSystem(JobSystemDTO dto) {
        update(dto);
    }

    public void update(JobSystemDTO dto) {
        setName(dto.getName());
        setShortDescription(dto.getShortDescription());
        setDetailedDescription(dto.getDetailedDescription());
        setUrl(dto.getUrl());
        setSystemType(dto.getSystemType() != null ?
            SystemType.valueOf(dto.getSystemType()) : SystemType.INTERNAL);
    }
}
