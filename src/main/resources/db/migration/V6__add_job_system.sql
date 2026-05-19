CREATE TABLE IF NOT EXISTS job_system(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    short_description VARCHAR(1000),
    detailed_description TEXT,
    url TEXT,
    system_type VARCHAR(10) NOT NULL DEFAULT 'INTERNAL'
);

CREATE INDEX IF NOT EXISTS idx_job_system_name ON job_system(name);
CREATE INDEX IF NOT EXISTS idx_job_system_system_type ON job_system(system_type);
