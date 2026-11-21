# Personal Finance Ledger

A local personal-finance app for tracking income, expenses and investments by month and year.
Java 25, Spring Boot, SQLite, Thymeleaf, HTMX, Tailwind CSS and Chart.js. All data stays in one SQLite file on your machine.

## Features
- **Summary**: yearly cards, monthly roll-up, historical table, comparison across years with year-on-year change and a chart, and bank accounts with a balance per year.
- **Salary**: monthly grid for gross pay, allowances and deductions.
- **Extra Revenue**: per-month list of extra income lines, split by quarter.
- **Expenses**: three months side by side per quarter. Add one expense at a time, or paste a whole month (description and amount per line) and check a preview before saving. Entries can be excluded from the total.
- **Investments**: one column per investment, one row per month, with a sticky month column. Record sales with proceeds and cost, reorder or permanently remove investments, and track loans. Pie charts show allocation and ETFs.
- **Backup**: download or upload a `.db` snapshot. A daily automatic snapshot keeps the last 14, and a copy is saved before any restore.

The year in the header applies to every page.

## Run

    mvn test
    mvn spring-boot:run -Dspring-boot.run.profiles=dev    # http://localhost:8080

The `dev` profile turns off template caching so edits to templates show on refresh.

With Docker:

    docker compose up --build

On Linux, run `FINANCES_UID=$(id -u) FINANCES_GID=$(id -g) docker compose up --build` so the `./data` bind mount is writable. The container runs as a non-root user with a read-only filesystem and no extra capabilities.

On first start the app creates `finances.db` in the data folder with its default revenue columns. Add data from the Summary, Salary, Extra Revenue, Expenses and Investments pages, or paste a whole month of expenses.

## Configuration

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_ADDRESS` | `127.0.0.1` | Interface to listen on. The container sets `0.0.0.0`. |
| `FINANCE_DATA_DIR` | `./data` | Folder holding `finances.db`. |
| `FINANCE_BASE_YEAR` | `2023` | First year of your history. Its yearly figures are typed in on the Summary page; later years are calculated. |
| `FINANCE_BACKUP_DIR` | `./data/backups` | Where snapshots are kept. |

## Security
There is no login. The app is meant for one person on a trusted machine, so it listens on localhost by default and `docker-compose.yml` publishes the port on `127.0.0.1` only. Do not expose it to a network or the internet without putting authentication in front of it.

Browsers are protected from other websites by refusing state-changing requests that do not come from the app's own origin, and every response carries a strict Content-Security-Policy, `X-Frame-Options` and `X-Content-Type-Options`. Uploaded backups must pass a SQLite header, integrity and schema check before they replace the database.

## How the yearly figures work
- The base year (`FINANCE_BASE_YEAR`, 2023 by default) is the first year of your history. Its Net Value, Invested, Gross, Net and Invested-for-the-year values are typed once on the Summary page.
- Every later year starts from the previous year's Net Value and Invested. Its Net Value is that year's Total Liquid, and its Invested is the previous Invested plus the year's investments, minus the cost of anything sold.
- A year with no monthly data keeps the previous year's values.
- Each year's opening investment values come from the previous year, unless you type an explicit opening value.
- Bank balances are stored per year. A year without its own balance uses the latest earlier one, so changing this year's balance never rewrites history.

## Extending
- New revenue column: Salary page. Earning columns add to net, deduction columns subtract, info columns are ignored by the default formulas.
- New investment: Investments page. Tags choose which pie charts include it.
- New or changed formula: edit `src/main/java/com/finances/formula/DefaultFormulaCatalog.java`. Month rules become roll-up columns, year rules become summary figures, and portfolio rules feed the investments page. Rules are checked when the app starts.
- New fields on a table: add a Flyway migration. Never edit an existing migration.

## Frontend
Styles live in `frontend/src/input.css` and are compiled into `src/main/resources/static/css/app.css`. After changing a template or the stylesheet:

    cd frontend && npm ci && npm run build

The build also copies the pinned htmx and Chart.js files into `static/js`. CI fails if the committed assets are out of date.
