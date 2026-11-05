# SubAdvise API Documentation

**URL:** `http://{BASE_URL}:7071/api/subscriptions`

This document outlines the available REST endpoints for managing **Subscriptions**, expenses, and currency conversion in the SubAdvise API.

### Currency

Subscription prices use the `currency` property, represented as a three-letter ISO 4217 code. Include it when creating a subscription. It is returned in subscription responses. For partial updates, omit `currency` to keep the existing value.

Accepted values:

```text
AED, AFN, ALL, AMD, AOA, ARS, AUD, AWG, AZN,
BAM, BBD, BDT, BGN, BHD, BIF, BMD, BND, BOB, BOV, BRL, BSD, BTN, BWP, BYN, BZD,
CAD, CDF, CHE, CHF, CHW, CLF, CLP, CNY, COP, COU, CRC, CUP, CVE, CZK,
DJF, DKK, DOP, DZD,
EGP, ERN, ETB, EUR,
FJD, FKP,
GBP, GEL, GHS, GIP, GMD, GNF, GTQ, GYD,
HKD, HNL, HTG, HUF,
IDR, ILS, INR, IQD, IRR, ISK,
JMD, JOD, JPY,
KES, KGS, KHR, KMF, KPW, KRW, KWD, KYD, KZT,
LAK, LBP, LKR, LRD, LSL, LYD,
MAD, MDL, MGA, MKD, MMK, MNT, MOP, MRU, MUR, MVR, MWK, MXN, MXV, MYR, MZN,
NAD, NGN, NIO, NOK, NPR, NZD,
OMR,
PAB, PEN, PGK, PHP, PKR, PLN, PYG,
QAR,
RON, RSD, RUB, RWF,
SAR, SBD, SCR, SDG, SEK, SGD, SHP, SLE, SOS, SRD, SSP, STN, SYP, SZL,
THB, TJS, TMT, TND, TOP, TRY, TTD, TWD, TZS,
UAH, UGX, USD, USN, UYI, UYU, UYW, UZS,
VES, VND, VUV,
WST,
XAF, XAG, XAU, XBA, XBB, XBC, XBD, XCD, XCG, XDR, XOF, XPD, XPF, XPT, XSU, XTS, XUA, XXX,
YER,
ZAR, ZMW, ZWG
```

---

## Summary of Endpoints

| Method     | Endpoint                                       | Description                                                       |
| ---------- | ---------------------------------------------- | ----------------------------------------------------------------- |
| **GET** | `/api/health` | Check whether the API is available |
| **GET**    | `/api/subscriptions`                           | Retrieve all subscriptions                                        |
| **GET**    | `/api/subscriptions/search?name=netflix`        | Search subscription names by case-insensitive substring           |
| **GET**    | `/api/subscriptions/{id}`                      | Retrieve a subscription by its unique ID                          |
| **POST**   | `/api/subscriptions`                           | Create a new subscription                                         |
| **PATCH**  | `/api/subscriptions/{id}`                      | Update an existing subscription partially                         |
| **DELETE** | `/api/subscriptions/{id}`                      | Delete a subscription by its unique ID                            |
| **DELETE** | `/api/data/subscriptions`                      | Delete all subscriptions                                          |
| **DELETE** | `/api/data/targets`                            | Delete all targets and expenses linked to them                    |
| **DELETE** | `/api/data/subscriptions-and-targets`          | Delete all subscriptions, targets, and expenses linked to targets |
| **PATCH**  | `/api/subscriptions/{id}/convert`              | Convert and save a subscription in another currency               |
| **GET**    | `/api/subscriptions/filter/subscription-date`  | Filter subscriptions by a specific subscription date              |
| **GET**    | `/api/subscriptions/filter/renewal-date`       | Filter subscriptions by a specific renewal date                   |
| **GET**    | `/api/subscriptions/filter/renewal-range`      | Filter subscriptions within a renewal date range                  |
| **GET**    | `/api/subscriptions/filter/subscription-range` | Filter subscriptions within a subscription date range             |
| **GET**    | `/api/currency/convert`                        | Convert an amount using the latest available rates                |

---

## Detailed Endpoint Reference

### Health Check

* **URL:** `GET /api/health`
* **Description:** Confirms that the API is available.
* **Response Status:** `200 OK`
* **Response Body:**

```json
{
  "status": "UP"
}
```

### 1. Get All Subscriptions

* **URL:** `GET /api/subscriptions`
* **Description:** Returns a list of all saved subscriptions.
* **Response Status:** `200 OK`
* **Response Body Example:**

```json
[
  {
    "subscriptionId": "a1b2c3d4-e5f6-7890-abcd-ef0123456789",
    "subscriptionName": "Netflix",
    "subscriptionDate": "2026-01-01",
    "renewalDate": "2027-01-01",
    "subscriptionPrice": 15.99,
    "currency": "USD"
  }
]
```

---

### Search Subscriptions by Name

* **URL:** `GET /api/subscriptions/search?name=netflix`
* **Description:** Returns subscriptions whose names contain the supplied text, ignoring case. The `name` parameter must not be blank.
* **Response Status:** `200 OK`
* **Response Body:** A list of matching subscription objects; an empty list is returned when there are no matches.

