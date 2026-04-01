DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS member;

CREATE TABLE member (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id  BIGINT       NOT NULL UNIQUE,
    token      TEXT         NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_token (token);
