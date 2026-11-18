#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
base='http://127.0.0.1:8080'
url="$base/api/orders/estimate"
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
curl -fsS "$url?sku=SKU-001&quantity=2&destination=ES&weightGrams=500" | python3 -c 'import json,sys;x=json.load(sys.stdin);assert x["canBeFulfilled"] is True and x["shipping"]["priceCents"]==499 and x["inventoryAvailable"]==42'
curl -fsS "$url?sku=SKU-001&quantity=43&destination=ES&weightGrams=500" | python3 -c 'import json,sys;x=json.load(sys.stdin);assert x["canBeFulfilled"] is False and x.get("shipping") is None'
for q in 'sku=SKU-001&quantity=0&destination=ES&weightGrams=500' 'sku=SKU-001&quantity=1&destination=es&weightGrams=500'; do
  [[ "$(curl -sS -o "$tmp" -w '%{http_code}' "$url?$q")" == 400 ]]
done
curl -fsS "$base/stock/SKU-001" | python3 -c 'import json,sys;assert json.load(sys.stdin)["sku"]=="SKU-001"'
make verify
echo 'P01 acceptance passed'
