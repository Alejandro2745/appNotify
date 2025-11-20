CREATE TABLE IF NOT EXISTS message_log (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    from_user VARCHAR(100),
    to_user VARCHAR(100),
    topic VARCHAR(255),
    text TEXT,
    payload TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_message_log_type_created_at ON message_log(type, created_at DESC);
