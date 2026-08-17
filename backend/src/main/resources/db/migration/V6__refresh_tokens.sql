-- Refresh tokens for session lifecycle (V6)

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    family_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_by_token_id UUID,
    reuse_detected_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id, organization_id)
        REFERENCES users (id, organization_id),
    CONSTRAINT fk_refresh_tokens_replaced_by FOREIGN KEY (replaced_by_token_id)
        REFERENCES refresh_tokens (id)
);

CREATE INDEX idx_refresh_tokens_user_revoked ON refresh_tokens (user_id, revoked_at);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
