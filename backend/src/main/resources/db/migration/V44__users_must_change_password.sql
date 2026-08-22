-- Require password change on first login for provisioned accounts (V44)

ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
