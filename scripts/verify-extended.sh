#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/verify-baseline.sh
for port in 8083 8084 8085; do
  curl -fsS --max-time 10 "http://127.0.0.1:$port/actuator/health" | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"]=="UP"'
done
curl -fsS --max-time 10 'http://127.0.0.1:8080/api/payments/methods' | python3 -c 'import json,sys; x=json.load(sys.stdin); assert x["chargingEnabled"] is False'
curl -fsS --max-time 10 'http://127.0.0.1:8080/api/shipping/quote?destination=ES&weightGrams=500' | python3 -c 'import json,sys; x=json.load(sys.stdin); assert x["priceCents"]==499'
curl -fsS --max-time 10 'http://127.0.0.1:8080/api/notifications/provider' | python3 -c 'import json,sys; x=json.load(sys.stdin); assert x["mode"]=="simulated" and x["deliveryEnabled"] is False and x["credentialConfigured"] is False'
echo "Extended contracts OK"
