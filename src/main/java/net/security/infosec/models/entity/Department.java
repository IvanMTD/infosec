package net.security.infosec.models.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("department")
public class Department {
    @Id
    private long id;
    @NotBlank(message = "Не может быть пустым")
    private String title;
    @NotBlank(message = "Не может быть пустым")
    private String description;
    private int number;
}
