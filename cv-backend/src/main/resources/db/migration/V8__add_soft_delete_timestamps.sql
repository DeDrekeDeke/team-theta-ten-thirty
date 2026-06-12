ALTER TABLE user_account
    ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE cv
    ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

CREATE INDEX idx_user_account_deleted_at ON user_account(deleted_at);
CREATE INDEX idx_cv_deleted_at ON cv(deleted_at);
CREATE INDEX idx_cv_owner_deleted_archived ON cv(owner_user_id, deleted_at, archived_at);