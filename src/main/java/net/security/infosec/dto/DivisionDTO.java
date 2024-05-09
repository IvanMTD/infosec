package net.security.infosec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.security.infosec.models.Division;

@Data
@NoArgsConstructor
public class DivisionDTO {
    private long id;
    private String title;

    public DivisionDTO(Division division) {
        this.id = division.getId();
        this.title = division.getTitle();
    }
}
