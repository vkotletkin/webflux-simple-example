CREATE TABLE IF NOT EXISTS events
(
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    payload    TEXT
);