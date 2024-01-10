package net.security.infosec.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TroubleDataTransferObject {
    private String name;
    private String description;
}
