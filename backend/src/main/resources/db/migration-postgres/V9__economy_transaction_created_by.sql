ALTER TABLE settlement_transactions
    ADD COLUMN created_by_user_id UUID;

UPDATE settlement_transactions
SET created_by_user_id = paid_by_user_id
WHERE created_by_user_id IS NULL;

ALTER TABLE settlement_transactions
    ALTER COLUMN created_by_user_id SET NOT NULL;

ALTER TABLE settlement_transactions
    ADD CONSTRAINT fk_settlement_transactions_created_by_user
        FOREIGN KEY (created_by_user_id) REFERENCES users(id);
