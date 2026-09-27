package com.pulse.vpn.data

import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class LicenseResult(val ok: Boolean, val days: Int, val message: String)

object License {
    // ЗАМЕНИ на свой секрет -  тот же, что в pulse_admin.py (переменная PULSE_SECRET)
    const val SECRET = "PULSE_CHANGE_ME_9f2c41"

    fun verify(json: String): LicenseResult = try {
        val o = JSONObject(json)
        if (o.optString("app") != "pulse") LicenseResult(false, 0, "Это не файл лицензии Pulse VPN")
        else {
            val type = o.optString("type")
            val days = o.optInt("days", 0)
            val tid = o.optString("tid")
            val ts = o.optLong("ts", 0)
            val sig = o.optString("sig")
            when {
                days <= 0 || (type != "premium" && type != "bonus") -> LicenseResult(false, 0, "Неверная лицензия")
                hmac("$type|$days|$tid|$ts") != sig -> LicenseResult(false, 0, "Подпись не совпадает")
                else -> LicenseResult(true, days, "Премиум активирован: +$days дн.")
            }
        }
    } catch (e: Exception) { LicenseResult(false, 0, "Ошибка чтения файла") }

    fun hmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
