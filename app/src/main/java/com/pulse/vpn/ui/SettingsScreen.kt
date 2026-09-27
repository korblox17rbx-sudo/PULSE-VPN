package com.pulse.vpn.ui

import android.content.Intent
import android.provider.Settings as SysSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.vpn.AppVm
import com.pulse.vpn.admin.AdminKit
import com.pulse.vpn.data.AccountState
import com.pulse.vpn.ui.theme.*

@Composable
fun SettingsScreen(vm: AppVm, state: AccountState, onOpenAdmin: () -> Unit) {
    val ctx = LocalContext.current
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.importLicense(ctx, it) }
    }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Настройки", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        SettingCard("Kill Switch (Always-On VPN)") {
            Text("Если связь оборвётся - интернет заблокируется, и твой IP не «засветится». Включается в системных настройках Android.", color = TextDim, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            SmallButton("Открыть системные настройки") {
                runCatching { ctx.startActivity(Intent(SysSettings.ACTION_VPN_SETTINGS)) }
            }
        }
        SettingCard("Лицензия") {
            Text("Есть подписанный файл лицензии? Импортируй -  премиум добавится автоматически.", color = TextDim, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            SmallButton("Импортировать лицензию (.json)") { import.launch(arrayOf("*/*")) }
        }
        SettingCard("Набор управления") {
            Text("Скачивает папку pulse_admin.py + README в Download/PulseVPN_Admin - для выдачи бесплатных дней и премиума другим пользователям.", color = TextDim, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallButton("⬇ Скачать") {
                    val n = AdminKit.download(ctx)
                    vm.toastMsg("Сохранено файлов: $n → Download/PulseVPN_Admin")
                }
                SmallButton("Панель управления") { onOpenAdmin() }
            }
        }
        SettingCard("О приложении") {
            Text("Pulse VPN 1.0 · ядро WireGuard\nПробный период 30 дней, далее премиум 130 ₽/мес.", color = TextDim, fontSize = 13.sp)
        }
    }
}

@Composable
fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).padding(16.dp)) {
        Text(title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        content()
        Spacer(Modifier.height(4.dp))
    }
    Spacer(Modifier.height(12.dp))
}
