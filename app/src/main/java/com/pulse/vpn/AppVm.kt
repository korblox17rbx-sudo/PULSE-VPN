package com.pulse.vpn

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.vpn.data.*
import com.pulse.vpn.vpn.WireGuardManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppVm(app: Application) : AndroidViewModel(app) {
    companion object { const val DAY = 86_400_000L }

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state

    private val _latency = MutableStateFlow<Map<String, Int>>(emptyMap())
    val latency: StateFlow<Map<String, Int>> = _latency

    val up = WireGuardManager.up

    private val _paywall = MutableStateFlow(false)
    val paywall: StateFlow<Boolean> = _paywall

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    private var pending: Server? = null

    val premiumNow: Boolean get() = System.currentTimeMillis() < _state.value.premiumUntil
    val loaded: Boolean get() = _state.value.trialStart > 0
    val canUseNow: Boolean get() =
        premiumNow || !loaded || System.currentTimeMillis() < _state.value.trialStart + 30 * DAY

    init {
        viewModelScope.launch {
            Prefs.ensureTrial(getApplication())
            Prefs.state(getApplication()).collect { _state.value = it }
        }
    }

    fun requestConnect(s: Server, askPermission: () -> Unit) {
        if (!canUseNow) { _paywall.value = true; return }
        pending = s
        askPermission()
    }

    fun proceedAfterPermission(ctx: Context) {
        val s = pending ?: Servers.byId(_state.value.selectedServerId) ?: Servers.fastest(_latency.value)
        pending = null
        viewModelScope.launch {
            WireGuardManager.connect(s, premiumNow)
                .onSuccess { _toast.value = "Pulse VPN подключен -  ${if (s.id == "auto") "Автовыбор" else s.name}" }
                .onFailure { _toast.value = "Ошибка подключения: ${it.message}" }
        }
    }

    fun disconnect() = viewModelScope.launch { WireGuardManager.disconnect() }

    fun select(s: Server) = viewModelScope.launch {
        Prefs.setServer(getApplication(), s.id)
        if (WireGuardManager.up.value) {
            val target = if (s.id == "auto") Servers.fastest(_latency.value) else s
            WireGuardManager.disconnect()
            WireGuardManager.connect(target, premiumNow)
            _toast.value = "Регион изменен: ${target.flag} ${target.name}"
        }
    }

    fun pingAll() = viewModelScope.launch(Dispatchers.IO) {
        val m = mutableMapOf<String, Int>()
        Servers.real().forEach { m[it.id] = Net.latency(it.host) }
        _latency.value = m
    }

    suspend fun traffic(): Long? = WireGuardManager.trafficBytes()

    fun importLicense(ctx: Context, uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val text = runCatching { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull()
        if (text.isNullOrBlank()) { _toast.value = "Не удалось прочитать файл"; return@launch }
        val r = License.verify(text)
        if (r.ok) {
            Prefs.addPremium(getApplication(), r.days)
            _toast.value = "✅ ${r.message}"
        } else _toast.value = "❌ ${r.message}"
    }

    fun consentShown() = viewModelScope.launch { Prefs.setConsent(getApplication()) }
    fun openPaywall() { _paywall.value = true }
    fun closePaywall() { _paywall.value = false }
    fun clearToast() { _toast.value = null }
    fun toastMsg(t: String) { _toast.value = t }
}
