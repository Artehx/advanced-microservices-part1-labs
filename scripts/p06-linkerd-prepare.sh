#!/usr/bin/env bash
set -euo pipefail
# Execute in Codespaces only; isolated kubeconfig and explicit kind context.
cd "$(dirname "$0")/.."
command -v kind >/dev/null; command -v kubectl >/dev/null; command -v linkerd >/dev/null
export KUBECONFIG="$HOME/.kube/p06-linkerd"
mkdir -p "$HOME/.kube"
if ! kind get clusters | grep -qx p06-linkerd; then
  kind create cluster --name p06-linkerd --image kindest/node:v1.34.0 --kubeconfig "$KUBECONFIG"
fi
[[ "$(kubectl config current-context)" == kind-p06-linkerd ]] || exit 2
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.2.1/standard-install.yaml
linkerd check --pre
linkerd install --crds | kubectl apply -f -
linkerd install | kubectl apply -f -
linkerd check
linkerd viz install | kubectl apply -f -
linkerd viz check
kubectl create namespace p06 --dry-run=client -o yaml | kubectl apply -f -
docker build -f services/inventory/Dockerfile -t p06-inventory:lab .
docker build -f services/orders/Dockerfile -t p06-orders:lab .
kind load docker-image --name p06-linkerd p06-inventory:lab p06-orders:lab
echo "Prepared. Use export KUBECONFIG=$KUBECONFIG and P06_CONTEXT=kind-p06-linkerd."
