ALTER TABLE investment_asset ADD COLUMN closed_from_year INTEGER;

CREATE TABLE investment_sale (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    year     INTEGER NOT NULL,
    month    INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    asset_id INTEGER NOT NULL REFERENCES investment_asset (id) ON DELETE CASCADE,
    proceeds NUMERIC NOT NULL CHECK (proceeds >= 0),
    cost     NUMERIC NOT NULL CHECK (cost >= 0)
);

CREATE INDEX investment_sale_year ON investment_sale (year);
