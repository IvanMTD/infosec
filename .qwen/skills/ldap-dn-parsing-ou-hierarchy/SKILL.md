---
name: ldap-dn-parsing-ou-hierarchy
description: Парсинг Distinguished Name из Active Directory, извлечение OU-цепочки, правила маппинга на Department/Division и блокирующие вызовы в WebFlux
source: auto-skill
extracted_at: '2026-06-11T20:00:00.000Z'
---

# LDAP DN Parsing & OU → Department/Division Mapping

## 1. Парсинг Distinguished Name

DN: `CN=Карачков Иван,OU=Служба ИБ,OU=Управление ИТ,OU=ФЦПСР,DC=internal,DC=fcpsr,DC=ru`

```java
private String[] parseDN(String dn) {
    // Разбиваем на RDN по запятым с учётом экранирования (\, = экранированная запятая)
    List<String> rdns = splitRDN(dn);
    List<String> ous = new ArrayList<>();
    for (String rdn : rdns) {
        rdn = rdn.trim()
            .replace("\\,", ",")   // отменяем экранирование: \, → ,
            .replace("\\=", "=")   // \= → =
            .replace("\\\\", "\\"); // \\ → \
        if (rdn.startsWith("OU=") && !"ФЦПСР".equalsIgnoreCase(rdn.substring(3))) {
            ous.add(rdn.substring(3));
        }
    }
    // НЕ переворачиваем! Порядок: от ближнего к пользователю CN к дальнему DC
    return ous.toArray(new String[0]);
}

private List<String> splitRDN(String dn) {
    List<String> parts = new ArrayList<>();
    int start = 0;
    for (int i = 0; i < dn.length(); i++) {
        // Запятая без предшествующего \ = разделитель RDN
        if (dn.charAt(i) == ',' && (i == 0 || dn.charAt(i - 1) != '\\')) {
            parts.add(dn.substring(start, i));
            start = i + 1;
        }
    }
    if (start < dn.length()) parts.add(dn.substring(start));
    return parts;
}
```

### Порядок OU в массиве

`["Служба ИБ", "Управление ИТ"]` — ous[0] ближе к пользователю, ous[last] ближе к корню.

## 2. Правила маппинга OU → Департамент/Дивижин

```java
// Всегда берём ПЕРВЫЕ два OU, остальные откидываем
String divName = ous.length >= 2 ? normalize(ous[0]) : null;     // дивижин
String deptName = ous.length >= 2 ? normalize(ous[1]) : normalize(ous[0]); // департамент

// Если ous.length == 1 → это департамент (дивижина нет)
// Если ous.length >= 2 → ous[0]=дивижин, ous[1]=департамент
// Если ous.length == 0 → только ФЦПСР, отвязать (deptId=0, divId=0)
```

Примеры:
- `["Служба ИБ", "Управление ИТ"]` → div=Служба ИБ, dept=Управление ИТ
- `["Отдел Х", "Управление Y", "Центр Z"]` → div=Отдел Х, dept=Управление Y (Центр Z откинут)
- `["Управление ИТ"]` → dept=Управление ИТ, div=null

## 3. Нормализация названий

```java
private String normalize(String s) {
    // Только первая буква заглавная, остальные строчные
    String trimmed = s.trim();
    return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1).toLowerCase();
}
```

Не делать каждое слово с заглавной — «Управление организации и проведения» а не «Управление Организации И Проведения».

## 4. Ключ поиска — email (lowercase)

```java
String mail = ad.get("mail").toLowerCase();
Employee emp = findByEmail(dbEmployees, mail); // equalsIgnoreCase
```

## 5. `.block()` в WebFlux-потоке

При использовании `.block()` внутри контроллера WebFlux:

```
java.lang.IllegalStateException: block()/blockFirst()/blockLast() are blocking,
which is not supported in thread reactor-tcp-nio-N
```

**Решение:** обернуть в `Mono.fromCallable().subscribeOn(Schedulers.boundedElastic())`:

```java
@PostMapping("/admin/sync-directory")
public Mono<Map<String, Object>> syncDirectory() {
    return Mono.fromCallable(() -> service.blockingMethod())
        .subscribeOn(Schedulers.boundedElastic());
}
```

Это переносит блокирующий код в отдельный пул, не блокируя event loop.
