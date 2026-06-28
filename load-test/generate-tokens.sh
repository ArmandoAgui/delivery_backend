#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://32.199.155.252.sslip.io}"
COUNT="${COUNT:-1000}"
PARALLEL="${PARALLEL:-10}"
PASSWORD="${LOAD_TEST_PASSWORD:-Password123!}"
OUTPUT="${OUTPUT:-load-test/tokens.json}"
TEMP_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

export BASE_URL PASSWORD TEMP_DIR

seq 1 "$COUNT" | xargs -P "$PARALLEL" -I '{}' bash -c '
  number="$1"
  padded="$(printf "%04d" "$number")"
  response="$(curl --fail-with-body --silent --show-error \
    --connect-timeout 10 \
    --max-time 30 \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    --data "{\"email\":\"loadtest-$padded@example.com\",\"password\":\"$PASSWORD\"}")"
  access_token="$(jq -er ".accessToken" <<<"$response")"
  address_id="$(printf "20000000-0000-7000-8000-%012d" "$number")"
  jq -n \
    --argjson index "$number" \
    --arg accessToken "$access_token" \
    --arg deliveryAddressId "$address_id" \
    "{index: \$index, accessToken: \$accessToken, deliveryAddressId: \$deliveryAddressId}" \
    > "$TEMP_DIR/$padded.json"
' _ '{}'

mkdir -p "$(dirname "$OUTPUT")"
jq -s "sort_by(.index)" "$TEMP_DIR"/*.json > "$OUTPUT"
chmod 600 "$OUTPUT"
printf 'Generated %s temporary load-test tokens in %s\n' "$COUNT" "$OUTPUT"
