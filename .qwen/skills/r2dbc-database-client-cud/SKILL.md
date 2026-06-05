---
name: r2dbc-database-client-cud
description: Use DatabaseClient.fetch().rowsUpdated() for CUD on tables with composite keys — avoids R2DBC one() errors
source: auto-skill
extracted_at: '2026-06-08T11:00:00.000Z'
---

# DatabaseClient for CUD on composite-key tables

When a Spring R2DBC `@Table` has a composite primary key (no single `@Id` field), **never use `@Query`** on the repository for `Mono<>` returning methods. R2DBC calls `DefaultFetchSpec.one()` internally, which throws `IncorrectResultSizeDataAccessException: non unique result`.

## Solution

Use `DatabaseClient` (auto-configured by `spring-boot-starter-data-r2dbc`) and inject it:

```java
private final DatabaseClient databaseClient;
// Spring injects automatically — no config needed
```

### UPDATE with nullable fields

```java
var spec = databaseClient.sql("UPDATE tbl SET col1 = :v1, col2 = :v2 WHERE id1 = :id1 AND id2 = :id2")
    .bind("v1", dto.getVal1())
    .bind("id1", id1)
    .bind("id2", id2);
// Handle nulls explicitly — .bind() rejects null!
spec = dto.getVal2() != null ? spec.bind("v2", dto.getVal2()) : spec.bindNull("v2", String.class);
return spec.fetch().rowsUpdated().then(...);
```

### DELETE

```java
return databaseClient.sql("DELETE FROM tbl WHERE id1 = :id1 AND id2 = :id2")
    .bind("id1", id1).bind("id2", id2)
    .fetch().rowsUpdated().then();
```

## What NOT to use

- `@Query("SELECT...")` returning `Mono<T>` → one() crash
- `@Query("UPDATE...")` returning `Mono<Void>` → one() crash  
- `@Query("DELETE...")` returning `Mono<Integer>` → one() crash
- `repository.save(entity)` on tables without `@Id` → always INSERT, never UPDATE

## Only safe operations for composite-key tables

- `Flux<T>` methods (no `one()`)
- `DatabaseClient.fetch().rowsUpdated()` for CUD
- Java-side filtering with `.filter().next()` instead of `@Query` SELECT
