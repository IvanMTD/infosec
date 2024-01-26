package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import net.security.infosec.dto.RoleDataTransferObject;
import net.security.infosec.models.Implementer;
import net.security.infosec.models.Role;
import net.security.infosec.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

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

    public Mono<Role> updateRole(RoleDataTransferObject role, int id) {
        return roleRepository.findById(id).flatMap(r -> {
            r.setName(role.getName());
            r.setDescription(role.getDescription());
            r.setAuthorities(new HashSet<>());
            for(Role.Authority authority : role.getAuthorities()){
                if(authority != null){
                    r.addAuthority(authority);
                }
            }
            return roleRepository.save(r);
        });
    }

    public Flux<Role> getAll() {
        return roleRepository.findAll();
    }

    public Mono<Role> getRoleById(int id) {
        return roleRepository.findById(id);
    }

    public Mono<RoleDataTransferObject> getRoleDTOById(int id) {
        return roleRepository.findById(id).flatMap(role -> Mono.just(new RoleDataTransferObject(role)));
    }

    public Mono<Set<Role.Authority>> getRoleAuthoritiesById(int id) {
        return roleRepository.findById(id).flatMap(role -> Mono.just(role.getAuthorities()));
    }

    public Mono<Role> addImplementerIdToRole(Implementer impl) {
        return roleRepository.findById(impl.getRoleId()).flatMap(role -> {
            role.addImplementer(impl);
            return roleRepository.save(role);
        });
    }
}
