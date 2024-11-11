package net.security.infosec.models;

/*
create table if not exists employee(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    name text,
    middle_name text,
    lastname text,
    position text,
    department_id bigint,
    division_id bigint,
    cabinet int,
    address text,
    phone int
);
 */

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.dto.EmployeeDTO;
import net.security.infosec.dto.PersonDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("employee")
public class Employee {
    @Id
    private long id;

    @NotBlank(message = "Не может быть пустым")
    private String name;
    @NotBlank(message = "Не может быть пустым")
    private String middleName;
    @NotBlank(message = "Не может быть пустым")
    private String lastname;

    private String position;
    private long departmentId;
    private long divisionId;

    @NotBlank(message = "Не может быть пустым")
    private String cabinet;
    @NotBlank(message = "Не может быть пустым")
    private String address;

    /*@Min(value = 100, message = "Короткий номер не может быть меньше 3 цифр")
    @Max(value = 9999, message = "Короткий номер не может быть больше 4 цифр")*/
    private String phone;
    private String personalPhone;
    @Email(message = "Не валидный email")
    @NotBlank(message = "Не может быть пустым")
    private String email;
    private int number;

    public Employee(Employee employee) {
        setId(employee.getId());
        setName(employee.getName());
        setMiddleName(employee.getMiddleName());
        setLastname(employee.getLastname());
        setPosition(employee.getPosition());
        setDepartmentId(employee.getDepartmentId());
        setDivisionId(employee.getDivisionId());
        setCabinet(employee.getCabinet());
        setAddress(employee.getAddress());
        setPhone(employee.getPhone());
        setPersonalPhone(employee.getPersonalPhone());
        setEmail(employee.getEmail());
        setNumber(employee.getNumber());
    }

    public Employee(PersonDTO person) {
        setId(person.getId());
        setNumber(person.getNumber());
        setLastname(person.getLastname());
        setName(person.getName());
        setMiddleName(person.getMiddleName());
        setAddress(person.getLocation());
        setCabinet(person.getOffice());
        setPhone(person.getShortNumber());
        setPersonalPhone(person.getPersonalPhone());
        setPosition(person.getPosition());
        setEmail(person.getEmail());

        if(person.getDivision() != 0){
            setDivisionId(person.getDivision());
        }else{
            setDepartmentId(person.getDepartment());
        }
    }

    public String getFullName(){
        return lastname + " " + name + " " + middleName;
    }

    public void update(Employee employee) {
        setName(employee.getName());
        setMiddleName(employee.getMiddleName());
        setLastname(employee.getLastname());
        setPosition(employee.getPosition());
        setCabinet(employee.getCabinet());
        setAddress(employee.getAddress());
        setPhone(employee.getPhone());
        setPersonalPhone(employee.getPersonalPhone());
        setEmail(employee.getEmail());
        setNumber(employee.getNumber());
        if(employee.getDivisionId() != 0){
            setDepartmentId(0);
            setDivisionId(employee.getDivisionId());
        }else{
            setDepartmentId(employee.getDepartmentId());
            setDivisionId(0);
        }
    }
}
