package net.security.infosec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Category;
import net.security.infosec.models.DepartmentRole;
import net.security.infosec.models.Trouble;

@Data
@RequiredArgsConstructor
public class TicketDataTransferObject {

    private int id;

    @NotBlank(message = "not empty")
    private String name;
    @NotBlank(message = "not empty")
    private String description;
    private DepartmentRole departmentRole;
    private int categoryId;

    public TicketDataTransferObject(Category category) {
        setId(category.getId());
        setName(category.getName());
        setDescription(category.getDescription());
        setDepartmentRole(category.getDepartmentRole());
    }

    public TicketDataTransferObject(Trouble trouble) {
        setId(trouble.getId());
        setName(trouble.getName());
        setDescription(trouble.getDescription());
        setCategoryId(trouble.getCategoryId());
        setDepartmentRole(trouble.getDepartmentRole());
    }
}
