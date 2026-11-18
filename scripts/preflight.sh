#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
for cmd in java mvn docker curl python3 make openssl git; do
  command -v "$cmd" >/dev/null || { echo "Falta $cmd"; exit 1; }
done
java -version
mvn -version
python3 --version
openssl version
docker info >/dev/null
docker compose version
docker compose config -q
echo "Preflight OK: entorno local; no se utiliza Azure."
