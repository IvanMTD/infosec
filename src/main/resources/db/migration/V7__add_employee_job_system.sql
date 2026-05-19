CREATE TABLE IF NOT EXISTS employee_job_system (
    employee_id BIGINT NOT NULL,
    job_system_uuid UUID NOT NULL REFERENCES job_system(uuid) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, job_system_uuid)
);

CREATE INDEX IF NOT EXISTS idx_emp_sys_employee ON employee_job_system(employee_id);
CREATE INDEX IF NOT EXISTS idx_emp_sys_system ON employee_job_system(job_system_uuid);
