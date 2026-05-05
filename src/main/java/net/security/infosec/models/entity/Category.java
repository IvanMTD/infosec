package net.security.infosec.models.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.dto.TicketDataTransferObject;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor
public class Category {
    @Id
    private int id;

    private String name;
    private String description;
    private DepartmentRole departmentRole;

    public Category(TicketDataTransferObject dto) {
        setName(dto.getName());
        setDescription(dto.getDescription());
        setDepartmentRole(dto.getDepartmentRole());
    }

    public Category update(TicketDataTransferObject ticketDTO) {
        setName(ticketDTO.getName());
        setDescription(ticketDTO.getDescription());
        setDepartmentRole(ticketDTO.getDepartmentRole());
        return this;
    }
}
