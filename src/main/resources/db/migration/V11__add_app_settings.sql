CREATE TABLE IF NOT EXISTS app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL DEFAULT ''
);

INSERT INTO app_settings (key, value) VALUES ('ldap.sync.enabled', 'false')
ON CONFLICT (key) DO NOTHING;
