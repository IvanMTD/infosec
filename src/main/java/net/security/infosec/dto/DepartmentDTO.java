package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Department;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class DepartmentDTO {
    private long id;
    private String title;
    private String description;
    private List<DivisionDTO> divisions = new ArrayList<>();
    private List<EmployeeDTO> employees = new ArrayList<>();

    private Set<Long> divisionIds = new HashSet<>();
    private int number;

    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.title = department.getTitle();
        this.description = department.getDescription();
        this.divisionIds = department.getDivisionIds();
        this.number = department.getNumber();
    }

    public void addDivision(DivisionDTO division){
        divisions.add(division);
    }

    public void addEmployee(EmployeeDTO employee){
        employees.add(employee);
    }
}
