#!/bin/sh
# Khannum Gradle launcher. Downloads Gradle 8.9 on first run if needed.
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST="$HOME/.gradle/wrapper/dists/gradle-8.9-bin/khannum/gradle-8.9/bin/gradle"
if [ ! -x "$DIST" ]; then
  CACHE="$HOME/.gradle/wrapper/dists/gradle-8.9-bin/khannum"
  ZIP="$CACHE/gradle-8.9-bin.zip"
  mkdir -p "$CACHE"
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 -o "$ZIP" "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
  else
    echo "curl or wget is required to download Gradle 8.9." >&2
    exit 1
  fi
  rm -rf "$CACHE/gradle-8.9"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$ZIP" -d "$CACHE"
  else
    echo "unzip is required to extract Gradle 8.9." >&2
    exit 1
  fi
fi
exec "$DIST" "$@"
