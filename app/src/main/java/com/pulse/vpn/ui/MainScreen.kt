package com.pulse.vpn.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.vpn.AppVm
import com.pulse.vpn.data.AccountState
import com.pulse.vpn.data.Server
import com.pulse.vpn.data.Servers
import com.pulse.vpn.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun dmy(ms: Long): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ms))
fun daysLeft(trialStart: Long, now: Long): Int = (((trialStart + 30 * AppVm.DAY) - now) / AppVm.DAY).toInt().coerceIn(0, 30)
fun gb(bytes: Long): String = "%.1f ГБ".format(bytes / 1e9)
fun fmtTime(sec: Int): String = "%02d:%02d:%02d".format(sec / 3600, sec / 60 % 60, sec % 60)

@Composable
fun MainScreen(vm: AppVm, state: AccountState, up: Boolean, onToggle: () -> Unit, onServers: () -> Unit, onAdmin: () -> Unit) {
    val latency by vm.latency.collectAsStateWithLifecycle()
    val premium = System.currentTimeMillis() < state.premiumUntil
    val secs by produceState(0, up) { if (up) while (true) { delay(1000); value++ } }
    var traffic by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(up) { while (up) { traffic = vm.traffic(); delay(3000) } }

    val selected = Servers.byId(state.selectedServerId) ?: Servers.fastest(latency)
    val effMs = if (state.selectedServerId == "auto") Servers.fastestMs(latency) else (latency[selected.id] ?: -1)

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        TopBar(onAdmin)
        Spacer(Modifier.height(16.dp))
        StatusChip(up)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { OrbButton(up, onToggle) }
        if (up) {
            Text(fmtTime(secs), color = TextMain, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center))
            traffic?.let { Text("↕ ${gb(it)}", color = TextDim, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center)) }
        }
        Spacer(Modifier.height(18.dp))
        ServerCard(selected, effMs, onServers)
        Spacer(Modifier.height(12.dp))
        SubscriptionCard(state, premium)
        Spacer(Modifier.height(12.dp))
        PremiumButton { vm.openPaywall() }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun TopBar(onAdmin: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("PULSE VPN", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(CardHi).clickable(onClick = onAdmin)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { Text("⬇ Управление", color = TextMain, fontSize = 13.sp) }
    }
}

@Composable
fun StatusChip(up: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            Modifier.clip(RoundedCornerShape(12.dp)).background(Card).padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (up) { Text("✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(Modifier.width(6.dp)) }
            Text(if (up) "VPN включен" else "VPN отключен", color = TextMain, fontSize = 15.sp)
        }
    }
}

@Composable
fun OrbButton(on: Boolean, onTap: () -> Unit) {
    val p by animateFloatAsState(if (on) 1f else 0f, animationSpec = tween(700), label = "p")
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "pulseV"
    )
    Box(Modifier.size(240.dp).clickable { onTap() }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(240.dp)) {
            val c = center
            val r = size.minDimension / 2f
            drawCircle(Color(0xFF15161B), r * 0.98f, c)
            drawCircle(Color(0xFF22242C), r * 0.86f, c, style = Stroke(1.5.dp.toPx()))
            if (on) for (i in 0..2) {
                val t = (pulse + i / 3f) % 1f
                drawCircle(Accent.copy(alpha = (1f - t) * 0.30f), r * (0.72f + 0.30f * t), c, style = Stroke(2.dp.toPx()))
            }
        }
        Canvas(Modifier.size(150.dp)) {
            val c = center
            val w = size.width
            if (p < 0.5f) {
                drawArc(
                    Color.White, 175f, 170f, false,
                    topLeft = Offset(c.x - w * 0.30f, c.y - w * 0.06f),
                    size = Size(w * 0.60f, w * 0.26f),
                    style = Stroke(width = w * 0.030f, cap = StrokeCap.Round)
                )
            } else {
                val a = ((p - 0.5f) * 2f).coerceIn(0f, 1f)
                val eye = Path().apply {
                    moveTo(c.x - w * 0.44f, c.y)
                    cubicTo(c.x - w * 0.20f, c.y - w * 0.34f, c.x + w * 0.20f, c.y - w * 0.34f, c.x + w * 0.44f, c.y)
                    cubicTo(c.x + w * 0.20f, c.y + w * 0.34f, c.x - w * 0.20f, c.y + w * 0.34f, c.x - w * 0.44f, c.y)
                    close()
                }
                drawPath(eye, Color.White.copy(alpha = a))
                drawCircle(Accent, w * 0.16f, c)
                drawCircle(Color.White.copy(alpha = a), w * 0.16f, c, style = Stroke(w * 0.018f))
                drawLine(Color.White.copy(alpha = a), Offset(c.x - w * 0.16f, c.y), Offset(c.x + w * 0.16f, c.y), w * 0.018f)
                drawArc(Color.White.copy(alpha = a), 0f, 360f, false,
                    topLeft = Offset(c.x - w * 0.055f, c.y - w * 0.16f),
                    size = Size(w * 0.11f, w * 0.32f), style = Stroke(w * 0.018f))
            }
        }
    }
}

