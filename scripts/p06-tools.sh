#!/usr/bin/env bash
set -euo pipefail
# CLI installation ONLY inside the Codespace. No Azure login or cluster modification.
[[ "${CODESPACES:-}" == true ]] || { echo "Run inside GitHub Codespaces"; exit 2; }
[[ "$(uname -m)" == x86_64 ]] || { echo "Use an amd64 Codespace"; exit 2; }
command -v az >/dev/null || { echo "Rebuild devcontainer for P06 (Azure CLI feature)"; exit 2; }
mkdir -p "$HOME/.local/bin"
# Instructor must confirm kubectl minor compatibility with AKS and kind v1.34.0.
: "${P06_KUBECTL_VERSION:?instructor supplies compatible version, e.g. v1.34.0}"
az aks install-cli --client-version "$P06_KUBECTL_VERSION" --install-location "$HOME/.local/bin/kubectl" --kubelogin-install-location "$HOME/.local/bin/kubelogin"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
curl --proto '=https' --tlsv1.2 -fsSL https://kind.sigs.k8s.io/dl/v0.30.0/kind-linux-amd64 -o "$tmp/kind"
install -m 0755 "$tmp/kind" "$HOME/.local/bin/kind"
export LINKERD2_VERSION=edge-26.10.1
curl --proto '=https' --tlsv1.2 -sSfL https://run.linkerd.io/install-edge -o "$tmp/install-linkerd.sh"
sh "$tmp/install-linkerd.sh"
export PATH="$HOME/.local/bin:$HOME/.linkerd2/bin:$PATH"
kubectl version --client
kubelogin --version
kind version
linkerd version --client
echo 'CLI ready. In your shell: export PATH="$HOME/.local/bin:$HOME/.linkerd2/bin:$PATH"'
