package com.pulse.vpn.admin

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore

object AdminKit {
    const val DIR = "Download/PulseVPN_Admin"

    val PY = """
        |#!/usr/bin/env python3
        |# Pulse VPN -  генератор лицензий (админ-набор)
        |import argparse, hashlib, hmac, json, time, uuid, os
        |
        |SECRET = os.environ.get("PULSE_SECRET", "PULSE_CHANGE_ME_9f2c41")
        |
        |def make_lic(t, days):
        |    tid = uuid.uuid4().hex[:12]
        |    ts = int(time.time())
        |    data = "%s|%d|%s|%d" % (t, days, tid, ts)
        |    sig = hmac.new(SECRET.encode(), data.encode(), hashlib.sha256).hexdigest()
        |    return {"app": "pulse", "type": t, "days": days, "tid": tid, "ts": ts, "sig": sig}
        |
        |def main():
        |    ap = argparse.ArgumentParser(description="Pulse VPN license generator")
        |    ap.add_argument("--type", default="premium", choices=["premium", "bonus"])
        |    ap.add_argument("--days", type=int, default=30)
        |    ap.add_argument("--out", default=None)
        |    a = ap.parse_args()
        |    lic = make_lic(a.type, a.days)
        |    name = a.out or ("pulse-license-%s.json" % uuid.uuid4().hex[:6])
        |    with open(name, "w", encoding="utf-8") as f:
        |        json.dump(lic, f, ensure_ascii=False, indent=2)
        |    print("Готово:", name)
        |    print("Срок: %d дн. Тип: %s" % (a.days, a.type))
        |    print("Отправь файл пользователю. Импорт в приложении: Настройки -> Импортировать лицензию.")
        |
        |if __name__ == "__main__":
        |    main()
    """.trimMargin()

    val README = """
        |PULSE VPN - НАБОР УПРАВЛЕНИЯ
        |
        |1) Установи Python 3.10+ (python.org).
        |2) Экспортируй секрет (тот же, что в License.kt приложения):
        |   Windows:   set PULSE_SECRET=твой_секрет
        |   Linux/mac: export PULSE_SECRET=твой_секрет
        |3) Выдать 30 дней премиума:
        |   python pulse_admin.py --days 30
        |4) Полученный .json отправь пользователю (Telegram/почта).
        |   Импорт у него: Настройки -> Импортировать лицензию.
        |
        |Пока секрет совпадает -  лицензии будут приниматься.
    """.trimMargin()

    fun download(ctx: Context): Int {
        var ok = 0
        fun save(name: String, mime: String, text: String) {
            val cv = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, DIR)
            }
            runCatching {
                ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)?.let { uri ->
                    ctx.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                    ok++
                }
            }
        }
        save("pulse_admin.py", "text/x-python", PY)
        save("README_ADMIN.txt", "text/plain", README)
        return ok
    }
}
