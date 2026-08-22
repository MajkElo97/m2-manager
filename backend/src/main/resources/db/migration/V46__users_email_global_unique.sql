-- Global unique user login/email (V46)

ALTER TABLE users DROP CONSTRAINT uq_users_organization_email;

ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
