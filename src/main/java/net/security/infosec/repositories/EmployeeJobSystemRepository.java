package net.security.infosec.repositories;

import net.security.infosec.models.entity.EmployeeJobSystem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface EmployeeJobSystemRepository extends ReactiveCrudRepository<EmployeeJobSystem, Void> {

    Flux<EmployeeJobSystem> findAllByJobSystemUuid(UUID jobSystemUuid);

    @Query("DELETE FROM employee_job_system WHERE employee_id = ?1 AND job_system_uuid = ?2")
    Mono<Void> deleteByEmployeeIdAndJobSystemUuid(long employeeId, UUID jobSystemUuid);
}
