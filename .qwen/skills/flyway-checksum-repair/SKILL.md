---
name: flyway-checksum-repair
description: Fix Flyway migration checksum mismatch by deleting the schema history row and reapplying
source: auto-skill
extracted_at: '2026-06-08T11:00:00.000Z'
---

# Fix Flyway checksum mismatch

When a migration file content changes after it was already applied to a database, Flyway detects a checksum mismatch and refuses to start:

```
Migration checksum mismatch for migration version N
 -> Applied to database : 2053567691
 -> Resolved locally    : -1614467908
```

## Fix

Connect to the database and delete the offending row from Flyway's schema history table:

```sql
DELETE FROM flyway_schema_history WHERE version = 'N';
```

Replace `N` with the version number from the error (e.g., `'8'`).

Then restart the application. Flyway will re-apply the migration with the new file content.

## When to use

- Only when the **same** migration version (same V number) needs different SQL content
- Safe because the table structure is already at the target state from the failed attempt
- Do NOT use if the migration was partially applied — in that case, manually fix the schema first

## Prevention

Never edit an already-applied migration file. Always create a new V-file (V9, V10…) for new changes.
