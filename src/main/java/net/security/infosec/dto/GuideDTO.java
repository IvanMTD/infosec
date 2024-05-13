package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GuideDTO {
    private List<DepartmentDTO> departments = new ArrayList<>();

    public void addDepartment(DepartmentDTO department){
        if(departments == null){
            departments = new ArrayList<>();
        }
        departments.add(department);
    }
}
