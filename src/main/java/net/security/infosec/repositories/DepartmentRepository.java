package net.security.infosec.repositories;

import net.security.infosec.models.entity.Department;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DepartmentRepository extends ReactiveCrudRepository<Department,Long> {
    @Query("SELECT d.* FROM department d JOIN division dv ON dv.department_id = d.id WHERE dv.id = :divisionId")
    Mono<Department> findByDivisionId(long divisionId);
}
