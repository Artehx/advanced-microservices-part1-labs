#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
c=(docker compose -f docker-compose.yml -f compose.kafka.yml --profile extended)
# The official image runs as appuser UID/GID 1000; initialize only the dedicated data volume.
"${c[@]}" run --rm --no-deps --user 0:0 --entrypoint /bin/sh kafka -c 'chown -R 1000:1000 /tmp/kraft-combined-logs'
"${c[@]}" up -d --wait kafka
"${c[@]}" exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists --topic orders.created.v1 --partitions 3 --replication-factor 1
"${c[@]}" up -d --build --wait inventory orders api-gateway notifications
