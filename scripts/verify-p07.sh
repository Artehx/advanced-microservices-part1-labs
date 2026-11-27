#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
base=http://127.0.0.1:8080/api
id="incident-$(date +%s)-$$"
get(){ curl -fsS --max-time 10 "$base/$1"; }
post(){ curl -fsS --max-time 10 -X POST "$base/$1" -H 'Content-Type: application/json' -d "$2"; }
field(){ python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"; }
eq(){ [[ "$1" == "$2" ]] || { echo "Unexpected $1 expected $2"; exit 1; }; }
stock(){ get inventory/training/state/stock/SKU-001 | field available; }
create(){ post orders/training/state/commands "{\"orderId\":\"$1\",\"sku\":\"SKU-001\",\"quantity\":2,\"decline\":$2}"; }
advance(){ post "orders/training/state/$1/advance" '{}'; }
before=$(stock)
(( before >= 4 )) || { echo "Need at least 4 units, use fresh sandbox"; exit 2; }
eq "$(create "$id-fail" true | field status)" NEW
eq "$(advance "$id-fail" | field status)" RESERVED
eq "$(stock)" "$((before-2))"
eq "$(advance "$id-fail" | field status)" PAYMENT_FAILED
eq "$(advance "$id-fail" | field status)" CANCELLED
after=$(stock)
echo "incident=$id-fail stock_before=$before stock_after_cancel=$after"
get "orders/training/state/$id-fail/events"; echo
if [[ "$after" != "$before" ]]; then
  echo "FAIL INCIDENT: cancellation did not restore available stock"
  exit 42
fi
eq "$(advance "$id-fail" | field version)" 4
eq "$(stock)" "$before"
eq "$(create "$id-ok" false | field status)" NEW
eq "$(advance "$id-ok" | field status)" RESERVED
eq "$(advance "$id-ok" | field status)" PAID
eq "$(advance "$id-ok" | field status)" CONFIRMED
eq "$(stock)" "$((before-2))"
echo "PASS incident regression and successful order"
