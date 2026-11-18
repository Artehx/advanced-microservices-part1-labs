FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Tools used by preflight, JSON/HTTP verification and P05 HMAC.
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 openssl curl ca-certificates make git \
    && rm -rf /var/lib/apt/lists/*
