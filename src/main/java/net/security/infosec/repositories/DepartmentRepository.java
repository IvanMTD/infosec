package net.security.infosec.repositories;

import net.security.infosec.models.Department;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DepartmentRepository extends ReactiveCrudRepository<Department,Long> {
    @Query("SELECT * FROM department WHERE :divisionId = ANY(division_ids)")
    Mono<Department> findByDivisionId(long divisionId);
}
