CREATE TABLE IF NOT EXISTS member (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id         BIGSERIAL    PRIMARY KEY,
    member_id  BIGINT       NOT NULL UNIQUE REFERENCES member (id) ON DELETE CASCADE,
    token      TEXT         NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_token (token);
