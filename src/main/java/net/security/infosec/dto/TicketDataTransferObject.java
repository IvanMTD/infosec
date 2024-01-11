package net.security.infosec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TicketDataTransferObject {
    @NotBlank(message = "not empty")
    private String name;
    @NotBlank(message = "not empty")
    private String description;
    private int categoryId;
}
