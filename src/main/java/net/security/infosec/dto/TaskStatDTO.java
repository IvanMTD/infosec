package net.security.infosec.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TaskStatDTO {
    private String categoryName;
    private int troubleCount;
}
