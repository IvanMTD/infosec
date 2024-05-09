package net.security.infosec.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

/*
create table if not exists department(
    id bigint GENERATED BY DEFAULT AS IDENTITY,
    department_head bigint,
    division_ids bigint[],
    title text,
    description text
);
 */

@Data
@NoArgsConstructor
public class Department {
    @Id
    private long id;
    private Set<Long> divisionIds = new HashSet<>();
    @NotBlank(message = "Не может быть пустым")
    private String title;
    @NotBlank(message = "Не может быть пустым")
    private String description;
}