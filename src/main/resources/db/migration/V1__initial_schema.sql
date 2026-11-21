CREATE TABLE revenue_field (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    code       TEXT    NOT NULL UNIQUE,
    label      TEXT    NOT NULL,
    kind       TEXT    NOT NULL CHECK (kind IN ('EARNING', 'DEDUCTION', 'INFO')),
    sort_order INTEGER NOT NULL DEFAULT 0,
    attributes TEXT
);

CREATE TABLE revenue_value (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    year       INTEGER NOT NULL,
    month      INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    field_code TEXT    NOT NULL REFERENCES revenue_field (code) ON UPDATE CASCADE ON DELETE CASCADE,
    amount     NUMERIC NOT NULL,
    UNIQUE (year, month, field_code)
);

CREATE TABLE revenue_note (
    id    INTEGER PRIMARY KEY AUTOINCREMENT,
    year  INTEGER NOT NULL,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    note  TEXT    NOT NULL,
    UNIQUE (year, month)
);

CREATE TABLE revenue_line (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    year        INTEGER NOT NULL,
    month       INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    description TEXT    NOT NULL,
    amount      NUMERIC NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    attributes  TEXT
);
CREATE INDEX idx_revenue_line_period ON revenue_line (year, month);

CREATE TABLE expense (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    year        INTEGER NOT NULL,
    month       INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    description TEXT    NOT NULL,
    amount      NUMERIC NOT NULL,
    counted     INTEGER NOT NULL DEFAULT 1 CHECK (counted IN (0, 1)),
    sort_order  INTEGER NOT NULL DEFAULT 0,
    attributes  TEXT
);
CREATE INDEX idx_expense_period ON expense (year, month);

CREATE TABLE investment_asset (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL UNIQUE,
    tags       TEXT    NOT NULL DEFAULT '',
    sort_order INTEGER NOT NULL DEFAULT 0,
    attributes TEXT
);

CREATE TABLE investment_base (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    year     INTEGER NOT NULL,
    asset_id INTEGER NOT NULL REFERENCES investment_asset (id) ON DELETE CASCADE,
    amount   NUMERIC NOT NULL,
    UNIQUE (year, asset_id)
);

CREATE TABLE investment_contribution (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    year     INTEGER NOT NULL,
    month    INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    asset_id INTEGER NOT NULL REFERENCES investment_asset (id) ON DELETE CASCADE,
    amount   NUMERIC NOT NULL,
    UNIQUE (year, month, asset_id)
);

CREATE TABLE loan_entry (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    loan_name  TEXT    NOT NULL,
    label      TEXT    NOT NULL,
    amount     NUMERIC NOT NULL,
    kind       TEXT    NOT NULL CHECK (kind IN ('PRINCIPAL', 'OPENING', 'PAYMENT')),
    counted    INTEGER NOT NULL DEFAULT 1 CHECK (counted IN (0, 1)),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE investment_target (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    label  TEXT    NOT NULL UNIQUE,
    amount NUMERIC NOT NULL
);

CREATE TABLE annual_figure (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    year   INTEGER NOT NULL,
    metric TEXT    NOT NULL,
    amount NUMERIC NOT NULL,
    UNIQUE (year, metric)
);

CREATE TABLE bank_account (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL UNIQUE,
    balance    NUMERIC NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    attributes TEXT
);

CREATE TABLE formula_rule (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    code        TEXT    NOT NULL UNIQUE,
    scope       TEXT    NOT NULL CHECK (scope IN ('MONTH', 'YEAR', 'PORTFOLIO')),
    expression  TEXT    NOT NULL,
    description TEXT,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    active      INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
);
