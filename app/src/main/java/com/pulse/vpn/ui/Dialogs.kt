package com.pulse.vpn.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.pulse.vpn.AppVm
import com.pulse.vpn.ui.theme.*

object Pay {
    const val URL = "https://t.me/Pulse_tex"
}

@Composable
fun ConsentExplainer(onOk: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Card,
        title = { Text("Запрос на подключение", color = TextMain, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Pulse VPN запрашивает разрешение на настройку VPN-подключения, которое позволяет отслеживать сетевой трафик. Разрешайте это только в том случае, если вы доверяете источнику.\n\nВо время использования VPN сверху экрана будет выводиться значок. Разрешить?",
                color = TextDim, fontSize = 14.sp
            )
        },
        confirmButton = { TextButton(onClick = onOk) { Text("OK", color = Accent) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Отмена", color = Accent) } }
    )
}

@Composable
fun PaywallDialog(vm: AppVm) {
    val ctx = LocalContext.current
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.importLicense(ctx, it) }
    }
    Dialog(onDismissRequest = { vm.closePaywall() }) {
        Column(Modifier.clip(RoundedCornerShape(24.dp)).background(Card).padding(22.dp)) {
            Text("👑 Премиум Pulse VPN", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            listOf(
                "Приоритетные серверы и мгновенный автоподбор",
                "Ускоренный режим соединения (keepalive 10 c)",
                "Новые регионы -  раньше всех",
                "Поддержка 24/7 в Telegram"
            ).forEach { Text("•  $it", color = TextDim, fontSize = 14.sp, modifier = Modifier.padding(vertical = 3.dp)) }
            Spacer(Modifier.height(12.dp))
            Text("130 ₽ / месяц", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Accent, AccentSoft))).clickable {
                        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Pay.URL))) }
                    },
                contentAlignment = Alignment.Center
            ) { Text("Оплатить 130 ₽/мес", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                    .border(1.dp, TextDim, RoundedCornerShape(16.dp)).clickable { import.launch(arrayOf("*/*")) },
                contentAlignment = Alignment.Center
            ) { Text("Ввести лицензию", color = TextMain, fontSize = 15.sp) }
            Spacer(Modifier.height(10.dp))
            Text("Оплатил? Напиши в @Pulse_tex -  вышлем файл лицензии.", color = TextDim, fontSize = 12.sp)
            Text("Позже", color = Accent, fontSize = 14.sp, modifier = Modifier.clickable { vm.closePaywall() }.padding(top = 6.dp))
        }
    }
}
