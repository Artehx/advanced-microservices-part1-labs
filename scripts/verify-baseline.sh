#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
for port in 8080 8081 8082; do
  curl -fsS --max-time 10 "http://127.0.0.1:$port/actuator/health" | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"]=="UP"'
done
curl -fsS --max-time 10 'http://127.0.0.1:8080/api/orders/preview?sku=SKU-001&quantity=2' | python3 -c 'import json,sys; x=json.load(sys.stdin); print(json.dumps(x,indent=2)); assert x["canBeFulfilled"] is True'
curl -fsS --max-time 10 'http://127.0.0.1:8080/api/inventory/SKU-001' | python3 -c 'import json,sys; assert json.load(sys.stdin)["sku"]=="SKU-001"'
echo "Baseline OK"
