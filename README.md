# SubAdvise API

SubAdvise is a Spring Boot REST API for subscription tracking and currency conversion. It also contains Target and Expense persistence models and data-access components.

## Features

- Create, read, partially update, delete, and filter subscriptions by subscription or renewal dates.
- Store ISO 4217 currency codes with subscription prices, target expenses, and actual expenses.
- Calculate an Expense difference as `targetExpense - actualExpense`; the Expense and Target currencies must match.
- Convert amounts using current available exchange-rate snapshots from ExchangeRate-API Open Access.

## Requirements

- JDK 25 or later.
- MariaDB available at `localhost:3306`.
- A database named `subadvise`.
- Internet access for currency conversion requests.

The local default datasource settings are in `src/main/resources/application.properties` (`root` / `root`). Configure credentials securely for other environments. Hibernate is configured with `ddl-auto=update`.

## Run

From the repository root on Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dmaven.compiler.proc=full"
```

The API listens on `http://localhost:7071` by default. The datasource and port can be overridden with Spring configuration, including `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `SERVER_PORT`. The rate provider URL can be overridden with `CURRENCY_RATES_API_URL`.

### Use a Local MariaDB

If you do not want to use Docker, install and start MariaDB on your machine, listening on port `3306`, and create a database named `subadvise`. Configure the application credentials with `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. The datasource URL should point to the local server:

```text
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/subadvise
```

When running the API directly on your machine, `localhost` is correct. The Compose setup below uses `db` instead because the API runs in a container.

## Run with Docker Compose

Install Docker Desktop (or Docker Engine with the Compose plugin) and Git. From the `subadvise-api` repository directory, create a local `.env` file with the database credentials. Compose creates the `subadvise` database and application user on the first database startup. The API connection URL defaults to `jdbc:mariadb://db:3306/subadvise`; `db` is the MariaDB service name, so do not change it to `localhost` when running in Compose.

On Linux or macOS, run:

```sh
cat > .env <<'EOF'
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password
MARIADB_ROOT_PASSWORD=root
EOF
docker compose up --build -d
docker compose ps
```

On Windows PowerShell, run:

```powershell
@'
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password
MARIADB_ROOT_PASSWORD=root
'@ | Set-Content -Encoding ascii .env
docker compose up --build -d
docker compose ps
```

The API is available at `http://localhost:7071`. View its logs with `docker compose logs -f api`; stop the services with `docker compose down`. The database is stored in a persistent volume, so stopping the services does not delete data. These credentials are for local development; use strong, unique passwords for any shared or deployed environment. MariaDB applies the username and password when it initializes an empty data volume. Changing `.env` later does not change credentials in an existing database.

After the API is running, clone the AI API repository beside this one. From the `subadvise-api` directory, run:

```sh
cd ..
git clone https://github.com/drjkumr/subadvise-ai-api.git
```

## Subscription API

Base URL: `http://localhost:7071/api/subscriptions`

| Method   | Path                                                                                   | Purpose                                        |
| -------- | -------------------------------------------------------------------------------------- | ---------------------------------------------- |
| `GET`    | `/api/subscriptions`                                                                   | List subscriptions                             |
| `GET`    | `/api/subscriptions/{id}`                                                              | Get a subscription; returns `404` if missing   |
| `POST`   | `/api/subscriptions`                                                                   | Create a subscription                          |
| `PATCH`  | `/api/subscriptions/{id}`                                                              | Partially update a subscription                |
| `POST`   | `/api/subscriptions/{id}/convert?to=EUR`                                               | Convert and save its price in another currency |
| `DELETE` | `/api/subscriptions/{id}`                                                              | Delete a subscription                          |
| `GET`    | `/api/subscriptions/filter/subscription-date?date=YYYY-MM-DD`                          | Match subscription date                        |
| `GET`    | `/api/subscriptions/filter/renewal-date?date=YYYY-MM-DD`                               | Match renewal date                             |
| `GET`    | `/api/subscriptions/filter/renewal-range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`      | Match renewal date range                       |
| `GET`    | `/api/subscriptions/filter/subscription-range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Match subscription date range                  |

Create example:

```http
POST /api/subscriptions
Content-Type: application/json

