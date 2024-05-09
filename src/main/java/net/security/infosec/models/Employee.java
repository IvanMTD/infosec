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
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
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

    @Min(value = 100, message = "Короткий номер не может быть меньше 3 цифр")
    @Max(value = 9999, message = "Короткий номер не может быть больше 4 цифр")
    private int phone;
    @Email(message = "Не валидный email")
    @NotBlank(message = "Не может быть пустым")
    private String email;

    public String getFullName(){
        return lastname + " " + name + " " + middleName;
    }
}