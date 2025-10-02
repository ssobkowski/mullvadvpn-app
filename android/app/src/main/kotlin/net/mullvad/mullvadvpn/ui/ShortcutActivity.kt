package net.mullvad.mullvadvpn.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import net.mullvad.mullvadvpn.lib.common.constant.KEY_CONNECT_ACTION
import net.mullvad.mullvadvpn.lib.common.constant.KEY_DISCONNECT_ACTION
import net.mullvad.mullvadvpn.lib.common.constant.VPN_SERVICE_CLASS

class ShortcutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            KEY_CONNECT_ACTION -> {
                val serviceIntent =
                    Intent().apply {
                        setClassName(packageName, VPN_SERVICE_CLASS)
                        action = KEY_CONNECT_ACTION
                    }
                startForegroundService(serviceIntent)
            }
            KEY_DISCONNECT_ACTION -> {
                val serviceIntent =
                    Intent().apply {
                        setClassName(packageName, VPN_SERVICE_CLASS)
                        action = KEY_DISCONNECT_ACTION
                    }
                startForegroundService(serviceIntent)
            }
        }

        finish()
    }
}
