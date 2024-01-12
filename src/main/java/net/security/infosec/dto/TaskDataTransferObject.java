package net.security.infosec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TaskDataTransferObject {
    @NotBlank(message = "not empty")
    private String title;
    @NotBlank(message = "not empty")
    private String description;
    private int troubleId;
    private int implementerId;
}
