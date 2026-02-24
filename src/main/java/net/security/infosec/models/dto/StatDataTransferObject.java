package net.security.infosec.models.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.entity.Category;
import net.security.infosec.models.entity.Implementer;
import net.security.infosec.models.entity.Task;
import net.security.infosec.models.entity.Trouble;

@Data
@RequiredArgsConstructor
public class StatDataTransferObject {
    private Implementer implementer;
    private Category category;
    private Trouble trouble;
    private Task task;
}
