#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
c=(docker compose -f docker-compose.yml -f compose.integrations.yml --profile extended)
base=http://127.0.0.1:8080/api
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
id="p05-$(date +%s)-$$"
payload="{\"eventId\":\"$id\",\"type\":\"shipping.delivered\",\"orderId\":\"order-demo\"}"
post(){ curl -fsS --max-time 10 -X POST "$base/notifications/training/webhooks/delivery" -H 'Content-Type: application/json' -d "$payload"; }
expect(){ [[ "$1" == "$2" ]] || { echo "expected $2 got $1"; cat "$tmp"; exit 1; }; }
field(){ python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"; }
post | python3 -c 'import json,sys; x=json.load(sys.stdin); assert x["received"]==1 and x["effects"]==1'
post | python3 -c "import json,sys; x=json.load(sys.stdin); assert x['received']==2 and x['effects']==1"
curl -fsS "$base/shipping/quote?destination=ES&weightGrams=500" | python3 -c 'import json,sys; assert json.load(sys.stdin)["priceCents"]==499'
curl -fsS "$base/orders/training/grpc/quote?destination=ES&weightGrams=500&deadlineMs=3000" | python3 -c 'import json,sys; x=json.load(sys.stdin); assert x["priceCents"]==499 and x["grpcCode"]=="OK"'
expect "$(curl -sS -o "$tmp" -w '%{http_code}' "$base/orders/training/grpc/quote?weightGrams=-1&deadlineMs=3000")" 400
expect "$(field grpcCode < "$tmp")" INVALID_ARGUMENT
P05_GRPC_DELAY_MS=1500 "${c[@]}" up -d --wait --force-recreate --no-deps shipping
expect "$(curl -sS --max-time 10 -o "$tmp" -w '%{http_code}' "$base/orders/training/grpc/quote?deadlineMs=300")" 504
expect "$(field grpcCode < "$tmp")" DEADLINE_EXCEEDED
"${c[@]}" up -d --wait --force-recreate --no-deps shipping
# Reconnect may need a brief moment after recreation.
for i in $(seq 1 15); do if curl -fsS "$base/orders/training/grpc/quote?deadlineMs=3000" > "$tmp"; then break; fi; sleep 1; done
expect "$(field grpcCode < "$tmp")" OK
P05_WEBHOOK_SECRET=lab-signature-only "${c[@]}" up -d --wait --force-recreate --no-deps notifications
expect "$(curl -sS -o "$tmp" -w '%{http_code}' -X POST "$base/notifications/training/webhooks/delivery" -H 'Content-Type: application/json' -d "$payload")" 401
sig=$(printf '%s' "$payload" | openssl dgst -sha256 -hmac lab-signature-only | awk '{print $NF}')
curl -fsS -X POST "$base/notifications/training/webhooks/delivery" -H 'Content-Type: application/json' -H "X-Lab-Signature: $sig" -d "$payload" | python3 -c 'import json,sys; assert json.load(sys.stdin)["effects"]==1'
"${c[@]}" up -d --wait --force-recreate --no-deps notifications
echo "P05 webhook, gRPC quote, deadline, recovery and HMAC passed"
