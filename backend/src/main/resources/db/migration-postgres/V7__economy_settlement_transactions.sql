CREATE TABLE settlement_transactions (
    id UUID PRIMARY KEY,
    period_id UUID NOT NULL REFERENCES settlement_periods(id) ON DELETE CASCADE,
    amount NUMERIC(19, 4) NOT NULL,
    description TEXT NOT NULL,
    paid_by_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    category VARCHAR(100) NULL
);

CREATE INDEX idx_settlement_transactions_period
    ON settlement_transactions(period_id);

CREATE INDEX idx_settlement_transactions_period_deleted
    ON settlement_transactions(period_id, deleted_at);
