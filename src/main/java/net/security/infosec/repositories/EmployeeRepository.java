package net.security.infosec.repositories;

import net.security.infosec.models.Employee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EmployeeRepository extends ReactiveCrudRepository<Employee,Long> {
}
