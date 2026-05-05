package net.security.infosec.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.entity.Department;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DepartmentDTO {
    private long id;
    private String title;
    private String description;
    private List<DivisionDTO> divisions = new ArrayList<>();
    private List<EmployeeDTO> employees = new ArrayList<>();
    private int number;

    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.title = department.getTitle();
        this.description = department.getDescription();
        this.number = department.getNumber();
    }

    public void addDivision(DivisionDTO division){
        divisions.add(division);
    }

    public void addEmployee(EmployeeDTO employee){
        employees.add(employee);
    }
}
