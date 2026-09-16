UPDATE user_file SET base64_path = base64path
WHERE base64_path IS NULL AND base64path IS NOT NULL;

ALTER TABLE user_file DROP COLUMN IF EXISTS base64path;
ALTER TABLE user_file ALTER COLUMN base64_path DROP NOT NULL;