---

### 2. Get Subscription By ID

* **URL:** `GET /api/subscriptions/{id}`
* **Description:** Fetches details of a single subscription by its UUID.
* **Path Parameters:**
* `id` (UUID) - The unique identifier of the subscription.


* **Response Status:**
* `200 OK` (Found)
* `404 Not Found` (If ID does not exist)


* **Response Body Example:**

```json
{
  "subscriptionId": "a1b2c3d4-e5f6-7890-abcd-ef0123456789",
  "subscriptionName": "Netflix",
  "subscriptionDate": "2026-01-01",
  "renewalDate": "2027-01-01",
  "subscriptionPrice": 15.99,
  "currency": "USD"
}

```

---

### 3. Create Subscription

* **URL:** `POST /api/subscriptions`
* **Description:** Creates a new subscription record.
* **Request Body (`SubscriptionRequestDto`):**

When changing `currency` through this endpoint, include a replacement `subscriptionPrice`. To convert the existing price using current exchange rates instead, use `PATCH /api/subscriptions/{id}/convert?to=EUR`.

```json
{
  "subscriptionName": "Spotify",
  "subscriptionDate": "2026-06-01",
  "renewalDate": "2027-06-01",
  "subscriptionPrice": 9.99,
  "currency": "USD"
}

```

* **Response Status:** `200 OK` or `201 Created`
* **Response Body Example:**

```json
{
  "subscriptionId": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
  "subscriptionName": "Spotify",
  "subscriptionDate": "2026-06-01",
  "renewalDate": "2027-06-01",
  "subscriptionPrice": 9.99,
  "currency": "USD"
}

```

---

### 4. Update Subscription

* **URL:** `PATCH /api/subscriptions/{id}`
* **Description:** Partially updates an existing subscription.
* **Path Parameters:**
* `id` (UUID) - The unique identifier of the subscription to update.


* **Request Body (`SubscriptionRequestDto`):**

```json
{
  "subscriptionName": "Spotify Premium",
  "subscriptionPrice": 11.99,
  "currency": "USD"
}

```

* **Response Status:**
* `200 OK` (Updated successfully)
* `404 Not Found` (If ID does not exist)


* **Response Body Example:**

```json
{
  "subscriptionId": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
  "subscriptionName": "Spotify Premium",
  "subscriptionDate": "2026-06-01",
  "renewalDate": "2027-06-01",
  "subscriptionPrice": 11.99,
  "currency": "USD"
}

```

---

### 5. Delete Subscription

* **URL:** `DELETE /api/subscriptions/{id}`
* **Description:** Deletes a subscription by its UUID.
* **Path Parameters:**
* `id` (UUID) - The unique identifier of the subscription to delete.


* **Response Status:**
* `204 No Content` (Successfully deleted)
* `404 Not Found` (If ID does not exist)



---

### 6. Purge Subscriptions

* **URL:** `DELETE /api/data/subscriptions`
* **Description:** Permanently deletes all subscriptions. Targets, expenses, and expense color thresholds are not affected.
* **Response Status:** `204 No Content`

---

### 7. Purge Targets

* **URL:** `DELETE /api/data/targets`
* **Description:** Permanently deletes all targets and their linked expenses. Subscriptions and expense color thresholds are not affected.
* **Response Status:** `204 No Content`

---

### 8. Purge Subscriptions and Targets

* **URL:** `DELETE /api/data/subscriptions-and-targets`
* **Description:** Permanently deletes all subscriptions and targets. Expenses are deleted first because each expense references a target. Expense color thresholds are not affected.
* **Response Status:** `204 No Content`

---

### 9. Filter by Subscription Date

* **URL:** `GET /api/subscriptions/filter/subscription-date`
* **Description:** Retrieves all subscriptions matching a exact subscription date.
* **Query Parameters:**
* `date` (String, format: `YYYY-MM-DD`) - e.g., `?date=2026-06-01`


* **Response Status:** `200 OK`

---

### 10. Convert a Subscription to Another Currency

* **URL:** `PATCH /api/subscriptions/{id}/convert?to=EUR`
* **Description:** Converts the saved subscription price from its current currency to the requested currency, then updates the subscription's stored price and currency.
* **Path Parameter:**
  * `id` (UUID) - The subscription to convert.
* **Query Parameter:**
  * `to` (currency code) - The target currency, such as `EUR`.
* **Request Body:** None.
* **Response Status:**
  * `200 OK` (Conversion completed and saved)
  * `400 Bad Request` (Malformed currency parameter)
  * `404 Not Found` (Subscription does not exist)
  * `422 Unprocessable Entity` (The provider has no rate for the target currency)
  * `503 Service Unavailable` (Current rates are unavailable)
* **Response Body Example:**

```json
{
  "subscription": {
    "subscriptionId": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
    "subscriptionName": "Spotify",
    "subscriptionDate": "2026-06-01",
    "renewalDate": "2027-06-01",
    "subscriptionPrice": 9.18,
    "currency": "EUR"
  },
  "conversion": {
    "amount": 9.99,
    "from": "USD",
    "to": "EUR",
    "convertedAmount": 9.18,
    "rateLastUpdated": "2026-10-17T00:02:32Z",
    "rateNextUpdate": "2026-10-18T00:11:42Z",
    "rateProvider": "ExchangeRate-API Open Access",
    "attributionUrl": "https://www.exchangerate-api.com"
  }
}
```

