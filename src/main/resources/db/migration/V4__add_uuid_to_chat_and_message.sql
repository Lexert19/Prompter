
ALTER TABLE chat ADD COLUMN uuid UUID NOT NULL DEFAULT gen_random_uuid();
CREATE UNIQUE INDEX idx_chat_uuid ON chat(uuid);