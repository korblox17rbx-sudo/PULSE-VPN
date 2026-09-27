package com.pulse.vpn.vpn

import android.content.Context
import com.pulse.vpn.data.Server
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.InetNetwork
import com.wireguard.config.Peer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.net.InetAddress

object WireGuardManager {
    private var backend: GoBackend? = null
    private var tunnel: Tunnel? = null
    private val _up = MutableStateFlow(false)
    val up: StateFlow<Boolean> = _up

    fun init(app: Context) {
        if (backend == null) backend = GoBackend(app.applicationContext)
    }

    private class PulseTunnel(private val tunnelName: String) : Tunnel {
        override fun getName(): String = tunnelName
        override fun onStateChange(newState: Tunnel.State) {
            _up.value = newState == Tunnel.State.UP
        }
    }

    fun buildConfig(s: Server, premium: Boolean): Config {
        val keepalive = if (premium) "10" else "25"
        return Config.Builder()
            .setInterface(
                Interface.Builder()
                    .addAddress(InetNetwork.parse("${s.clientIp}/32"))
                    .parsePrivateKey(s.clientPriv)
                    .addDnsServer(InetAddress.getByName("1.1.1.1"))
                    .addDnsServer(InetAddress.getByName("8.8.8.8"))
                    .build()
            )
            .addPeer(
                Peer.Builder()
                    .parsePublicKey(s.serverPub)
                    .parseEndpoint("${s.host}:${s.port}")
                    .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
                    .addAllowedIp(InetNetwork.parse("::/0"))
                    .parsePersistentKeepalive(keepalive)
                    .build()
            )
            .build()
    }

    suspend fun connect(s: Server, premium: Boolean) = withContext(Dispatchers.IO) {
        runCatching {
            val b = backend ?: error("Бэкенд не инициализирован")
            val t = PulseTunnel("Pulse VPN")
            tunnel = t
            b.setState(t, Tunnel.State.UP, buildConfig(s, premium))
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching {
            val t = tunnel ?: return@withContext
            backend?.setState(t, Tunnel.State.DOWN, null)
        }
    }

    suspend fun trafficBytes(): Long? = withContext(Dispatchers.IO) {
        runCatching {
            val t = tunnel ?: return@withContext null
            val st = backend?.getStatistics(t)
            (st?.totalRx() ?: 0L) + (st?.totalTx() ?: 0L)
        }.getOrNull()
    }
}
