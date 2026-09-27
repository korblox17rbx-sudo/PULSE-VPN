package com.pulse.vpn.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.vpn.ui.theme.*

@Composable
fun SupportScreen() {
    val ctx = LocalContext.current
    fun openTg() {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=Pulse_tex"))) }
            .onFailure { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Pulse_tex"))) } }
    }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Поддержка", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Accent, AccentSoft))).clickable { openTg() },
            contentAlignment = Alignment.Center
        ) { Text("✈  Написать в Telegram @Pulse_tex", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(16.dp))
        listOf(
            "VPN не подключается" to "Проверь интернет, затем смени регион или нажми «Проверить скорость».",
            "Медленная скорость" to "Включи Автовыбор -  он сам подберёт ближайший сервер.",
            "Сервер не работает" to "Напиши в Telegram - заменим сервер и раздалим компенсацию дней."
        ).forEach { (q, a) ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Card).padding(16.dp)) {
                Text(q, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(a, color = TextDim, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
