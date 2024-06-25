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
    Flux<Employee> findAllByMiddleNameLikeIgnoreCaseAndDepartmentIdAndDivisionId(String middleName, long departmentId, long divisionId);

    Flux<Employee> findAllByLastnameLikeIgnoreCase(String lastname);
    Flux<Employee> findAllByNameLikeIgnoreCase(String name);
    Flux<Employee> findAllByMiddleNameLikeIgnoreCase(String middleName);
    Flux<Employee> findAllByEmailLikeIgnoreCase(String email);
    Flux<Employee> findAllByPositionLikeIgnoreCase(String position);
    Flux<Employee> findAllByPhoneLikeIgnoreCase(String phone);

    Mono<Employee> findByLastnameIgnoreCaseAndNameIgnoreCaseAndMiddleNameIgnoreCase(String lastname, String name, String middleName);
}
