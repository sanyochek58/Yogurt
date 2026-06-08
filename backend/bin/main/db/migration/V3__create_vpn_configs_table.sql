CREATE TABLE vpn_configs(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    vless_uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    vless_link TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vpn_configs_user_id ON vpn_configs(user_id);