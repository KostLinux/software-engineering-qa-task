# JDK 21 is required: Gradle 8.14 can't run on newer JDKs (see README).
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

ARG K6_VERSION=2.4.0
ARG HURL_VERSION=8.1.0
ARG KOTLIN_VERSION=2.0.21

# General QA / HTTP / networking tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends \
        curl wget jq httpie \
        apache2-utils \
        netcat-openbsd dnsutils iputils-ping iproute2 lsof \
        procps htop less vim \
        python3 python3-pip python3-venv pipx \
        unzip zip ca-certificates gnupg \
    && rm -rf /var/lib/apt/lists/*

# k6 (load testing) and hurl (HTTP test runner), pinned, multi-arch
RUN set -eux; \
    arch="$(dpkg --print-architecture)"; \
    curl -fsSL "https://github.com/grafana/k6/releases/download/v${K6_VERSION}/k6-v${K6_VERSION}-linux-${arch}.tar.gz" \
        | tar -xz -C /tmp; \
    install -m 0755 "/tmp/k6-v${K6_VERSION}-linux-${arch}/k6" /usr/local/bin/k6; \
    rm -rf /tmp/k6-*; \
    curl -fsSL -o /tmp/hurl.deb "https://github.com/Orange-OpenSource/hurl/releases/download/${HURL_VERSION}/hurl_${HURL_VERSION}_${arch}.deb"; \
    apt-get update; \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends /tmp/hurl.deb; \
    rm -rf /tmp/hurl.deb /var/lib/apt/lists/*

# Kotlin compiler (matches the Kotlin Gradle plugin version) via the image's SDKMAN
RUN su vscode -c ". /usr/local/sdkman/bin/sdkman-init.sh && sdk install kotlin ${KOTLIN_VERSION} && sdk flush archives && sdk flush temp"

# Pre-create mount points so named volumes are owned by the vscode user
RUN mkdir -p /home/vscode/.claude /home/vscode/.gradle \
    && chown -R vscode:vscode /home/vscode/.claude /home/vscode/.gradle
