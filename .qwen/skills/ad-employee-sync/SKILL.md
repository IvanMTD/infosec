---
name: ad-employee-sync
description: Полный цикл синхронизации справочника из Active Directory — сброс структуры, сохранение порядка, блокирующий код в WebFlux
source: auto-skill
extracted_at: '2026-06-11T20:21:09.448Z'
---

# AD → БД синхронизация справочника (Employee/Department/Division)

Полный цикл синхронизации сотрудников и структуры из Active Directory в локальную БД.

## 1. Архитектура

```
@Scheduled (раз в час) ──┐
                          ├── LdapSyncService.syncAll()
POST /api/admin/sync-directory ──┘        │
                                           ├── 1. Сохранить порядок (Map<title→number>)
                                           ├── 2. Открепить всех сотрудников
                                           ├── 3. Удалить все Department + Division
                                           ├── 4. Загрузить пользователей из AD
                                           ├── 5. Для каждого: найти/создать dept+div, создать/обновить employee
                                           ├── 6. Открепить тех, кого нет в AD
                                           └── 7. INACTIVE в employee_job_system для откреплённых
```

## 2. Endpoint (WebFlux + блокирующий код)

```java
@PostMapping("/admin/sync-directory")
@PreAuthorize("hasRole('ADMIN')")
public Mono<Map<String, Object>> syncDirectory() {
    return Mono.fromCallable(() -> ldapSyncService.syncAll())
        .subscribeOn(Schedulers.boundedElastic());
}
```

`.block()` внутри `syncAll()` разрешён только в `boundedElastic`-потоке.

## 3. Сохранение порядка перед сбросом

```java
Map<String, Integer> deptOrder = new HashMap<>();  // title(lower) → number
Map<String, Integer> divOrder = new HashMap<>();   // title(lower)|deptId → number

for (Department d : oldDepts) deptOrder.put(d.getTitle().toLowerCase(), d.getNumber());
for (Division d : oldDivs) divOrder.put(d.getTitle().toLowerCase() + "|" + d.getDepartmentId(), d.getNumber());
```

## 4. Восстановление порядка при создании

```java
Department d = new Department();
Integer oldNum = deptOrder.get(title.toLowerCase());
d.setNumber(oldNum != null ? oldNum : maxNum + 1);
```

## 5. INACTIVE в employee_job_system при откреплении

```java
databaseClient.sql("UPDATE employee_job_system SET status = 'INACTIVE', disconnect_date = :now " +
    "WHERE employee_id = :empId AND status = 'ACTIVE'")
    .bind("empId", emp.getId())
    .bind("now", LocalDate.now())
    .fetch().rowsUpdated().subscribe();
```

## 6. Ключ синхронизации — email (lowercase)

objectGUID ненадёжен в JNDI (возвращается строкой, теряются байты). Использовать `email` с `equalsIgnoreCase`.

## 7. CN → ФИО

```java
String[] parts = cn.trim().split("\\s+"); // "Карачков Иван Сергеевич"
lastname = normalize(parts[0]);  // Карачков
name = normalize(parts[1]);      // Иван
middleName = parts.length > 2 ? normalize(parts[2]) : "—";
```

## 8. UI — кнопка в профиле (ADMIN only)

```html
<div class="profile-card__footer" th:if="${admin}">
    <button class="btn btn-smoke w-100" onclick="syncDirectory()">
        <i class="bi bi-arrow-repeat"></i> Синхронизировать с AD
    </button>
</div>
```

CSRF-токен обязателен в fetch-запросе (читать из `<meta name='_csrf'>`).

## 9. Шедулер

```java
@EnableScheduling  // на @SpringBootApplication
@Scheduled(cron = "0 0 * * * *")  // раз в час
public void scheduledSync() { syncAll(); }
```

## Связанные навыки

- `ldap-dn-parsing-ou-hierarchy` — парсинг DN и маппинг OU
- `spring-ldap-integration` — LdapTemplate, фильтры, конфигурация
- `r2dbc-database-client-cud` — DatabaseClient для CUD
- `webflux-preauthorize-reactive-return` — Mono обёртка для @PreAuthorize
- `app-settings-feature-toggle` — тумблер включения/выключения фичи из БД
