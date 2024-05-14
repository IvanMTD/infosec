package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Division;
import net.security.infosec.models.Employee;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DivisionDTO {
    private long id;
    private String title;
    private String description;
    private DepartmentDTO department;
    private List<EmployeeDTO> employees = new ArrayList<>();
    private int number;

    public DivisionDTO(Division division) {
        this.id = division.getId();
        this.title = division.getTitle();
        this.description = division.getDescription();
        this.number = division.getNumber();
    }

    public void addEmployee(EmployeeDTO employee){
        employees.add(employee);
    }
}
