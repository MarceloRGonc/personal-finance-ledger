CREATE TABLE bank_balance (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL REFERENCES bank_account (id) ON DELETE CASCADE,
    year       INTEGER NOT NULL,
    amount     NUMERIC NOT NULL,
    UNIQUE (account_id, year)
);

INSERT INTO bank_balance (account_id, year, amount)
SELECT id, 2026, balance FROM bank_account;

ALTER TABLE bank_account DROP COLUMN balance;
