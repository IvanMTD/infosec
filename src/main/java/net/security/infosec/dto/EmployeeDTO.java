package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Employee;

@Data
@NoArgsConstructor
public class EmployeeDTO {
    private long id;

    private String name;
    private String middleName;
    private String lastname;
    private String position;
    private long departmentId;
    private long divisionId;
    private String cabinet;
    private String address;
    private String phone;
    private String personalPhone;
    private String email;
    private int number;

    public EmployeeDTO(Employee employee) {
        setId(employee.getId());
        setName(employee.getName());
        setMiddleName(employee.getMiddleName());
        setLastname(employee.getLastname());
        setPosition(employee.getPosition());
        setCabinet(employee.getCabinet());
        setAddress(employee.getAddress());
        setPhone(employee.getPhone());
        setPersonalPhone(employee.getPersonalPhone());
        setEmail(employee.getEmail());
        setDivisionId(employee.getDivisionId());
        setDepartmentId(employee.getDepartmentId());
        setNumber(employee.getNumber());
    }

    public String getFullName(){
        return lastname + " " + name + " " + middleName;
    }
}