@Composable
fun ServerCard(s: Server, ms: Int, onOpen: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).clickable(onClick = onOpen).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(AccentSoft), contentAlignment = Alignment.Center) {
            Text(s.flag, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (s.id == "auto") "Автовыбор - самый быстрый" else s.name, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(if (s.id == "auto") "Регион подбирается автоматически" else "Регион: ${s.name}", color = TextDim, fontSize = 12.sp)
        }
        if (ms in 1..9000) { Bars(ms); Spacer(Modifier.width(6.dp)); Text("$ms мс", color = Green, fontSize = 13.sp) }
        Spacer(Modifier.width(8.dp))
        Text("›", color = TextDim, fontSize = 22.sp)
    }
}

@Composable
fun SubscriptionCard(state: AccountState, premium: Boolean) {
    val now = System.currentTimeMillis()
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.clip(RoundedCornerShape(50)).background(CardHi).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(if (premium) "Премиум" else "Пробный тариф", color = TextMain, fontSize = 13.sp)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (premium) "Активен" else "${daysLeft(state.trialStart, now)} дн.",
                color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
            Text(
                "до " + dmy(if (premium) state.premiumUntil else state.trialStart + 30 * AppVm.DAY),
                color = TextDim, fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PremiumButton(onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Accent, Color(0xFF2B55D8)))).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text("👑  Улучшить до Премиум", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun Bars(ms: Int?) {
    val col = when {
        ms == null || ms > 9000 -> TextDim
        ms < 150 -> Green
        ms < 450 -> Color(0xFFE0B13E)
        else -> Color(0xFFE5533C)
    }
    Row(verticalAlignment = Alignment.Bottom) {
        listOf(9f, 14f, 19f).forEach { hh ->
            Box(Modifier.padding(horizontal = 1.dp).size(4.dp, hh.dp).clip(RoundedCornerShape(2.dp)).background(col))
        }
    }
}

@Composable
fun BottomNav(current: String, onGo: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Bg).navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NavItem("🌐", "VPN", current == "main", Modifier.weight(1f)) { onGo("main") }
        NavItem("⚙️", "Настройки", current == "settings", Modifier.weight(1f)) { onGo("settings") }
        NavItem("🎧", "Поддержка", current == "support", Modifier.weight(1f)) { onGo("support") }
    }
}

@Composable
fun NavItem(icon: String, label: String, active: Boolean, mod: Modifier, onClick: () -> Unit) {
    Box(
        mod.clip(RoundedCornerShape(18.dp)).background(if (active) CardHi else Color.Transparent)
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 18.sp, color = if (active) Accent else TextDim)
            Text(label, fontSize = 12.sp, color = if (active) Accent else TextDim)
        }
    }
}
