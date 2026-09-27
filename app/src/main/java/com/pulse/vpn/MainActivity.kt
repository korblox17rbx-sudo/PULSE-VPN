package com.pulse.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulse.vpn.data.Servers
import com.pulse.vpn.ui.AdminScreen
import com.pulse.vpn.ui.BottomNav
import com.pulse.vpn.ui.ConsentExplainer
import com.pulse.vpn.ui.MainScreen
import com.pulse.vpn.ui.PaywallDialog
import com.pulse.vpn.ui.ServersScreen
import com.pulse.vpn.ui.SettingsScreen
import com.pulse.vpn.ui.SupportScreen
import com.pulse.vpn.ui.theme.Bg
import com.pulse.vpn.ui.theme.PulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PulseTheme { Root() } }
    }
}

@Composable
fun Root() {
    val vm: AppVm = viewModel()
    val ctx = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val up by vm.up.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val paywall by vm.paywall.collectAsStateWithLifecycle()

    var screen by rememberSaveable { mutableStateOf("main") }
    var showExplainer by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) vm.proceedAfterPermission(ctx)
    }
    fun requestVpn() {
        val intent: Intent? = VpnService.prepare(ctx)
        if (intent != null) permLauncher.launch(intent) else vm.proceedAfterPermission(ctx)
    }

    LaunchedEffect(toast) {
        toast?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); vm.clearToast() }
    }

    val onToggle: () -> Unit = {
        if (up) vm.disconnect()
        else vm.requestConnect(Servers.byId(state.selectedServerId) ?: Servers.fastest(vm.latency.value)) {
            if (!state.consentExplained) showExplainer = true else requestVpn()
        }
    }

    BackHandler(enabled = screen != "main") { screen = "main" }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.weight(1f)) {
            when (screen) {
                "main" -> MainScreen(vm, state, up, onToggle, { screen = "servers" }, { screen = "admin" })
                "servers" -> ServersScreen(vm, state, onBack = { screen = "main" })
                "settings" -> SettingsScreen(vm, state, onOpenAdmin = { screen = "admin" })
                "support" -> SupportScreen()
                "admin" -> AdminScreen(vm, state, onBack = { screen = "main" })
            }
        }
        if (screen in listOf("main", "settings", "support")) BottomNav(screen) { screen = it }
    }

    if (showExplainer) {
        ConsentExplainer(
            onOk = { showExplainer = false; vm.consentShown(); requestVpn() },
            onCancel = { showExplainer = false }
        )
    }
    if (paywall) PaywallDialog(vm)
}
