#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
base=http://127.0.0.1:8080/api
id="p04-$(date +%s)-$$"
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
get(){ curl -fsS --max-time 10 "$base/$1"; }
post(){ curl -fsS --max-time 10 -X POST "$base/$1" -H 'Content-Type: application/json' -d "$2"; }
field(){ python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"; }
expect(){ local got="$1" want="$2"; [[ "$got" == "$want" ]] || { echo "expected $want, got $got"; exit 1; }; }
status(){ field status; }
stock(){ get inventory/training/state/stock/SKU-001 | field available; }
create(){ post orders/training/state/commands "{\"orderId\":\"$1\",\"sku\":\"SKU-001\",\"quantity\":2,\"decline\":$2}"; }
advance(){ post "orders/training/state/$1/advance" '{}'; }
query(){ get "orders/training/state/$1/query"; }
project(){ post "orders/training/state/$1/project" '{}'; }
before=$(stock)
[[ "$before" -ge 2 ]] || { echo "Need at least 2 available; reset only P04 sandbox if required"; exit 1; }
expect "$(create "$id-fail" true | status)" NEW
expect "$(create "$id-fail" true | field version)" 1
expect "$(curl -sS -o "$tmp" -w '%{http_code}' "$base/orders/training/state/$id-fail/query")" 404
expect "$(project "$id-fail" | status)" NEW
expect "$(advance "$id-fail" | status)" RESERVED
expect "$(stock)" "$((before-2))"
expect "$(query "$id-fail" | status)" NEW
# Repeating the participant command must not reduce stock a second time.
expect "$(post inventory/training/state/reserve "{\"orderId\":\"$id-fail\",\"sku\":\"SKU-001\",\"quantity\":2}" | status)" RESERVED
expect "$(stock)" "$((before-2))"
expect "$(advance "$id-fail" | status)" PAYMENT_FAILED
expect "$(advance "$id-fail" | status)" CANCELLED
expect "$(stock)" "$before"
expect "$(advance "$id-fail" | field version)" 4
expect "$(post inventory/training/state/release "{\"orderId\":\"$id-fail\",\"sku\":\"SKU-001\",\"quantity\":2}" | status)" RELEASED
expect "$(stock)" "$before"
expect "$(project "$id-fail" | status)" CANCELLED
curl -fsS -X DELETE "$base/orders/training/state/$id-fail/projection"
expect "$(curl -sS -o "$tmp" -w '%{http_code}' "$base/orders/training/state/$id-fail/query")" 404
expect "$(project "$id-fail" | field version)" 4
get "orders/training/state/$id-fail/events" | python3 -c 'import json,sys; assert [x["kind"] for x in json.load(sys.stdin)]==["CREATED","RESERVED","PAYMENT_DECLINED","RELEASED"]'
expect "$(create "$id-ok" false | status)" NEW
expect "$(advance "$id-ok" | status)" RESERVED
expect "$(advance "$id-ok" | status)" PAID
expect "$(advance "$id-ok" | status)" CONFIRMED
expect "$(project "$id-ok" | status)" CONFIRMED
expect "$(stock)" "$((before-2))"
echo "P04 event history, stale read, replay, compensation and success passed"
