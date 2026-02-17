#!/system/bin/sh
sleep 15

echo "========================================"
echo "  一加平板Pro 120帧循环锁帧脚本 启动中"
echo "========================================"

# 基础性能配置
POLICY0_FREQ="2265000"
POLICY2_FREQ="3148000"
POLICY5_FREQ="2956000"
POLICY7_FREQ="3302000"
MIN_GPU_CLOCK_MHZ="915"

POLICY0_MAX_FREQ="/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq"
POLICY0_MIN_FREQ="/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq"
POLICY2_MAX_FREQ="/sys/devices/system/cpu/cpufreq/policy2/scaling_max_freq"
POLICY2_MIN_FREQ="/sys/devices/system/cpu/cpufreq/policy2/scaling_min_freq"
POLICY5_MAX_FREQ="/sys/devices/system/cpu/cpufreq/policy5/scaling_max_freq"
POLICY5_MIN_FREQ="/sys/devices/system/cpu/cpufreq/policy5/scaling_min_freq"
POLICY7_MAX_FREQ="/sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq"
POLICY7_MIN_FREQ="/sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq"
MIN_CLOCK_PATH="/sys/class/kgsl/kgsl-3d0/min_clock_mhz"

echo "[1] 设置CPU频率权限..."
chmod 666 $POLICY0_MAX_FREQ $POLICY0_MIN_FREQ
chmod 666 $POLICY2_MAX_FREQ $POLICY2_MIN_FREQ
chmod 666 $POLICY5_MAX_FREQ $POLICY5_MAX_FREQ
chmod 666 $POLICY7_MAX_FREQ $POLICY7_MIN_FREQ
chmod 666 $MIN_CLOCK_PATH

echo "[2] 锁定CPU频率..."
echo "$POLICY0_FREQ" > $POLICY0_MAX_FREQ
echo "$POLICY0_FREQ" > $POLICY0_MIN_FREQ
echo "$POLICY2_FREQ" > $POLICY2_MAX_FREQ
echo "$POLICY2_FREQ" > $POLICY2_MIN_FREQ
echo "$POLICY5_FREQ" > $POLICY5_MAX_FREQ
echo "$POLICY5_FREQ" > $POLICY5_MIN_FREQ
echo "$POLICY7_FREQ" > $POLICY7_MAX_FREQ
echo "$POLICY7_FREQ" > $POLICY7_MIN_FREQ

echo "[3] 锁定GPU最小频率..."
echo "$MIN_GPU_CLOCK_MHZ" > $MIN_CLOCK_PATH

# 性能函数
hons() {
    local content="$1"
    local file_path="$2"
    chmod 666 "$file_path" >/dev/null 2>&1
    echo "$content" > "$file_path" 2>&1
    chmod 444 "$file_path" >/dev/null 2>&1
}

echo "[4] 关闭系统CPU频率限制..."
hons "1" "/proc/game_opt/disable_cpufreq_limit"

echo "[5] 设置CPU调度为 performance..."
for file in /sys/bus/cpu/devices/*/*/scaling_governor; do
    hons "performance" "$file"
done

echo ""
echo "========================================"
echo "  开始循环强制锁定 120 帧（每2秒一次）"
echo "  帧率低于120会自动拉回"
echo "========================================"
echo ""

# 循环锁帧
while true; do
    echo "[锁定中] 强制保持 120 帧..."
    service call SurfaceFlinger 1035 i32 1
    service call SurfaceFlinger 1036 i32 120

    setprop debug.sf.max_frame_rate 120
    setprop debug.sf.frame_rate 120
    setprop debug.sf.preferred_frame_rate 120
    setprop debug.sf.override_frame_rate 120

    setprop persist.sys.max_refresh_rate 120
    setprop persist.sys.ui.frame_rate 120
    setprop persist.sys.game.frame_rate 120

    setprop vendor.display.disable_dynamic_fps 1
    setprop vendor.display.disable_idle_frame_rate 1

    sleep 2
done
