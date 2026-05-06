-- Удаление массивных колонок, дублирующих FK-связи
ALTER TABLE department DROP COLUMN IF EXISTS division_ids;
ALTER TABLE implementer DROP COLUMN IF EXISTS task_ids;
ALTER TABLE category DROP COLUMN IF EXISTS trouble_ids;
