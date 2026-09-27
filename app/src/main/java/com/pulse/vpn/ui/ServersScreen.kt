package com.pulse.vpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.vpn.AppVm
import com.pulse.vpn.data.AccountState
import com.pulse.vpn.data.Servers
import com.pulse.vpn.ui.theme.*

@Composable
fun ServersScreen(vm: AppVm, state: AccountState, onBack: () -> Unit) {
    val latency by vm.latency.collectAsStateWithLifecycle()
    val up by vm.up.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Card).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Text("‹", color = TextMain, fontSize = 24.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("Список серверов", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp).clip(CircleShape).background(Card).clickable { vm.pingAll() }, contentAlignment = Alignment.Center) {
                Text("↻", color = TextMain, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).clickable { vm.pingAll() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 16.sp); Spacer(Modifier.width(10.dp))
            Text("Проверить скорость", color = TextMain, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("ⓘ", color = TextDim)
        }
        Spacer(Modifier.height(14.dp))
        (listOf(Servers.AUTO) + Servers.real()).forEach { s ->
            val ms = if (s.id == "auto") Servers.fastestMs(latency) else latency[s.id]
            val sel = state.selectedServerId == s.id
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (sel) Accent else Card)
                    .clickable { vm.select(s) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.flag, fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    if (s.id == "auto") "Автовыбор - самый быстрый" else s.name,
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)
                )
                Bars(ms); Spacer(Modifier.width(10.dp))
                Box(Modifier.size(22.dp).clip(CircleShape).background(if (sel) Color.White else CardHi), contentAlignment = Alignment.Center) {
                    if (sel) Text("✓", color = Accent, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (up) "Смена региона: VPN переподключится к новому серверу"
            else "Выбери регион - он применится при подключении",
            color = TextDim, fontSize = 12.sp
        )
    }
}
