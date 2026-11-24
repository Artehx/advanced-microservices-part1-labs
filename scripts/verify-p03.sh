#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
c=(docker compose -f docker-compose.yml -f compose.kafka.yml --profile extended)
id="ci-$(date +%s)-$$"
publish() {
  curl -fsS --max-time 20 -X POST http://127.0.0.1:8080/api/orders/training/events -H 'Content-Type: application/json' -d "{\"eventId\":\"$1\",\"orderId\":\"order-ci\",\"sku\":\"SKU-001\",\"quantity\":2}"
}
wait_counts() {
  local event="$1" received="$2" effects="$3"
  for i in $(seq 1 45); do
    if curl -fsS --max-time 3 "http://127.0.0.1:8080/api/notifications/training/events/$event" | python3 -c "import json,sys; x=json.load(sys.stdin); assert x['received']==$received and x['effects']==$effects" 2>/dev/null; then return 0; fi
    sleep 1
  done
  echo "Counts did not converge for $event"; "${c[@]}" logs --tail=100 orders notifications kafka; return 1
}
first="$(publish "$id")"
printf '%s\n' "$first"
wait_counts "$id" 1 1
second="$(publish "$id")"
printf '%s\n' "$second"
python3 - "$first" "$second" <<'PY'
import json,sys
a,b=map(json.loads,sys.argv[1:])
assert a["status"] == b["status"] == "BROKER_ACKNOWLEDGED", (a,b)
assert a["eventId"] == b["eventId"], (a,b)
assert a["topic"] == b["topic"] == "orders.created.v1", (a,b)
assert a["partition"] == b["partition"] and a["offset"] < b["offset"], (a,b)
PY
# Final acceptance: two records, two deliveries, one effect.
wait_counts "$id" 2 1
"${c[@]}" stop notifications
publish "$id-offline"
"${c[@]}" up -d --wait notifications
wait_counts "$id-offline" 1 1
echo "P03 broker, duplicate and offline recovery smoke passed"
