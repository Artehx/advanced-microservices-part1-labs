#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${INVENTORY_IMAGE:?set INVENTORY_IMAGE}" "${ORDERS_IMAGE:?set ORDERS_IMAGE}"
for img in "$INVENTORY_IMAGE" "$ORDERS_IMAGE"; do [[ "$img" =~ ^[a-zA-Z0-9./:_@-]+$ ]] || exit 2; done
sed -e "s|__INVENTORY_IMAGE__|$INVENTORY_IMAGE|g" -e "s|__ORDERS_IMAGE__|$ORDERS_IMAGE|g" labs/p06-mesh/workloads.yaml
