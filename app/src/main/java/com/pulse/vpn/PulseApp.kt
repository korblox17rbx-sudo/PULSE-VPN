package com.pulse.vpn

import android.app.Application
import com.pulse.vpn.vpn.WireGuardManager

class PulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WireGuardManager.init(this)
    }
}
