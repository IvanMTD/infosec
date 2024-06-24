package net.security.infosec.repositories;

import net.security.infosec.models.Employee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository extends ReactiveCrudRepository<Employee,Long> {
    Flux<Employee> findByDivisionId(long divisionId);

    Flux<Employee> findByDepartmentId(long id);

    Flux<Employee> findAllByLastnameLikeIgnoreCaseAndDepartmentIdAndDivisionId(String lastname, long departmentId, long divisionId);
    Flux<Employee> findAllByNameLikeIgnoreCaseAndDepartmentIdAndDivisionId(String name, long departmentId, long divisionId);
    Flux<Employee> findAllByMiddleNameIgnoreCaseAndDepartmentIdAndDivisionId(String middleName, long departmentId, long divisionId);

    Mono<Employee> findByLastnameIgnoreCaseAndNameIgnoreCaseAndMiddleNameIgnoreCase(String lastname, String name, String middleName);
}
