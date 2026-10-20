package com.nova.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF8B5CF6), secondary = Color(0xFF38BDF8),
                background = Color(0xFF0D0B1A), surface = Color(0xFF171329))) { NovaApp() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp() {
    val ctx = LocalContext.current
    val dev = remember { readDevice(ctx) }
    var tab by remember { mutableStateOf(0) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                listOf("▶" to "Play", "🎮" to "Controls", "🤖" to "Mod AI").forEachIndexed { i, t ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Text(t.first) }, label = { Text(t.second) })
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) { 0 -> PlayScreen(dev); 1 -> ControlsScreen(); else -> AdvisorScreen(dev) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(dev: DeviceInfo) {
    val ctx = LocalContext.current
    val versions = listOf("1.21.11", "26.1", "26.1.2", "26.2")
    var ver by remember { mutableStateOf("26.1.2") }
    var pkg by remember { mutableStateOf("com.movtery.zalithlauncher") }
    var ram by remember { mutableStateOf(ramPlanMb(dev).toFloat()) }
    var found by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nova Mobile", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("${dev.model} · ${dev.soc} · ${"%.1f".format(dev.ramGb)} GB RAM · Android API ${dev.sdk}", fontSize = 13.sp)
        Text("Minecraft version")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            versions.forEach { v -> FilterChip(selected = ver == v, onClick = { ver = v }, label = { Text(v) }) }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Settings to enter in your launcher", fontWeight = FontWeight.Bold)
                compatSteps(dev, ver).forEach { Text(it, fontSize = 14.sp) }
            }
        }
        Text("Suggested RAM: ${ram.toInt()} MB")
        Slider(value = ram, onValueChange = { ram = it }, valueRange = 1024f..6144f)
        OutlinedTextField(value = pkg, onValueChange = { pkg = it }, label = { Text("Launcher package name") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = {
            val pm = ctx.packageManager
            found = pm.getInstalledApplications(0).filter { a ->
                val l = pm.getApplicationLabel(a).toString().lowercase()
                listOf("zalith", "pojav", "bridge").any { k -> k in l || k in a.packageName.lowercase() }
            }.map { pm.getApplicationLabel(it).toString() to it.packageName }
        }) { Text("Find installed launchers") }
        found.forEach { f -> TextButton(onClick = { pkg = f.second }) { Text("${f.first}  (${f.second})") } }
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val i = ctx.packageManager.getLaunchIntentForPackage(pkg.trim())
            if (i != null) ctx.startActivity(i)
            else Toast.makeText(ctx, "Launcher not found. Check the package name.", Toast.LENGTH_LONG).show()
        }) { Text("Open launcher") }
        Text("Nova Mobile does not run Minecraft itself. It opens the launcher you installed and shows what to set there.", fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorScreen(dev: DeviceInfo) {
    val msgs = remember { mutableStateListOf("ai" to "Ask me which mods suit your ${dev.model}, or why a version won't start.") }
    var input by remember { mutableStateOf("") }
    fun ask(q: String) { if (q.isBlank()) return; msgs.add("me" to q); msgs.add("ai" to advisorReply(q, dev)); input = "" }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Mod AI", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Offline advisor built from your phone's specs.", fontSize = 12.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Best mods for my phone", "How much RAM?", "26.1 won't launch", "Can I use shaders?", "It gets hot").forEach { q ->
                AssistChip(onClick = { ask(q) }, label = { Text(q) })
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(msgs.toList()) { m ->
                Card(Modifier.fillMaxWidth().padding(start = if (m.first == "me") 40.dp else 0.dp, end = if (m.first == "ai") 40.dp else 0.dp)) {
                    Text(m.second, Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Ask about mods…") })
            Button(onClick = { ask(input) }) { Text("Ask") }
        }
    }
}
