#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
c=(docker compose -f docker-compose.yml -f compose.state.yml --profile extended)
"${c[@]}" up -d --wait postgres
# pg_isready may succeed during initial setup; wait for the actual schema via TCP.
for i in $(seq 1 60); do
  if "${c[@]}" exec -T postgres psql -h 127.0.0.1 -U lab -d advms -Atc "SELECT to_regclass('orders.event')" | grep -qx 'orders.event'; then
    "${c[@]}" up -d --build --wait inventory orders api-gateway payments
    exit 0
  fi
  sleep 1
done
echo "P04 schema not ready"; exit 1
