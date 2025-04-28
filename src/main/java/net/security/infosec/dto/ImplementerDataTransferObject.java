package net.security.infosec.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.DepartmentRole;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Role;

@Data
@RequiredArgsConstructor
public class ImplementerDataTransferObject {
    private int id;
    @Email(message = "not valid email")
    @NotBlank(message = "not blank")
    private String email;
    @Size(min = 8, message = "Minimum 8 chapters")
    private String password;
    @Size(min = 8, message = "Minimum 8 chapters")
    private String confirmPassword;
    @NotBlank(message = "Поле не может быть пустым")
    @Size(min = 2, max = 10, message = "Имя может быть от 2 до 10 символов")
    @Pattern(regexp = "^[А-Я][а-я]+", message = "Имя должно быть в формате: 'Иван'")
    private String firstname;
    @Size(min = 2, max = 15, message = "Отчество может быть от 2 до 15 символов")
    @Pattern(regexp = "^[А-Я][а-я]+", message = "Отчество должно быть в формате: 'Иванович'")
    private String middleName;
    @NotBlank(message = "Поле не может быть пустым")
    @Size(min = 2, max = 20, message = "Фамилия может быть от 2 до 20 символов")
    @Pattern(regexp = "^[А-Я][а-я]+", message = "Фамилия должна быть в формате: 'Иванов'")
    private String lastname;
    @NotBlank(message = "Поле не может быть пустым")
    private String officePosition;
    private Role role;
    private DepartmentRole departmentRole;

    public ImplementerDataTransferObject(Implementer implementer) {
        setId(implementer.getId());
        setEmail(implementer.getEmail());
        setFirstname(implementer.getFirstname());
        setMiddleName(implementer.getMiddleName());
        setLastname(implementer.getLastname());
        setOfficePosition(implementer.getOfficePosition());
        setRole(implementer.getRole());
        setDepartmentRole(implementer.getDepartmentRole());
    }

    public String getFullName(){
        return lastname + " " + firstname + " " + middleName;
    }
}
