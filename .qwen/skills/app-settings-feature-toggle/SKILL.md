---
name: app-settings-feature-toggle
description: Feature-флаг в БД (app_settings) с UI-тумблером на странице профиля
source: auto-skill
extracted_at: '2026-06-12T07:38:38.079Z'
---

# Feature-флаг на основе БД с UI-тумблером

Паттерн для включения/выключения функций через БД, с визуальным тумблером в интерфейсе (только для ADMIN).

## 1. V-миграция

```sql
CREATE TABLE IF NOT EXISTS app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL DEFAULT ''
);

INSERT INTO app_settings (key, value) VALUES ('ldap.sync.enabled', 'false')
ON CONFLICT (key) DO NOTHING;  -- значение по умолчанию, не перезаписывает существующее
```

## 2. Entity + Repo + Service

```java
@Data @NoArgsConstructor @Table("app_settings")
public class AppSettings {
    @Id private String key;
    private String value;
}
```

```java
public interface AppSettingsRepository extends ReactiveCrudRepository<AppSettings, String> {
    Mono<AppSettings> findByKey(String key);
}
```

```java
@Service @RequiredArgsConstructor
public class AppSettingsService {
    public static final String LDAP_SYNC_ENABLED = "ldap.sync.enabled";

    public Mono<Boolean> isLdapSyncEnabled() {
        return repository.findByKey(LDAP_SYNC_ENABLED)
            .map(s -> "true".equalsIgnoreCase(s.getValue()))
            .defaultIfEmpty(false);
    }

    public Mono<Void> setLdapSyncEnabled(boolean enabled) {
        return repository.save(new AppSettings(LDAP_SYNC_ENABLED, String.valueOf(enabled))).then();
    }
}
```

## 3. REST API

```java
@GetMapping("/admin/settings/ldap-sync")
@PreAuthorize("hasRole('ADMIN')")
public Mono<Boolean> getLdapSyncEnabled() {
    return appSettingsService.isLdapSyncEnabled();
}

@PutMapping("/admin/settings/ldap-sync")
@PreAuthorize("hasRole('ADMIN')")
public Mono<Void> setLdapSyncEnabled(@RequestBody Map<String, Boolean> body) {
    return appSettingsService.setLdapSyncEnabled(body.getOrDefault("enabled", false));
}
```

## 4. UI — тумблер на /profile (ADMIN)

```html
<div class="profile-card__footer" th:if="${admin}">
    <div class="form-check form-switch mb-2">
        <input class="form-check-input" type="checkbox" id="ldapSyncToggle" onchange="toggleLdapSync()">
        <label class="form-check-label" for="ldapSyncToggle" style="font-size:0.82rem;color:#aaa;">LDAP синхронизация</label>
    </div>
    <button class="btn btn-smoke w-100" id="syncBtn" style="display:none;" onclick="syncDirectory()">
        <i class="bi bi-arrow-repeat"></i> Синхронизировать с AD
    </button>
</div>
```

```javascript
// Инициализация тумблера
(function(){
    fetch('/api/admin/settings/ldap-sync')
    .then(function(r) { return r.json(); })
    .then(function(enabled) {
        document.getElementById('ldapSyncToggle').checked = enabled;
        document.getElementById('syncBtn').style.display = enabled ? '' : 'none';
    });
})();

function toggleLdapSync() {
    var enabled = document.getElementById('ldapSyncToggle').checked;
    document.getElementById('syncBtn').style.display = enabled ? '' : 'none';
    // PUT с CSRF
    var csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    var csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
    fetch('/api/admin/settings/ldap-sync', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
        body: JSON.stringify({ enabled: enabled })
    });
}
```

## 5. Проверка флага в сервисе/шедулере

```java
@Scheduled(cron = "0 0 * * * *")
public void scheduledSync() {
    Boolean enabled = appSettingsService.isLdapSyncEnabled().block();
    if (enabled == null || !enabled) {
        log.debug("LDAP синхронизация отключена — пропускаем");
        return;
    }
    // ... выполнение ...
}
```

`.block()` в шедулере безопасен — `@Scheduled` работает в отдельном пуле, не в реактивном event loop.

## Почему так

- **БД вместо properties** — можно менять на лету без перезапуска
- **Тумблер на UI** — удобно для администратора
- **По умолчанию выключен** — безопасно для первого деплоя без настроек
