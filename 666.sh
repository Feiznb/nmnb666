
/system/bin/stty -echo 2>/dev/null

# Sensor paths
CPU=/sys/class/thermal/thermal_zone10/temp
GPU=/sys/class/thermal/thermal_zone12/temp
BAT=/sys/class/power_supply/battery/temp
CUR=/sys/class/power_supply/battery/current_now
VOL=/sys/class/power_supply/battery/voltage_now

read_temp() {
 raw=$(/system/bin/cat $1 2>/dev/null)
 [ -z "$raw" ] && raw=0
    div=$2
    int=$((raw / div))
    dec=$((raw % div / (div/10)))
    [ $dec -lt 10 ] && dec=0$dec
    echo $int.$dec
}

read_power() {
  curr=$(/system/bin/cat $CUR 2>/dev/null)
    volt=$(/system/bin/cat $VOL 2>/dev/null)
    [ -z "$curr" ] && curr=0
    [ -z "$volt" ] && volt=0
    if [ $curr -lt 0 ]; then
        echo 0.0
        return
    fi
    pow=$((curr * volt / 100000000000))
    int=$((pow / 10))
    dec=$((pow % 10))
    echo $int.$dec
}

# Show grid (ASCII only)
show_grid() {
    /system/bin/clear 2>/dev/null
    /system/bin/echo "                         +------------+  +------------+"
    /system/bin/echo "                         | CPU:$1 C |  GPU:$2 C |"
    /system/bin/echo "                         +------------+  +------------+"
    /system/bin/echo "                         +------------+  +------------+"
    /system/bin/echo "                         | BAT:$3 C |  POW:$4 W |"
    /system/bin/echo "                         +------------+  +------------+"
}

while :; do
    CPUT=$(read_temp $CPU 1000)
    GPUT=$(read_temp $GPU 1000)
    BATT=$(read_temp $BAT 10)
    POWR=$(read_power)
    show_grid "$CPUT" "$GPUT" "$BATT" "$POWR"
    /system/bin/sleep 0.1
done

# Exit
trap '/system/bin/stty echo 2>/dev/null; /system/bin/clear; exit 0' INT TERM
