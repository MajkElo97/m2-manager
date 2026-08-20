-- Multi-organization user access (V38)

CREATE TABLE user_organizations (
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, organization_id),
    CONSTRAINT fk_user_organizations_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_organizations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_user_organizations_user_id ON user_organizations (user_id);
CREATE INDEX idx_user_organizations_organization_id ON user_organizations (organization_id);

INSERT INTO user_organizations (user_id, organization_id)
SELECT id, organization_id FROM users
ON CONFLICT DO NOTHING;

ALTER TABLE user_roles DROP CONSTRAINT fk_user_roles_user;
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);
