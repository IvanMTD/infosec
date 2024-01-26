package net.security.infosec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.security.infosec.models.Role;

import java.util.List;

@Data
@RequiredArgsConstructor
public class RoleDataTransferObject {
    private int id;
    @NotBlank(message = "Поле не может быть пустым")
    private String name;
    @NotBlank(message = "Поле не может быть пустым")
    private String description;
    private Role.Authority[] authorities = new Role.Authority[6];

    public RoleDataTransferObject(Role role){
        setId(role.getId());
        setName(role.getName());
        setDescription(role.getDescription());
        List<Role.Authority> authList = role.getAuthorities().stream().toList();
        for(int i=0; i<authList.size(); i++){
            authorities[i] = authList.get(i);
        }
    }
}
