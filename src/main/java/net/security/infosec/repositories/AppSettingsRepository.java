package net.security.infosec.repositories;

import net.security.infosec.models.entity.AppSettings;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AppSettingsRepository extends ReactiveCrudRepository<AppSettings, String> {
    Mono<AppSettings> findByKey(String key);
}
