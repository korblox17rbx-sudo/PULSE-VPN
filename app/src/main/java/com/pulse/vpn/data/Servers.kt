package com.pulse.vpn.data

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class Server(
    val id: String, val name: String, val flag: String, val premium: Boolean,
    val host: String, val port: Int,
    val serverPub: String,
    val clientPriv: String,
    val clientIp: String
)

object Servers {
    // ⚠️ Это ЗАПОЛНЯЕМЫЕ заглушки формата WireGuard. Замени host/ключи на свои VPS.
    private val DE = Server("de", "Германия", "🇩🇪", false, "de1.pulsevpn.net", 51820,
        "pK7tX2vR9mQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8=", "8fJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.66.2")
    private val NL = Server("nl", "Нидерланды", "🇳🇱", false, "nl1.pulsevpn.net", 51820,
        "nQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8pK7tX2vR=", "9kJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.67.2")
    private val UK = Server("uk", "Великобритания", "🇬🇧", false, "uk1.pulsevpn.net", 51820,
        "uQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8pK7tX2vR=", "7hJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.68.2")
    private val US = Server("us", "США", "🇺🇸", true, "us1.pulsevpn.net", 51820,
        "sQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8pK7tX2vR=", "6gJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.69.2")
    private val JP = Server("jp", "Япония", "🇯🇵", true, "jp1.pulsevpn.net", 51820,
        "jQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8pK7tX2vR=", "5fJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.70.2")
    private val SG = Server("sg", "Сингапур", "🇸🇬", true, "sg1.pulsevpn.net", 51820,
        "gQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8pK7tX2vR=", "4eJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.71.2")
    private val TR = Server("tr", "Турция", "🇹🇷", true, "tr1.pulsevpn.net", 51820,
        "tQwErTyU3iOpAsDfGhJkLzxCvBnM4FDkS8pK7tX2vR=", "3dJ2kL9pQwErTyUiOpAsDfGhJkLzxCvBnM3NvXyQ1aZ=", "10.66.72.2")

    val AUTO = Server("auto", "Автовыбор", "🚀", false, "", 0, "", "", "")

    fun all() = listOf(AUTO, DE, NL, UK, US, JP, SG, TR)
    fun real() = all().filter { it.id != "auto" }
    fun byId(id: String?) = all().firstOrNull { it.id == id }
    fun fastestMs(lat: Map<String, Int>): Int =
        real().mapNotNull { lat[it.id] }.filter { it in 1..9000 }.minOrNull() ?: -1
    fun fastest(lat: Map<String, Int>): Server =
        real().filter { (lat[it.id] ?: 9999) < 9000 }.minByOrNull { lat[it.id] ?: 9999 } ?: real().first()
}

object Net {
    fun latency(host: String): Int {
        if (host.isEmpty()) return -1
        var best = Int.MAX_VALUE
        runCatching {
            val t0 = System.currentTimeMillis()
            Socket().use { it.connect(InetSocketAddress(host, 443), 1200) }
            best = (System.currentTimeMillis() - t0).toInt()
        }
        if (best == Int.MAX_VALUE) runCatching {
            val t0 = System.currentTimeMillis()
            InetAddress.getByName(host).isReachable(1200)
            best = (System.currentTimeMillis() - t0).toInt() + 300
        }
        return if (best == Int.MAX_VALUE) 9999 else best
    }
}