{
  "subscriptionName": "Music service",
  "subscriptionDate": "2026-06-01",
  "renewalDate": "2027-06-01",
  "subscriptionPrice": 9.99,
  "currency": "USD"
}
```

`currency` is required when creating a subscription and must be an uppercase code from the `Currency` enum in `src/main/java/com/drjcodes/subadvise_api/enums/Currency.java`. Enums are in the `enums` package and application exceptions are in the `exception` package. Subscription responses include the currency. For `PATCH`, omitted fields (including `currency`) are left unchanged. If changing currency with `PATCH`, provide a replacement `subscriptionPrice` too; use the conversion endpoint to convert the existing amount.

## Expense API

| Method | Path                                      | Purpose                                                     |
| ------ | ----------------------------------------- | ----------------------------------------------------------- |
| `POST` | `/api/expenses/targets`                   | Create a target amount and period                           |
| `POST` | `/api/expenses`                           | Record an actual expense against a target                   |
| `GET`  | `/api/expenses/monthly?year=2026&month=2` | Get month totals by currency and each expense's share/color |
| `GET`  | `/api/expenses/color-thresholds`          | Read the configured color thresholds                        |
| `PUT`  | `/api/expenses/color-thresholds`          | Replace the configured color thresholds                     |

Monthly totals use the linked target's `endDate`, including the last day of the requested month (28, 29, 30, or 31). Amounts are grouped by currency and are never added across currencies. Each expense's percentage is its actual amount divided by that month's total in the same currency. Color thresholds are minimum percentages, must descend from red to green, and are shared application-wide. Defaults are red `50`, orange `30`, yellow `15`, lime `5`, and green `0`.

Expense differences are calculated as `actualExpense - targetExpense`.

## Currency Conversion

```http
GET /api/currency/convert?amount=100&from=USD&to=EUR
```

The query parameters are `amount` (zero or greater), `from`, and `to`. Currency codes must be uppercase enum values. The response contains the input and converted amounts, source and target codes, the rate snapshot's last and next update times, and provider attribution.

Rates come from `https://open.er-api.com/v6/latest/USD`. The service caches the snapshot until the provider's published next-update time; the first conversion request at or after that time fetches a new snapshot. The free feed refreshes daily, so these rates are not real-time. Conversions use USD-based cross rates and round to the target currency's standard minor-unit precision. The enum includes codes the feed may not cover; those conversions return `422` rather than guessing a rate. Invalid or negative amounts return `400`; unavailable or expired provider data returns `503`.

Responses include the `attributionUrl` required by ExchangeRate-API Open Access. Applications displaying converted amounts must preserve a visible attribution link to [ExchangeRate-API](https://www.exchangerate-api.com). Review the provider's [open-access terms](https://www.exchangerate-api.com/docs/free) before deploying or redistributing conversion output.

### Convert a Subscription

To convert a saved subscription and persist its new price and currency, call:

```http
POST /api/subscriptions/{id}/convert?to=EUR
```

No request body is required. The response contains the updated subscription and the conversion details, including rate timestamps and provider attribution. The stored price and currency are updated only after a current rate is successfully retrieved. A missing subscription returns `404`; unsupported rates return `422`; unavailable rates return `503`.

## Data Model

- `Subscription`: name, subscription date, renewal date, price, and currency. A missing subscription date defaults to the current date when persisted.
- `Target`: target expense, currency, start date, and end date. A missing start date defaults to the current date when persisted.
- `Expense`: actual expense, difference, currency, and a one-to-one Target reference. The difference is computed before insert/update and mismatched currencies are rejected.

Target and Expense DTOs, mappers, repositories, and model logic are present, but corresponding REST controllers and complete service workflows are not yet implemented.

## Tests

Run the full test suite with the Maven wrapper:

```powershell
.\mvnw.cmd clean test "-Dmaven.compiler.proc=full"
```

The tests cover controller routes, service behavior, DTO mapping, entity callbacks, and repository persistence using H2 for JPA slice tests. The `-Dmaven.compiler.proc=full` option enables Lombok annotation processing with the configured JDK.
