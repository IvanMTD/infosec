---
name: ldap-r2dbc-repository-conflict
description: Разрешение конфликта Spring Data LDAP + R2DBC — strict mode, @Table на все entity, отключение LDAP-репозиториев
source: auto-skill
extracted_at: '2026-06-11T09:13:46.333Z'
---

# LDAP + R2DBC Repository Conflict Resolution

При добавлении `spring-boot-starter-data-ldap` в проект с `spring-boot-starter-data-r2dbc`, Spring Data переходит в **strict repository configuration mode** и требует `@Table` на всех entity-классах, используемых репозиториями.

## Симптомы

```
No qualifying bean of type 'net.security.infosec.repositories.ImplementerRepository' available
```

Или в логах:
```
Found 2 R2DBC repository interfaces.
```
(вместо ожидаемых 10+)

## Решение — два шага

### 1. Отключить LDAP-репозитории

Если LDAP используется только через `LdapTemplate` напрямую (без Spring Data LDAP-репозиториев):

```properties
# application.properties
spring.data.ldap.repositories.enabled=false
```

### 2. Добавить @Table на ВСЕ entity

Каждая сущность, используемая R2DBC-репозиторием, должна иметь `@Table`:

```java
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("implementer")  // имя таблицы в БД
public class Implementer implements UserDetails {
    @Id
    private int id;
    // ...
}
```

Без `@Table` R2DBC не может идентифицировать репозиторий в strict mode и не создаёт бин.

## Порядок устранения

1. Добавить `spring.data.ldap.repositories.enabled=false`
2. Проверить логи: если `Found N R2DBC repository interfaces` где N меньше ожидаемого — добавить `@Table` на недостающие entity
3. Запустить, проверить что все репозитории найдены

## LDAP-конфигурация (для справки)

```properties
spring.ldap.urls=ldap://${ldap.host}
spring.ldap.base=${ldap.base}
spring.ldap.username=${ldap.bind.dn}
spring.ldap.password=${ldap.bind.pass}
```

Использование: `@Autowired LdapTemplate` → `ldapTemplate.search(base, filter, mapper)`.
