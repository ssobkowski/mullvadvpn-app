package net.mullvad.mullvadvpn.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import arrow.core.merge
import net.mullvad.mullvadvpn.compose.util.CreateVpnProfile
import net.mullvad.mullvadvpn.lib.common.constant.KEY_CONNECT_ACTION
import net.mullvad.mullvadvpn.lib.common.constant.KEY_DISCONNECT_ACTION
import net.mullvad.mullvadvpn.lib.common.constant.VPN_SERVICE_CLASS
import net.mullvad.mullvadvpn.lib.common.util.prepareVpnSafe
import net.mullvad.mullvadvpn.lib.model.PrepareError
import net.mullvad.mullvadvpn.lib.model.Prepared

class ShortcutActivity : ComponentActivity() {

    private val launchVpnPermission =
        registerForActivityResult(CreateVpnProfile()) { _ -> executeConnectAction() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the shortcut intent and forward to service
        when (intent?.action) {
            KEY_CONNECT_ACTION -> {
                handleConnectAction()
            }
            KEY_DISCONNECT_ACTION -> {
                executeDisconnectAction()
            }
            else -> {
                finish()
            }
        }
    }

    private fun handleConnectAction() {
        val prepareResult = prepareVpnSafe().merge()
        when (prepareResult) {
            is PrepareError.NotPrepared -> {
                // Launch VPN permission request
                launchVpnPermission.launch(prepareResult.prepareIntent)
            }
            // If legacy or other always on connect, let daemon generate an error state
            is PrepareError.OtherLegacyAlwaysOnVpn,
            is PrepareError.OtherAlwaysOnApp,
            Prepared -> {
                executeConnectAction()
            }
        }
    }

    private fun executeConnectAction() {
        val serviceIntent =
            Intent().apply {
                setClassName(packageName, VPN_SERVICE_CLASS)
                action = KEY_CONNECT_ACTION
            }
        startForegroundService(serviceIntent)
        finish()
    }

    private fun executeDisconnectAction() {
        val serviceIntent =
            Intent().apply {
                setClassName(packageName, VPN_SERVICE_CLASS)
                action = KEY_DISCONNECT_ACTION
            }
        startForegroundService(serviceIntent)
        finish()
    }
}
