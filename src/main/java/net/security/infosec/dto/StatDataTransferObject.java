package net.security.infosec.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Category;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Task;
import net.security.infosec.models.Trouble;

@Data
@RequiredArgsConstructor
public class StatDataTransferObject {
    private Implementer implementer;
    private Category category;
    private Trouble trouble;
    private Task task;
}
