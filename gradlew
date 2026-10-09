#!/usr/bin/env bash
set -eu

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
    exec "$DIR/gradlew" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
    echo "Gradle wrapper JAR missing; using system Gradle fallback..."
    exec gradle "$@"
fi

echo "Gradle is not installed and wrapper JAR is missing."
echo "Please run: gradle wrapper --gradle-version=8.5"
echo "or install Gradle first."
exit 1
