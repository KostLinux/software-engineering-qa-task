#!/usr/bin/env bash
# Runs on create and during Codespaces prebuilds: install/download everything slow.
set -euo pipefail

# Postman collection runner (needs the Node feature, so it's installed here, not in the Dockerfile)
npm install -g newman

# Download Gradle + dependencies and compile once, so the first `./gradlew run` is fast
./gradlew --no-daemon classes
