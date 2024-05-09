package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Department;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DepartmentDTO {
    private long id;
    private String title;
    private List<DivisionDTO> divisions = new ArrayList<>();

    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.title = department.getTitle();
    }
}
