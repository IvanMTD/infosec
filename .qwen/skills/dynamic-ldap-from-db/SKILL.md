---
name: dynamic-ldap-from-db
description: Динамический LdapTemplate из БД + шифрование пароля + отключение авто-LDAP для безопасного старта без AD
source: auto-skill
extracted_at: '2026-06-12T09:31:00.000Z'
---

# Динамический LdapTemplate из БД с шифрованием пароля

Паттерн для проектов где LDAP может быть недоступен при старте. Настройки хранятся в БД, `LdapTemplate` создаётся на лету, пароль шифруется AES.

## Проблема

Стандартная `spring-boot-starter-data-ldap` создаёт `LdapTemplate` при старте. Если AD недоступен — приложение падает. Решение: отключить авто-конфигурацию, хранить настройки в БД, создавать шаблон вручную.

## 1. Отключение авто-LDAP

```properties
# application.properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration
```

## 2. Шифрование пароля

```java
@Component
public class LdapPasswordCrypto {
    private final TextEncryptor encryptor;

    public LdapPasswordCrypto(@Value("${jwt.secret:default}") String secret) {
        this.encryptor = Encryptors.text(secret, "deadbeef");  // соль 8+ байт
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isBlank()) return "";
        return encryptor.encrypt(plain);
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) return "";
        try {
            return encryptor.decrypt(encrypted);
        } catch (Exception e) {
            return encrypted;  // обратная совместимость со старыми открытыми паролями
        }
    }
}
```

**Ключ:** `jwt.secret` уже есть в проекте, переиспользуем. Соль `"deadbeef"` — 8 байт (минимум для `Encryptors.text`).

## 3. DynamicLdapTemplate

```java
@Component
@RequiredArgsConstructor
public class DynamicLdapTemplate {
    private final AppSettingsService appSettingsService;
    private final AppSettingsRepository appSettingsRepository;
    private final LdapPasswordCrypto crypto;

    public LdapTemplate createTemplate() {
        Map<String, String> config = appSettingsService.getLdapConfig().block();
        if (config == null) return null;

        if (!"true".equalsIgnoreCase(config.getOrDefault("ldap.sync.enabled", "false")))
            return null;

        String url = config.getOrDefault("ldap.url", "");
        String base = config.getOrDefault("ldap.base", "");
        String bindDn = config.getOrDefault("ldap.bind.dn", "");

        // Пароль читаем напрямую из БД — getLdapConfig маскирует его на ***
        String encryptedPass = appSettingsRepository.findByKey("ldap.bind.pass")
            .map(s -> s.getValue()).defaultIfEmpty("").block();
        String bindPass = crypto.decrypt(encryptedPass != null ? encryptedPass : "");

        if (url.isBlank() || base.isBlank() || bindDn.isBlank() || bindPass.isBlank())
            return null;

        LdapContextSource ctx = new LdapContextSource();
        ctx.setUrl(url);
        ctx.setBase(base);
        ctx.setUserDn(bindDn);
        ctx.setPassword(bindPass);
        ctx.afterPropertiesSet();
        return new LdapTemplate(ctx);
    }

    public String testConnection() {
        LdapTemplate t = createTemplate();
        if (t == null) return "Не удалось создать подключение — проверьте настройки";
        try {
            t.search("", "(objectClass=*)", (AttributesMapper<Object>) attrs -> null);
            return null; // OK
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
```

**Ключевой момент:** `search("", filter)` — пустая строка означает «относительно base DN контекста». Не передавать абсолютный base DN — будет двойной путь.

## 4. Сохранение с шифрованием

```java
public Mono<Void> saveLdapConfig(Map<String, String> config) {
    return Flux.fromIterable(config.entrySet())
        .flatMap(e -> {
            String value = e.getValue() != null ? e.getValue() : "";
            if ("ldap.bind.pass".equals(e.getKey()) && !value.isBlank())
                value = crypto.encrypt(value);
            return databaseClient.sql(
                "INSERT INTO app_settings (key, value) VALUES (:key, :value) " +
                "ON CONFLICT (key) DO UPDATE SET value = :value")
                .bind("key", e.getKey()).bind("value", value)
                .fetch().rowsUpdated();
        }).then();
}
```

**Используем `DatabaseClient` с UPSERT** — `repository.save()` в R2DBC делает UPDATE для существующих ключей и падает если запись не найдена.

## 5. Использование в сервисе синхронизации

```java
// Было:
// private final LdapTemplate ldapTemplate;  // @Autowired — падает без AD

// Стало:
private final DynamicLdapTemplate dynamicLdap;

private LdapTemplate getLdap() {
    LdapTemplate t = dynamicLdap.createTemplate();
    if (t == null) throw new IllegalStateException("LDAP не настроен или отключен");
    return t;
}

// Далее везде: getLdap().search("", filter, mapper)
```

## 6. UI — проверка подключения при включении

При включении тумблера: сохраняем настройки → тестируем соединение → если ошибка — показываем красным, тумблер откатывается.

```javascript
fetch('/api/admin/settings/ldap-config', { method: 'PUT', ... })
.then(() => fetch('/api/admin/settings/ldap-test', { method: 'POST', ... }))
.then(r => r.json())
.then(res => {
    if (res.ok) { /* включено */ }
    else { /* ошибка: res.error */ }
});
```

## Почему так

- **Приложение не падает без AD** — авто-конфигурация отключена
- **Пароль не хранится открытым** — AES через `Encryptors.text`
- **Настройки в БД** — можно менять через UI без перезапуска
- **Тест соединения перед включением** — пользователь сразу видит ошибку
