package net.security.infosec.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.entity.JobSystem;

@Data
@NoArgsConstructor
public class JobSystemDTO {
    private String uuid;
    private String name;
    private String shortDescription;
    private String detailedDescription;
    private String url;
    private String systemType; // "INTERNAL" или "EXTERNAL"

    public JobSystemDTO(JobSystem entity) {
        setUuid(entity.getUuid() != null ? entity.getUuid().toString() : null);
        setName(entity.getName());
        setShortDescription(entity.getShortDescription());
        setDetailedDescription(entity.getDetailedDescription());
        setUrl(entity.getUrl());
        setSystemType(entity.getSystemType() != null ? entity.getSystemType().name() : "INTERNAL");
    }
}
