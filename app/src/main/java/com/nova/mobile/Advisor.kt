package com.nova.mobile

data class Mod(val name: String, val does: String, val boost: Int, val note: String)

val MODS = listOf(
    Mod("Sodium", "Faster rendering", 5, "Needs a renderer with modern OpenGL; may crash on some translation layers."),
    Mod("Lithium", "Faster game logic and ticks", 4, "Safe, no visual changes."),
    Mod("FerriteCore", "Uses less RAM", 4, "Good when RAM is tight."),
    Mod("ModernFix", "Faster startup, less RAM", 4, "Helps long load times."),
    Mod("Entity Culling", "Skips drawing hidden entities", 3, "Helps in farms and busy areas."),
    Mod("ImmediatelyFast", "Faster HUD and text drawing", 3, "Needs newer OpenGL; remove it if the game crashes on start."),
    Mod("More Culling", "Skips drawing hidden blocks", 3, "Small but steady gain."),
    Mod("Dynamic FPS", "Lowers FPS when the game is in the background", 2, "Saves battery."),
    Mod("Sodium Extra", "More video options (FPS cap, particles)", 2, "Needs Sodium.")
)
const val AVOID = "Avoid shader packs, Distant Horizons, and big modpacks on a phone. They need far more GPU power than a mobile chip gives."
const val CHECK = "Mod support for new versions (26.x) depends on each mod's author. Check the Modrinth page for your exact version before downloading."

fun advisorReply(q: String, d: DeviceInfo): String {
    val s = q.lowercase()
    val head = "${d.model} (${d.soc}, ${"%.0f".format(d.ramGb)} GB RAM)"
    fun has(vararg k: String) = k.any { it in s }
    return when {
        has("shader") -> "$head: skip shaders. $AVOID"
        has("ram", "memory", "alloc") ->
            "$head: give the game about ${ramPlanMb(d)} MB. Going higher can make Android close the app, and FerriteCore/ModernFix save more than extra RAM does."
        has("crash", "black", "won't", "wont", "launch", "java", "26") ->
            "Things to try on $head:\n" + compatSteps(d, "26.1").joinToString("\n") + "\n\n$CHECK"
        has("hot", "heat", "battery", "throttle") ->
            "$head can heat up in long sessions. Cap FPS (Sodium Extra), use Dynamic FPS, lower render distance to 6, and keep the screen brightness down."
        has("render", "distance", "chunk") ->
            "For $head start at render distance ${if (d.ramGb >= 10) 8 else 6} and simulation distance 5. Raise one step at a time."
        else -> {
            val top = if (d.ramGb >= 10) MODS.take(7) else if (d.ramGb >= 6) MODS.take(5) else MODS.take(3)
            "Best picks for $head:\n" + top.joinToString("\n") { "- ${it.name}: ${it.does}. ${it.note}" } + "\n\n$AVOID\n\n$CHECK"
        }
    }
}
