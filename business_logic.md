# Employee Performance & Bonus Service — Business Logic

This service is a small REST API. It works out how productive an employee is and how big their quarterly bonus should be. Employee data comes from the public JSONPlaceholder API (`https://jsonplaceholder.typicode.com`):

| Upstream resource         | Meaning in our domain            |
|---------------------------|----------------------------------|
| `GET /users/{id}`         | Employee profile                 |
| `GET /todos?userId={id}`  | Tasks assigned to the employee   |
| `GET /posts?userId={id}`  | Knowledge-base articles written  |

The upstream holds exactly 10 employees, with IDs `1` to `10`.

---

## 1. Health check — `GET /ping`

- Returns `200 OK` with the plain-text body `pong`.
- It is **not** rate limited.
- It must not affect memory usage, no matter how often it is called.

## 2. Ping statistics — `GET /ping/stats`

- Returns latency statistics for the `/ping` calls made in the **last 60 seconds** (a sliding window):
  ```json
  { "sampleCount": 42, "avgLatencyMicros": 120.5, "p99LatencyMicros": 410.0 }
  ```
- Samples older than 60 seconds are thrown away. If no `/ping` arrives for 60 seconds, `sampleCount` drops back to `0`.

## 3. Employee performance — `GET /api/v1/users/{id}/performance`

### Input validation
| Case                                          | Expected response   |
|-----------------------------------------------|---------------------|
| `id` is not an integer (e.g. `abc`)           | `400 Bad Request`   |
| `id` is an integer but no such employee exists (e.g. `0`, `11`, `-1`) | `404 Not Found` |
| Upstream is unreachable or returns 5xx        | `502 Bad Gateway`   |

### Calculation rules
1. **totalTasks**: the number of tasks assigned to the employee.
2. **completedTasks**: the number of those tasks where `completed == true`.
3. **completionRate**: `completedTasks / totalTasks * 100`, rounded to 2 decimal places (HALF_UP). If `totalTasks == 0`, the rate is `0.00`.
4. **tier**: set from the completion rate as it is shown in the response. The thresholds are **inclusive**:
   | Tier     | Condition                    | Multiplier |
   |----------|------------------------------|------------|
   | `GOLD`   | `completionRate >= 60`       | 1.5        |
   | `SILVER` | `40 <= completionRate < 60`  | 1.2        |
   | `BRONZE` | `completionRate < 40`        | 1.0        |
5. **bonus**:
   ```
   bonus = BASE_BONUS * tierMultiplier + postsCount * BONUS_PER_POST
   ```
   - `BASE_BONUS = 1000`, `BONUS_PER_POST = 10`
   - Only the base bonus is multiplied by the tier. The article bonus is a flat amount added afterwards.
   - The result is capped at `2000` and rounded to 2 decimal places.
6. **currency** is always `EUR`.

### Response fields
| Field            | Source                                 |
|------------------|----------------------------------------|
| `userId`         | `user.id`                              |
| `name`           | The employee's **full name** (`user.name`), e.g. `"Leanne Graham"` |
| `email`          | `user.email`                           |
| `company`        | `user.company.name`                    |
| `totalTasks`     | rule 1                                 |
| `completedTasks` | rule 2                                 |
| `completionRate` | rule 3                                 |
| `tier`           | rule 4                                 |
| `postsCount`     | number of posts written by the user    |
| `bonus`          | rule 5                                 |
| `currency`       | `EUR`                                  |

### Worked example — employee `5`
Employee 5 has 20 tasks, 12 of them completed, and 10 posts.

```json
{
  "userId": 5,
  "name": "Chelsey Dietrich",
  "email": "Lucio_Hettinger@annie.ca",
  "company": "Keebler LLC",
  "totalTasks": 20,
  "completedTasks": 12,
  "completionRate": 60.0,
  "tier": "GOLD",
  "postsCount": 10,
  "bonus": 1600.0,
  "currency": "EUR"
}
```
The bonus is `1000 * 1.5 + 10 * 10 = 1600`.

## 4. Leaderboard — `GET /api/v1/leaderboard?limit={n}`

- Ranks **all** employees (IDs `1` to `10`) by performance.
- Sort order:
  1. `completionRate` descending
  2. `completedTasks` descending
  3. `userId` **ascending** (the lower ID wins a full tie)
- `limit`:
  - optional, default `5`
  - must be an integer in the range `1..10`
  - any other value (`0`, `11`, `-3`, `abc`) → `400 Bad Request`
- Each entry has `rank` (starting at 1), `userId`, `name` (full name), `completionRate`, `completedTasks` and `tier`.
- `GET /api/v1/leaderboard?limit=10` must return exactly 10 entries.

## 5. Rate limiting

- Applies to every endpoint under `/api/*`. It does **not** apply to `/ping` or `/ping/stats`.
- Limit: **20 requests per 60 seconds per client IP address**.
- The client IP is the TCP peer address of the connection. Headers sent by the client, such as `X-Forwarded-For`, must not be trusted. Otherwise a client could get past the limit just by changing a header.
- When the limit is exceeded, the service responds with:
  - status **`429 Too Many Requests`**
  - a `Retry-After` header holding the number of seconds until the window resets
  - the body `{ "error": "rate_limited", "message": "..." }`

## 6. Caching

- Successful responses are cached for **60 seconds** for each logical resource: one entry per employee ID for performance, and one per `limit` value for the leaderboard.
- Query parameters that do not change the result (for example `?foo=bar`) must not create new cache entries.
- Expired entries must be removed, so the cache cannot grow without bound. Memory usage must stay flat under sustained load.

## 7. Error body format

Every error response uses this format:
```json
{ "error": "<machine_code>", "message": "<human readable message>" }
```
