CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nick VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE wallet_members (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    member_role VARCHAR(32) NOT NULL,
    can_see_budget BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (wallet_id, user_id)
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    tip VARCHAR(16) NOT NULL,
    icon_key VARCHAR(32) NOT NULL DEFAULT 'cart',
    color_bg VARCHAR(16) NOT NULL DEFAULT '#AAF0D1',
    created_by BIGINT REFERENCES users (id)
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users (id),
    category_id BIGINT NOT NULL REFERENCES categories (id),
    type VARCHAR(16) NOT NULL,
    amount DECIMAL(14, 2) NOT NULL CHECK (amount > 0),
    transaction_date DATE NOT NULL,
    comment VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE split_expenses (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id),
    share_amount DECIMAL(14, 2) NOT NULL CHECK (share_amount > 0)
);

CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories (id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    limit_amount DECIMAL(14, 2) NOT NULL DEFAULT 0
);

CREATE TABLE recurring_rules (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories (id),
    amount DECIMAL(14, 2) NOT NULL CHECK (amount > 0),
    day_of_month INT NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    next_run_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    comment VARCHAR(512)
);

CREATE INDEX idx_transactions_wallet_date ON transactions (wallet_id, transaction_date);
CREATE INDEX idx_wallet_members_user ON wallet_members (user_id);
CREATE INDEX idx_budgets_wallet_period ON budgets (wallet_id, period_start, period_end);
