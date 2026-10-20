package com.nova.mobile

import android.app.ActivityManager
import android.content.Context
import android.os.Build

data class DeviceInfo(val model: String, val soc: String, val ramGb: Double, val sdk: Int)

fun readDevice(ctx: Context): DeviceInfo {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    val soc = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else Build.HARDWARE
    return DeviceInfo(Build.MODEL, soc, mi.totalMem / 1073741824.0, Build.VERSION.SDK_INT)
}

fun ramPlanMb(d: DeviceInfo): Int = (d.ramGb * 1024 * 0.33).toInt().coerceIn(1024, 4096)

fun isNote10Plus(d: DeviceInfo) = d.model.startsWith("SM-N975")

/** Steps to try, in order, when 26.x will not start in your launcher. */
fun compatSteps(d: DeviceInfo, ver: String): List<String> {
    val l = mutableListOf<String>()
    if (ver.startsWith("26")) {
        l += "1. Select a Java 25 runtime in your launcher. Minecraft 26.1.x needs Java 25; an older Java is the most common reason 26.x will not start."
        l += "2. If it still fails, switch the renderer (try the Vulkan/Zink option if your launcher has one, then ANGLE, then GL4ES)."
    }
    l += "3. Allocate about ${ramPlanMb(d)} MB. More than that can get the game killed by Android."
    l += "4. Lower render distance to 6-8 and turn off fancy graphics for the first run."
    if (isNote10Plus(d)) l += "5. Note 10+ (Exynos 9825) gets hot under load. Cap FPS at 30-45 if it throttles."
    return l
}
