---
name: webflux-preauthorize-reactive-return
description: В WebFlux @PreAuthorize требует Mono/Flux возврат — как обернуть и когда можно без
source: auto-skill
extracted_at: '2026-06-11T12:55:34.865Z'
---

# @PreAuthorize in WebFlux requires reactive return type

При использовании `@PreAuthorize` в WebFlux-контроллере Spring Security создаёт реактивный прокси, который требует возврат `org.reactivestreams.Publisher` (Mono/Flux).

## Симптом

```
java.lang.IllegalStateException: The returnType ... must return an instance of
org.reactivestreams.Publisher (for example, a Mono or Flux) in order to support
Reactor Context
```

## Причина

Без `@PreAuthorize` Spring WebFlux автоматически оборачивает синхронный возврат (`List`, `String`) в `Mono`. Но при добавлении `@PreAuthorize` включается Security-прокси, который требует явный реактивный тип.

## Решение

Обернуть возврат в `Mono.just()` и изменить тип:

```java
// Было — работало без @PreAuthorize:
public List<Map<String, Object>> getUsers() {
    return someService.getData();
}

// Стало — для работы с @PreAuthorize:
@PreAuthorize("hasRole('ADMIN')")
public Mono<List<Map<String, Object>>> getUsers() {
    return Mono.just(someService.getData());
}
```

## Правила

| Ситуация | Возврат |
|---|---|
| Без `@PreAuthorize` | Можно `List`, `String`, любой тип |
| С `@PreAuthorize` | Только `Mono<T>` или `Flux<T>` |
| Метод уже возвращает `Mono/Flux` | Без изменений |

## Минимальный шаблон

```java
@GetMapping("/api/something")
@PreAuthorize("hasRole('ADMIN')")
public Mono<ТипОтвета> myMethod() {
    return Mono.just(синхронныйВызов());
}
```

Если внутри ошибка — ловить и возвращать `Mono.just(ошибка)`:

```java
try {
    return Mono.just(service.getData());
} catch (Exception e) {
    return Mono.just(errorResponse);
}
```
