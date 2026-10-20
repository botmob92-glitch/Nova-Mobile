package com.nova.mobile

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Ctl(val id: String, val x: Float, val y: Float, val size: Float = 52f, val alpha: Float = 0.85f)
enum class Scope(val label: String) { SELECTED("Selected only"), OTHERS("All except selected"), ALL("Everyone") }

fun defaultCtls() = listOf(
    Ctl("W", 70f, 150f), Ctl("A", 14f, 206f), Ctl("S", 70f, 206f), Ctl("D", 126f, 206f),
    Ctl("Jump", 270f, 206f), Ctl("Sneak", 270f, 150f), Ctl("Attack", 200f, 256f),
    Ctl("Use", 270f, 262f), Ctl("Inv", 120f, 8f), Ctl("Chat", 190f, 8f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlsScreen() {
    val dens = LocalDensity.current.density
    var ctls by remember { mutableStateOf(defaultCtls()) }
    var sel by remember { mutableStateOf(setOf<String>()) }
    var selectMode by remember { mutableStateOf(true) }
    var scope by remember { mutableStateOf(Scope.SELECTED) }

    fun targets(): Set<String> {
        val all = ctls.map { it.id }.toSet()
        return when (scope) { Scope.SELECTED -> sel; Scope.OTHERS -> all - sel; Scope.ALL -> all }
    }
    fun edit(f: (Ctl) -> Ctl) { val t = targets(); ctls = ctls.map { if (it.id in t) f(it) else it } }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Control layout", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text("Tap buttons to select several. Drag one selected button to move the whole selection.", fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectMode, onClick = { selectMode = !selectMode }, label = { Text("Select mode") })
            AssistChip(onClick = { sel = ctls.map { it.id }.toSet() }, label = { Text("All") })
            AssistChip(onClick = { sel = emptySet() }, label = { Text("None") })
            Text("${sel.size} selected", Modifier.align(Alignment.CenterVertically), fontSize = 13.sp)
        }
        Text("Apply changes to:", fontSize = 13.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Scope.values().forEach { s -> FilterChip(selected = scope == s, onClick = { scope = s }, label = { Text(s.label) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { edit { it.copy(size = (it.size - 6f).coerceIn(32f, 120f)) } }) { Text("Size -") }
            OutlinedButton(onClick = { edit { it.copy(size = (it.size + 6f).coerceIn(32f, 120f)) } }) { Text("Size +") }
            OutlinedButton(onClick = { edit { it.copy(alpha = (it.alpha - .1f).coerceIn(.2f, 1f)) } }) { Text("Fade") }
            OutlinedButton(onClick = { edit { it.copy(alpha = (it.alpha + .1f).coerceIn(.2f, 1f)) } }) { Text("Solid") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { edit { it.copy(x = it.x - 8f) } }) { Text("←") }
            OutlinedButton(onClick = { edit { it.copy(y = it.y - 8f) } }) { Text("↑") }
            OutlinedButton(onClick = { edit { it.copy(y = it.y + 8f) } }) { Text("↓") }
            OutlinedButton(onClick = { edit { it.copy(x = it.x + 8f) } }) { Text("→") }
            OutlinedButton(onClick = { ctls = defaultCtls(); sel = emptySet() }) { Text("Reset") }
        }
        Box(Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF1B1630))) {
            ctls.forEach { c ->
                key(c.id) {
                    val picked = c.id in sel
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        Modifier.offset(c.x.dp, c.y.dp).size(c.size.dp).alpha(c.alpha).clip(shape)
                            .background(Color(0xFF8B5CF6))
                            .border(if (picked) 3.dp else 1.dp, if (picked) Color(0xFF38BDF8) else Color.White.copy(alpha = .4f), shape)
                            .pointerInput(c.id) {
                                detectTapGestures(onTap = { if (selectMode) sel = if (c.id in sel) sel - c.id else sel + c.id })
                            }
                            .pointerInput(c.id) {
                                detectDragGestures { change, d ->
                                    change.consume()
                                    val g = if (c.id in sel) sel else setOf(c.id)
                                    ctls = ctls.map { if (it.id in g) it.copy(x = it.x + d.x / dens, y = it.y + d.y / dens) else it }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) { Text(c.id, color = Color.White, fontSize = 13.sp) }
                }
            }
        }
    }
}
