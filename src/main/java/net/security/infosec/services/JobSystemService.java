package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.dto.EmployeeDTO;
import net.security.infosec.models.dto.EmployeeJobSystemDTO;
import net.security.infosec.models.dto.JobSystemDTO;
import net.security.infosec.models.entity.EmployeeJobSystem;
import net.security.infosec.models.entity.JobSystem;
import net.security.infosec.repositories.EmployeeJobSystemRepository;
import net.security.infosec.repositories.EmployeeRepository;
import net.security.infosec.repositories.JobSystemRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSystemService {

    private final JobSystemRepository repository;
    private final EmployeeJobSystemRepository empSysRepo;
    private final EmployeeRepository employeeRepository;
    private final DatabaseClient databaseClient;

    public Mono<Map<String, Object>> getSystems(String search, String type, int page, int size) {
        String lowerSearch = (search == null || search.isBlank()) ? null : search.toLowerCase();
        boolean filterByType = type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type);

        return repository.findAllByOrderByName()
            .filter(sys -> {
                if (lowerSearch == null) return true;
                return matches(sys, lowerSearch);
            })
            .filter(sys -> {
                if (!filterByType) return true;
                return sys.getSystemType() != null && sys.getSystemType().name().equalsIgnoreCase(type);
            })
            .map(JobSystemDTO::new)
            .collectList()
            .map(list -> {
                long total = list.size();
                long totalPages = total == 0 ? 0 : (total + size - 1) / size;
                int from = page * size;
                int to = Math.min(from + size, (int) total);
                List<JobSystemDTO> paged = from < total ? list.subList(from, to) : List.of();
                return Map.<String, Object>of(
                    "content", paged,
                    "totalCount", total,
                    "totalPages", totalPages,
                    "page", page
                );
            });
    }

    private boolean matches(JobSystem sys, String lowerSearch) {
        if (sys.getName() != null && sys.getName().toLowerCase().contains(lowerSearch)) return true;
        if (sys.getShortDescription() != null && sys.getShortDescription().toLowerCase().contains(lowerSearch)) return true;
        if (sys.getDetailedDescription() != null && sys.getDetailedDescription().toLowerCase().contains(lowerSearch)) return true;
        if (sys.getUrl() != null && sys.getUrl().toLowerCase().contains(lowerSearch)) return true;
        return false;
    }

    public Mono<JobSystemDTO> getById(UUID uuid) {
        return repository.findById(uuid).map(JobSystemDTO::new);
    }

    public Mono<JobSystemDTO> create(JobSystemDTO dto) {
        JobSystem entity = new JobSystem(dto);
        return repository.save(entity).map(JobSystemDTO::new);
    }

    public Mono<JobSystemDTO> update(UUID uuid, JobSystemDTO dto) {
        return repository.findById(uuid)
            .flatMap(entity -> {
                entity.update(dto);
                return repository.save(entity);
            })
            .map(JobSystemDTO::new);
    }

    public Mono<String> delete(UUID uuid) {
        return repository.deleteById(uuid).thenReturn("OK");
    }

    // ======== Employee binding ========

    public Flux<EmployeeDTO> getEmployees(UUID systemUuid) {
        return empSysRepo.findAllByJobSystemUuid(systemUuid)
            .flatMap(es -> employeeRepository.findById(es.getEmployeeId()))
            .map(EmployeeDTO::new);
    }

    public Mono<EmployeeDTO> addEmployee(UUID systemUuid, long employeeId) {
        return empSysRepo.save(new EmployeeJobSystem(employeeId, systemUuid))
            .then(employeeRepository.findById(employeeId))
            .map(EmployeeDTO::new)
            .onErrorResume(e -> employeeRepository.findById(employeeId).map(EmployeeDTO::new));
    }

    public Mono<Void> removeEmployee(UUID systemUuid, long employeeId) {
        return empSysRepo.deleteByEmployeeIdAndJobSystemUuid(employeeId, systemUuid);
    }

    public Flux<EmployeeDTO> searchEmployees(String query) {
        String q = (query == null || query.isBlank()) ? "" : query.toLowerCase();
        return employeeRepository.findAll()
            .filter(emp -> matchesEmployee(emp, q))
            .map(EmployeeDTO::new)
            .take(20);
    }

    private boolean matchesEmployee(net.security.infosec.models.entity.Employee emp, String lowerQuery) {
        if (lowerQuery.isEmpty()) return false;
        String fullName = (emp.getLastname() + " " + emp.getName() + " " + emp.getMiddleName()).toLowerCase();
        return fullName.contains(lowerQuery);
    }

    public Mono<EmployeeJobSystemDTO> getBinding(UUID systemUuid, long employeeId) {
        return empSysRepo.findAllByJobSystemUuid(systemUuid)
            .filter(es -> es.getEmployeeId() == employeeId)
            .next()
            .map(EmployeeJobSystemDTO::new);
    }

    public Mono<EmployeeJobSystemDTO> updateBinding(UUID systemUuid, long employeeId, EmployeeJobSystemDTO dto) {
        var spec = databaseClient.sql("""
                UPDATE employee_job_system
                SET connect_date = :connectDate, disconnect_date = :disconnectDate,
                    status = :status, mchd = :mchd, role_in_system = :roleInSystem
                WHERE employee_id = :employeeId AND job_system_uuid = :systemUuid
                """)
            .bind("connectDate", dto.getConnectDate())
            .bind("status", dto.getStatus())
            .bind("employeeId", employeeId)
            .bind("systemUuid", systemUuid);
        spec = dto.getDisconnectDate() != null ? spec.bind("disconnectDate", dto.getDisconnectDate()) : spec.bindNull("disconnectDate", java.sql.Date.class);
        spec = dto.getMchd() != null ? spec.bind("mchd", dto.getMchd()) : spec.bindNull("mchd", String.class);
        spec = dto.getRoleInSystem() != null ? spec.bind("roleInSystem", dto.getRoleInSystem()) : spec.bindNull("roleInSystem", String.class);
        return spec.fetch().rowsUpdated()
            .then(empSysRepo.findAllByJobSystemUuid(systemUuid)
                .filter(es -> es.getEmployeeId() == employeeId)
                .next()
                .map(EmployeeJobSystemDTO::new));
    }
}
