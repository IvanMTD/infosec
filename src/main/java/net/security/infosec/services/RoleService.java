package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.RoleDataTransferObject;
import net.security.infosec.models.Role;
import net.security.infosec.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Mono<Role> saveRole(RoleDataTransferObject roleDTO){
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        for(Role.Authority authority : roleDTO.getAuthorities()){
            if(authority != null){
                role.addAuthority(authority);
            }
        }
        return roleRepository.save(role);
    }

    public Flux<Role> getAll() {
        return roleRepository.findAll();
    }

    public Mono<Role> getRoleById(int id) {
        return roleRepository.findById(id);
    }
}
