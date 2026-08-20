-- Align refresh_tokens FK with multi-organization model (V40)

ALTER TABLE refresh_tokens DROP CONSTRAINT fk_refresh_tokens_user;

ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_organization_id
    FOREIGN KEY (organization_id) REFERENCES organizations(id);
