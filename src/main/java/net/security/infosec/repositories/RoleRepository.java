package net.security.infosec.repositories;

import net.security.infosec.models.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RoleRepository extends ReactiveCrudRepository<Role,Integer> {

}
