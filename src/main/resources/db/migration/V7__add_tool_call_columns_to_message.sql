ALTER TABLE message
    ADD COLUMN tool_calls_json  TEXT,
    ADD COLUMN tool_call_id     VARCHAR(255),
    ADD COLUMN tool_name        VARCHAR(255),
    ADD COLUMN string_content   TEXT;