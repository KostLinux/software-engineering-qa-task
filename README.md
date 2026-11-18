# Employee Performance & Bonus Service

A small Kotlin/Ktor REST API. It has a `/ping` health check and works out employee performance and bonuses from the public [JSONPlaceholder](https://jsonplaceholder.typicode.com) API.

The expected behaviour is specified in [`business_logic.md`](business_logic.md).

## Requirements

- **JDK 21.** Gradle 8.14 can't run on newer JDKs such as 25 or 26, so point `JAVA_HOME` at a JDK 21.
- Internet access, because the service calls `jsonplaceholder.typicode.com`.

You don't need to install Gradle. The bundled wrapper (`./gradlew`) downloads it.

## Start the app

### Locally

```bash
# macOS: select JDK 21 for this shell
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

./gradlew run
```

### Via GitHub Codespaces

The repository includes a dev container (`.devcontainer/`) with JDK 21, Kotlin, k6, hurl, curl, wget, httpie, jq, newman and Claude Code already installed.

1. On the repository page on GitHub, click **Code → Codespaces → Create codespace on main**.
   With the GitHub CLI: `gh codespace create --repo <owner>/<repo>`, then `gh codespace code` to open it in VS Code.
2. Wait for the setup to finish. On first start it downloads Gradle and the dependencies and compiles the project, which takes a few minutes.
3. In the codespace terminal, run:

   ```bash
   ./gradlew run
   ```

4. Port `8080` is forwarded automatically. Open the **Ports** tab to get the public URL (`https://<codespace-name>-8080.app.github.dev`), or call the app from the codespace terminal:

   ```bash
   curl http://localhost:8080/ping
   ```

To use Claude Code, either add an `ANTHROPIC_API_KEY` secret under **GitHub → Settings → Codespaces → Secrets**, or run `claude` in the terminal and log in.

> **Rate-limit testing:** run load and rate-limit tests (k6, curl) from inside the codespace against `localhost:8080`. Requests through the forwarded URL all pass through GitHub's proxy, so they don't show the real client IP.

The same dev container also works locally: with Docker running, open the project in VS Code and choose **Dev Containers: Reopen in Container**.

### Server address

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
