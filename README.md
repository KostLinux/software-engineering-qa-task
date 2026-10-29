# Employee Performance & Bonus Service

A small Kotlin/Ktor REST API. It has a `/ping` health check and works out employee performance and bonuses from the public [JSONPlaceholder](https://jsonplaceholder.typicode.com) API.

The expected behaviour is specified in [`business_logic.md`](business_logic.md).

## Requirements

- **JDK 21.** Gradle 8.14 can't run on newer JDKs such as 25 or 26, so point `JAVA_HOME` at a JDK 21.
- Internet access, because the service calls `jsonplaceholder.typicode.com`.

You don't need to install Gradle. The bundled wrapper (`./gradlew`) downloads it.

## Start the app

```bash
# macOS: select JDK 21 for this shell
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

./gradlew run
```

The server listens on `http://localhost:8080`. When it is ready, Netty logs `Responding at http://0.0.0.0:8080`.

### Configuration (environment variables)

| Variable            | Default                                  | Description                    |
|---------------------|------------------------------------------|--------------------------------|
| `PORT`              | `8080`                                   | HTTP port                      |
| `UPSTREAM_BASE_URL` | `https://jsonplaceholder.typicode.com`   | Base URL of the employee API   |

```bash
PORT=9090 ./gradlew run
```

### Build and run a standalone distribution

```bash
./gradlew installDist
./build/install/software-engineering-qa-task/bin/software-engineering-qa-task
```

## Try it

```bash
curl -i http://localhost:8080/ping
curl    http://localhost:8080/ping/stats
curl    http://localhost:8080/api/v1/users/1/performance
curl    "http://localhost:8080/api/v1/leaderboard?limit=3"
```

| Method | Path                                  | Description                               |
|--------|---------------------------------------|-------------------------------------------|
| GET    | `/ping`                               | Health check, returns `pong`              |
| GET    | `/ping/stats`                         | Ping latency statistics                   |
| GET    | `/api/v1/users/{id}/performance`      | Performance and bonus of one employee     |
| GET    | `/api/v1/leaderboard?limit={n}`       | Employees ranked by performance           |

Endpoints under `/api/v1` are rate limited (see `business_logic.md`, section 5).

## Stop the app

Press `Ctrl+C` in the terminal that is running `./gradlew run`.

## Project layout

```
src/main/kotlin/com/example/pingpong/
├── Application.kt          # server bootstrap and plugin setup
├── api/                    # HTTP routes and response cache
├── client/                 # JSONPlaceholder HTTP client and DTOs
├── config/                 # constants and environment configuration
├── domain/                 # performance, tier and bonus logic
├── metrics/                # ping latency statistics
└── plugins/                # serialization, error handling, rate limiting
```
