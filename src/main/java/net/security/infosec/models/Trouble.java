package net.security.infosec.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.TicketDataTransferObject;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor
public class Trouble {
    @Id
    private int id;
    /**
     * Описание модели
     */
    private String name;
    private String description;
    private DepartmentRole departmentRole;
    /**
     * Связные модели
     */
    private int categoryId;

    public Trouble(TicketDataTransferObject dto) {
        setName(dto.getName());
        setDescription(dto.getDescription());
        setCategoryId(dto.getCategoryId());
        setDepartmentRole(dto.getDepartmentRole());
    }

    public Trouble update(TicketDataTransferObject ticketDTO) {
        setName(ticketDTO.getName());
        setDescription(ticketDTO.getDescription());
        setCategoryId(ticketDTO.getCategoryId());
        setDepartmentRole(ticketDTO.getDepartmentRole());
        return this;
    }
}
