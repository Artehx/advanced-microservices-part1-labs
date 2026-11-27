#!/usr/bin/env bash
set -euo pipefail
# CLI installation ONLY inside the Codespace. No Azure login or cluster modification.
[[ "${CODESPACES:-}" == true ]] || { echo "Run inside GitHub Codespaces"; exit 2; }
[[ "$(uname -m)" == x86_64 ]] || { echo "Use an amd64 Codespace"; exit 2; }
mkdir -p "$HOME/.local/bin"
# Instructor must confirm kubectl minor compatibility with AKS and kind v1.34.0.
: "${P06_KUBECTL_VERSION:?instructor supplies compatible version, e.g. v1.35.0}"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
# Existing P00-P05 Codespaces do not need a devcontainer rebuild.
if ! command -v az >/dev/null; then
  command -v sudo >/dev/null || { echo "Azure CLI missing and sudo unavailable; contact instructor"; exit 2; }
  curl --proto '=https' --tlsv1.2 -fsSL 'https://azurecliprod.blob.core.windows.net/$root/deb_install.sh' -o "$tmp/install-azure-cli.sh"
  sudo bash "$tmp/install-azure-cli.sh"
fi
az aks install-cli --client-version "$P06_KUBECTL_VERSION" --install-location "$HOME/.local/bin/kubectl" --kubelogin-install-location "$HOME/.local/bin/kubelogin"
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