The conversion uses the current stored subscription price and currency. The database update happens only after successful rate conversion; if the rate provider fails or does not support the requested currency, the subscription remains unchanged.

---

### 11. Filter by Renewal Date

* **URL:** `GET /api/subscriptions/filter/renewal-date`
* **Description:** Retrieves all subscriptions matching an exact renewal date.
* **Query Parameters:**
* `date` (String, format: `YYYY-MM-DD`) - e.g., `?date=2027-06-01`


* **Response Status:** `200 OK`

---

### 12. Filter by Renewal Date Range

* **URL:** `GET /api/subscriptions/filter/renewal-range`
* **Description:** Retrieves subscriptions whose renewal dates fall between a specified start and end date.
* **Query Parameters:**
* `startDate` (String, format: `YYYY-MM-DD`)
* `endDate` (String, format: `YYYY-MM-DD`)
* Example: `?startDate=2026-01-01&endDate=2026-12-31`


* **Response Status:** `200 OK`

---

### 13. Filter by Subscription Date Range

* **URL:** `GET /api/subscriptions/filter/subscription-range`
* **Description:** Retrieves subscriptions whose subscription start dates fall between a specified start and end date.
* **Query Parameters:**
* `startDate` (String, format: `YYYY-MM-DD`)
* `endDate` (String, format: `YYYY-MM-DD`)
* Example: `?startDate=2026-01-01&endDate=2026-06-30`


* **Response Status:** `200 OK`

---

### 14. Convert Currency

* **URL:** `GET /api/currency/convert`
* **Description:** Converts an amount from one currency to another using the latest available ExchangeRate-API rates.
* **Query Parameters:**
  * `amount` (decimal, zero or greater)
  * `from` (currency code, for example `USD`)
  * `to` (currency code, for example `EUR`)
* **Example:** `?amount=100&from=USD&to=EUR`
* **Response Status:**
  * `200 OK` (Converted successfully)
  * `400 Bad Request` (Invalid or negative amount, or malformed parameter)
  * `422 Unprocessable Entity` (A valid enum currency has no rate from the provider)
  * `503 Service Unavailable` (Current rates could not be retrieved)
* **Response Body Example:**

```json
{
  "amount": 100,
  "from": "USD",
  "to": "EUR",
  "convertedAmount": 92.14,
  "rateLastUpdated": "2026-10-17T00:02:32Z",
  "rateNextUpdate": "2026-10-18T00:11:42Z",
  "rateProvider": "ExchangeRate-API Open Access",
  "attributionUrl": "https://www.exchangerate-api.com"
}
```

Rates are fetched from `https://open.er-api.com/v6/latest/USD` and cached until the provider's `rateNextUpdate`. The first conversion request at or after that time fetches a fresh snapshot. The free feed refreshes once per day, not in real time. Conversions use USD-based cross rates and round the result to the target currency's standard minor-unit precision. Some enum codes (such as fund, metal, and special-purpose codes) are not provided with an exchange rate and return `422`. Responses include the provider attribution required by its open-access terms; clients displaying converted amounts must retain an attribution link to [ExchangeRate-API](https://www.exchangerate-api.com).

---

## Expenses

### Create a Target Expense

* **URL:** `POST /api/expenses/targets`
* **Description:** Creates a target amount and date period.
* **Request Body:** `targetExpense`, `currency`, optional `startDate`, and required `endDate`.
* **Response Status:** `201 Created`

### Record an Actual Expense

* **URL:** `POST /api/expenses`
* **Description:** Records an actual amount against an existing target. The expense and target currencies must match.
* **Request Body:** `actualExpense`, `currency`, and `targetId`.
* **Response Status:** `201 Created`; `404 Not Found` if the target does not exist.

The returned `difference` is calculated as `actualExpense - targetExpense`; positive values are over target and negative values are under target.

### Get Monthly Expenses

* **URL:** `GET /api/expenses/monthly?year=2026&month=2`
* **Description:** Returns expense totals grouped by currency and a color classification for each expense. The target's `endDate` determines its month, including the month's final day (28, 29, 30, or 31).
* **Response Status:** `200 OK`

Each expense's percentage is calculated against the total for its currency in that month. Totals in different currencies are not combined.

### Configure Expense Colors

* **URL:** `GET` or `PUT /api/expenses/color-thresholds`
* **Description:** Reads or replaces application-wide minimum percentage thresholds for `red`, `orange`, `yellow`, `lime`, and `green`.
* **Request Body for `PUT`:**

```json
{
  "red": 50,
  "orange": 30,
  "yellow": 15,
  "lime": 5,
  "green": 0
}
```

Thresholds must be between 0 and 100 and strictly descend from red to green. A monthly expense is assigned the first color whose minimum threshold is met. Defaults are `50`, `30`, `15`, `5`, and `0` respectively.