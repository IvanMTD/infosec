---
name: spring-ldap-integration
description: Подключение Spring Boot к Active Directory через LdapTemplate — зависимости, конфигурация, поиск, SearchControls для бинарных атрибутов
source: auto-skill
extracted_at: '2026-06-11T12:55:34.865Z'
---

# Spring Boot LDAP / Active Directory Integration

Подключение к MS AD через `spring-boot-starter-data-ldap`. `LdapTemplate` используется напрямую (не через Spring Data LDAP-репозитории).

## 1. Зависимость

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-ldap</artifactId>
</dependency>
```

## 2. Конфигурация (application.properties)

```properties
spring.ldap.urls=ldap://${ldap.host}          # или ldaps://host:636
spring.ldap.base=${ldap.base}                  # OU=ФЦПСР,DC=internal,DC=fcpsr,DC=ru
spring.ldap.username=${ldap.bind.dn}           # CN=svc-user,OU=Service Accounts,DC=...
spring.ldap.password=${ldap.bind.pass}
spring.data.ldap.repositories.enabled=false    # отключить Spring Data LDAP-репозитории
```

**Важно:** `spring.data.ldap.repositories.enabled=false` — если в проекте есть R2DBC, иначе конфликт (см. skill `ldap-r2dbc-repository-conflict`).

## 3. Базовое использование LdapTemplate

```java
private final LdapTemplate ldapTemplate;  // Spring инжектит автоматически

@Value("${spring.ldap.base}")
private String baseDn;

// Простой поиск
List<Map<String, Object>> result = ldapTemplate.search(
    baseDn,                            // откуда ищем
    "(&(objectClass=user)(mail=*))",   // LDAP-фильтр
    (Attributes attrs) -> {            // AttributesMapper (лямбда)
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cn", safeGetAttr(attrs, "cn"));
        return map;
    }
);
```

## 4. LDAP-фильтр (основные операторы)

| Оператор | Описание | Пример |
|---|---|---|
| `&` | AND | `(&(a=1)(b=2))` |
| `\|` | OR | `(\|(a=1)(b=2))` |
| `!` | NOT | `(!(a=1))` |
| `*` | Wildcard | `cn=*иван*` |
| `:1.2.840...:=2` | Битовая маска | `userAccountControl:1.2.840.113556.1.4.803:=2` (DISABLED) |

## 5. SearchControls — получить бинарные атрибуты

По умолчанию LDAP не возвращает некоторые атрибуты (например `objectGUID`). Нужно явно запросить:

```java
SearchControls controls = new SearchControls();
controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
controls.setReturningAttributes(new String[]{"*", "objectGUID"});  // "*" = все стандартные
return ldapTemplate.search(baseDn, filter, controls, mapper);
```

## 6. Конвертация objectGUID → UUID-строка

`objectGUID` — 16-байтный бинарный GUID, не меняется никогда. AD хранит его в mixed-endian.
**Практический опыт:** в JNDI (через Spring LdapTemplate) `objectGUID` возвращается как `String` с бинарными данными, а не `byte[]`. Конвертация через `ISO_8859_1` теряет байты (замена на `?`). Суффикс `;binary` решает проблему нестабильно. **Вывод:** использовать `mail` как ключ синхронизации вместо objectGUID.

## 7. Продуктивный LDAP-фильтр (пользователи с телефоном и почтой)

```java
String filter = "(&(objectCategory=person)(mail=*)(telephoneNumber=*)"
    + "(|(&(objectClass=user)(givenName=*)(sn=*)"
    +   "(!(userAccountControl:1.2.840.113556.1.4.803:=2)))"
    +   "(objectClass=contact)))";
```

Ключевое: `mail=*` и `telephoneNumber=*` — обязательные поля (отсекаем служебные учётки). `!(userAccountControl:=2)` — исключаем отключенные.

## 8. Простой safeGetAttr helper

```java
private String safeGetAttr(Attributes attrs, String name) {
    try {
        Attribute attr = attrs.get(name);
        return attr != null ? String.valueOf(attr.get(0)) : "";
    } catch (Exception e) { return ""; }
}
```

## 9. Поиск с несколькими DC (отказоустойчивость)

```properties
spring.ldap.urls=ldap://dc1.domain.ru ldap://dc2.domain.ru
```

Spring LDAP автоматически пробует следующий сервер при ошибке подключения.

## 10. Чтение многозначных атрибутов (memberOf)

```java
Attribute attr = attrs.get("memberOf");
NamingEnumeration<?> en = attr.getAll();
while (en.hasMore()) list.add(String.valueOf(en.next()));
```
