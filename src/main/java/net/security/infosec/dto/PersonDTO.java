package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonDTO {
    private int number;
    private String lastname;
    private String name;
    private String middleName;
    private String location;
    private String office;
    private int shortNumber;
    private String position;
    private String email;
    private long department;
    private long division;

    public String getFullName(){
        return lastname + " " + name + " " + middleName;
    }
}
