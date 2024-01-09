package net.security.infosec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Role;

@Data
@RequiredArgsConstructor
public class RoleDataTransferObject {
    @NotBlank(message = "Поле не может быть пустым")
    private String name;
    @NotBlank(message = "Поле не может быть пустым")
    private String description;
    private Role.Authority[] authorities = new Role.Authority[6];
}
