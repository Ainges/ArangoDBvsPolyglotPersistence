#!/usr/bin/env bash
FILE="../data/sessions.json"
while IFS= read -r session; do
  user=$(echo "$session" | jq -r '.user')
  last_active=$(echo "$session" | jq -r '.last_active')
  online=$(echo "$session" | jq -r '.online')
  device=$(echo "$session" | jq -r '.device')
  redis-cli HMSET "session:$user" \
    last_active "$last_active" \
    online       "$online" \
    device       "$device"
done < <(jq -c '.[]' "$FILE")