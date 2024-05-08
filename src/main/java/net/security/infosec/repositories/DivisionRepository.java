package net.security.infosec.repositories;

import net.security.infosec.models.Division;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DivisionRepository extends ReactiveCrudRepository<Division,Long> {
}
