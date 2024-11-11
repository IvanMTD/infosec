package net.security.infosec.repositories;

import net.security.infosec.models.Employee;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository extends R2dbcRepository<Employee,Long> {
    Flux<Employee> findByDivisionId(long divisionId);
    @Query("SELECT * FROM employee WHERE department_id != 0 AND division_id != 0")
    Flux<Employee> findByDepartmentNotZeroAndDivisionNotZero();

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
