#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
url='http://127.0.0.1:8080/api/orders/preview?sku=SKU-001&quantity=2'
stats='http://127.0.0.1:8080/api/orders/training/resilience'
code() { curl -sS --max-time 12 -o /tmp/p02-ci-response.json -w '%{http_code}' "$url"; }
assert_code() { local actual; actual="$(code)"; [[ "$actual" == "$1" ]] || { cat /tmp/p02-ci-response.json; echo "expected $1 got $actual"; exit 1; }; }
field() { curl -fsS "$stats" | python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"; }
cleanup() { local rc=$?; if [[ "$rc" != 0 ]]; then docker compose logs --tail=100 || true; fi; docker compose --profile extended down >/dev/null || true; return "$rc"; }
trap cleanup EXIT
docker compose --profile extended down
docker compose up -d --wait
assert_code 200
TRAINING_INVENTORY_DELAY_MS=2000 docker compose up -d --wait --force-recreate --no-deps inventory
P02_READ_MS=500 docker compose up -d --wait --force-recreate --no-deps orders
assert_code 502
[[ "$(field attempts)" == 1 ]]
TRAINING_INVENTORY_FAIL=true docker compose up -d --wait --force-recreate --no-deps inventory
P02_RETRY=true docker compose up -d --wait --force-recreate --no-deps orders
assert_code 502
[[ "$(field attempts)" == 3 ]]
P02_CIRCUIT=true P02_OPEN_MS=15000 docker compose up -d --wait --force-recreate --no-deps orders
for i in 1 2 3 4; do assert_code 502; done
[[ "$(field circuitState)" == OPEN ]]
assert_code 503
[[ "$(field attempts)" == 4 ]]
docker compose up -d --wait --force-recreate --no-deps inventory
sleep 16
assert_code 200
assert_code 200
[[ "$(field circuitState)" == CLOSED ]]
TRAINING_INVENTORY_DELAY_MS=2000 docker compose up -d --wait --force-recreate --no-deps inventory
P02_BULKHEAD=true docker compose up -d --wait --force-recreate --no-deps orders
python3 labs/p02-resilience/burst.py --assert-bulkhead
a="$(field attempts)"
[[ "$a" -ge 1 && "$a" -lt 8 ]] || { echo "Unexpected attempts $a"; exit 1; }
echo "P02 integration smoke passed"
