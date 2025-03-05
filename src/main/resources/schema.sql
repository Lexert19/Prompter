      
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL NOT NULL,
    embeddings JSONB,
    files JSONB,
    name VARCHAR(255),
    user_id BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL NOT NULL,
    email VARCHAR(255),
    keys JSONB,
    password VARCHAR(255),
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS project
ADD CONSTRAINT IF NOT EXISTS FKf6x9js1e9r939j0k0j9916q69
FOREIGN KEY (user_id)
REFERENCES users;

