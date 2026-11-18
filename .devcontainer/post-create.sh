#!/usr/bin/env bash
set -euo pipefail

echo "Tool versions:"
java -version 2>&1 | head -1
kotlin -version 2>&1 | tail -1 || true
k6 version
hurl --version | head -1
curl --version | head -1
wget --version | head -1
http --version
newman --version
claude --version || true
echo
echo "Start the app with: ./gradlew run   (http://localhost:8080/ping)"
