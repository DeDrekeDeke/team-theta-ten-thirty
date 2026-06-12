ALTER TABLE user_account
DROP CONSTRAINT IF EXISTS user_account_email_key;

CREATE UNIQUE INDEX uq_user_account_active_email
ON user_account (lower(email))
WHERE deleted_at IS NULL;
