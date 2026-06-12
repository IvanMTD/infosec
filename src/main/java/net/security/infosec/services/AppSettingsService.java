package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.entity.AppSettings;
import net.security.infosec.repositories.AppSettingsRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppSettingsService {

    public static final String LDAP_SYNC_ENABLED = "ldap.sync.enabled";
    public static final String LDAP_URL = "ldap.url";
    public static final String LDAP_BASE = "ldap.base";
    public static final String LDAP_BIND_DN = "ldap.bind.dn";
    public static final String LDAP_BIND_PASS = "ldap.bind.pass";

    private final AppSettingsRepository repository;
    private final DatabaseClient databaseClient;
    private final LdapPasswordCrypto crypto;

    public Mono<Boolean> isLdapSyncEnabled() {
        return repository.findByKey(LDAP_SYNC_ENABLED)
            .map(s -> "true".equalsIgnoreCase(s.getValue()))
            .defaultIfEmpty(false);
    }

    public Mono<Void> setLdapSyncEnabled(boolean enabled) {
        return databaseClient.sql("INSERT INTO app_settings (key, value) VALUES (:key, :value) ON CONFLICT (key) DO UPDATE SET value = :value")
            .bind("key", LDAP_SYNC_ENABLED)
            .bind("value", String.valueOf(enabled))
            .fetch().rowsUpdated().then();
    }

    public Mono<Map<String, String>> getLdapConfig() {
        Map<String, String> config = new LinkedHashMap<>();
        return repository.findAll()
            .collectList()
            .map(list -> {
                for (AppSettings s : list) {
                    if (s.getKey().startsWith("ldap.")) {
                        config.put(s.getKey(), s.getValue());
                    }
                }
                config.putIfAbsent("ldap.url", "");
                config.putIfAbsent("ldap.base", "");
                config.putIfAbsent("ldap.bind.dn", "");
                config.putIfAbsent("ldap.bind.pass", "");
                // Пароль не возвращаем — вместо него признак наличия
                if (!config.getOrDefault("ldap.bind.pass", "").isBlank()) {
                    config.put("ldap.bind.pass", "***");
                }
                config.putIfAbsent("ldap.sync.enabled", "false");
                return config;
            });
    }

    public Mono<Void> saveLdapConfig(Map<String, String> config) {
        return Flux.fromIterable(config.entrySet())
            .flatMap(e -> {
                String value = e.getValue() != null ? e.getValue() : "";
                // Шифруем пароль перед сохранением
                if (LDAP_BIND_PASS.equals(e.getKey()) && !value.isBlank()) {
                    value = crypto.encrypt(value);
                }
                return databaseClient.sql("INSERT INTO app_settings (key, value) VALUES (:key, :value) ON CONFLICT (key) DO UPDATE SET value = :value")
                    .bind("key", e.getKey())
                    .bind("value", value)
                    .fetch().rowsUpdated();
            })
            .then();
    }
}
