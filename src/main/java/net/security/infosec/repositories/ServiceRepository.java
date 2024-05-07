package net.security.infosec.repositories;

import net.security.infosec.models.Service;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ServiceRepository extends ReactiveCrudRepository<Service, Long> {
}
