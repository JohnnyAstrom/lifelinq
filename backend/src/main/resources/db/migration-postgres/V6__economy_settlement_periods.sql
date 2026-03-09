CREATE TABLE settlement_periods (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NULL,
    status VARCHAR(32) NOT NULL,
    strategy_type VARCHAR(32) NOT NULL
);

CREATE INDEX idx_settlement_periods_group_status ON settlement_periods(group_id, status);
CREATE UNIQUE INDEX ux_settlement_periods_open_per_group
    ON settlement_periods(group_id)
    WHERE status = 'OPEN';

CREATE TABLE settlement_period_participants (
    period_id UUID NOT NULL REFERENCES settlement_periods(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (period_id, user_id)
);

CREATE INDEX idx_settlement_period_participants_user ON settlement_period_participants(user_id);
