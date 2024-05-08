package net.security.infosec.repositories;

import net.security.infosec.models.Department;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DepartmentRepository extends ReactiveCrudRepository<Department,Long> {
}
