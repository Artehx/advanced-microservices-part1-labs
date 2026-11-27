#!/usr/bin/env bash
set -euo pipefail
ns="${1:?namespace}" ; n="${2:-40}"
[[ "$ns" =~ ^(student(0[1-9]|1[0-9]|2[0-7])|p06|instructor)$ && "$n" =~ ^[0-9]+$ ]] && (( n>=1 && n<=200 )) || { echo "Invalid namespace or sample count: $ns $n" >&2; exit 2; }
ctx="${P06_CONTEXT:?set P06_CONTEXT explicitly}"
[[ "$ctx" == aks-advms26-course || "$ctx" == kind-p06-linkerd || "$ctx" == kind-p06-ci ]] || { echo "Invalid P06_CONTEXT: $ctx" >&2; exit 2; }
kubectl --context "$ctx" -n "$ns" exec deploy/p06-orders -c orders -- sh -c 'i=0; while [ "$i" -lt "$1" ]; do curl --max-time 5 -fsS http://p06-inventory:8080/api/inventory/SKU-001 || exit; printf "\n"; i=$((i+1)); done' sh "$n" | python3 -c 'import json,sys,collections; x=[json.loads(l) for l in sys.stdin if l.strip()]; assert len(x)==int(sys.argv[1]),len(x); print("versions",dict(collections.Counter(i["serviceVersion"] for i in x))); print("replicas",dict(collections.Counter(i["servedBy"] for i in x)))' "$n"
