CREATE INDEX IF NOT EXISTS idx_task_implementer ON task(implementer_id);
CREATE INDEX IF NOT EXISTS idx_task_execute_date ON task(execute_date);
CREATE INDEX IF NOT EXISTS idx_task_trouble ON task(trouble_id);
CREATE INDEX IF NOT EXISTS idx_division_department ON division(department_id);
CREATE INDEX IF NOT EXISTS idx_employee_department ON employee(department_id);
CREATE INDEX IF NOT EXISTS idx_employee_division ON employee(division_id);
CREATE INDEX IF NOT EXISTS idx_trouble_category ON trouble(category_id);
CREATE INDEX IF NOT EXISTS idx_implementer_email ON implementer(email);
