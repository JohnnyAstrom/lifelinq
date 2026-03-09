ALTER TABLE settlement_periods
    ADD COLUMN strategy_snapshot_json TEXT;

UPDATE settlement_periods
SET strategy_snapshot_json = CASE
    WHEN strategy_type = 'PERCENTAGE_COST'
        THEN '{"strategyType":"PERCENTAGE_COST","percentageShares":{}}'
    ELSE '{"strategyType":"EQUAL_COST","percentageShares":{}}'
END
WHERE strategy_snapshot_json IS NULL;

ALTER TABLE settlement_periods
    ALTER COLUMN strategy_snapshot_json SET NOT NULL;
