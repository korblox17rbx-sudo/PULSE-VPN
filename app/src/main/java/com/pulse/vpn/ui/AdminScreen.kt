package com.pulse.vpn.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun AdminScreen(vm: AppVm, state: AccountState, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.importLicense(ctx, it) }
    }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Card).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Text("‹", color = TextMain, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text("Управление Pulse VPN", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        SettingCard("Текущий статус") {
            Text(
                if (System.currentTimeMillis() < state.premiumUntil) "Премиум до " + dmy(state.premiumUntil)
                else "Пробный период: осталось ${daysLeft(state.trialStart, System.currentTimeMillis())} дн.",
                color = TextMain, fontSize = 14.sp
            )
        }
        SettingCard("Набор управления (выдача премиума)") {
            Text("Скачивает папку с pulse_admin.py и README в Download/PulseVPN_Admin. Скрипт подписывает лицензии твоим секретом.", color = TextDim, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            SmallButton("⬇ Скачать папку управления") {
                val n = AdminKit.download(ctx)
                vm.toastMsg("Сохранено файлов: $n → Download/PulseVPN_Admin")
            }
        }
        SettingCard("Импорт лицензии (тест)") {
            SmallButton("Выбрать .json лицензию") { import.launch(arrayOf("*/*")) }
        }
        SettingCard("Как выдать пользователю премиум") {
            Text("1. export PULSE_SECRET=<секрет из License.kt>\n2. python pulse_admin.py --days 30\n3. Отправь .json пользователю", color = TextDim, fontSize = 13.sp)
        }
    }
}

@Composable
fun SmallButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(14.dp)).background(CardHi).clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) { Text(label, color = TextMain, fontSize = 14.sp) }
}
