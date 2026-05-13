CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE message ADD COLUMN IF NOT EXISTS uuid UUID;

UPDATE message SET uuid = gen_random_uuid() WHERE uuid IS NULL;

ALTER TABLE message ALTER COLUMN uuid SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_message_uuid ON message(uuid);