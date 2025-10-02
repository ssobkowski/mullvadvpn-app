package net.mullvad.mullvadvpn.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.mullvad.mullvadvpn.lib.common.constant.KEY_CONNECT_ACTION
import net.mullvad.mullvadvpn.lib.common.constant.KEY_DISCONNECT_ACTION
import net.mullvad.mullvadvpn.lib.model.DeviceState
import net.mullvad.mullvadvpn.lib.resource.R
import net.mullvad.mullvadvpn.lib.shared.DeviceRepository
import net.mullvad.mullvadvpn.ui.ShortcutActivity

@RequiresApi(Build.VERSION_CODES.N_MR1)
class ShortcutManagerService(
    private val context: Context,
    private val deviceRepository: DeviceRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val CONNECT_SHORTCUT_ID = "connect"
        private const val DISCONNECT_SHORTCUT_ID = "disconnect"
    }

    private val shortcutManager = context.getSystemService(ShortcutManager::class.java)

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            scope.launch {
                deviceRepository.deviceState
                    .map { it is DeviceState.LoggedIn }
                    .distinctUntilChanged()
                    .collect { isLoggedIn ->
                        if (isLoggedIn) {
                            enableShortcuts()
                        } else {
                            disableShortcuts()
                        }
                    }
            }
        }
    }

    private fun enableShortcuts() {
        val shortcuts = listOf(createConnectShortcut(), createDisconnectShortcut())
        shortcutManager.setDynamicShortcuts(shortcuts)
    }

    private fun disableShortcuts() {
        shortcutManager.removeAllDynamicShortcuts()
    }

    private fun createConnectShortcut(): ShortcutInfo {
        val intent =
            Intent(context, ShortcutActivity::class.java).apply {
                action = KEY_CONNECT_ACTION
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }

        return ShortcutInfo.Builder(context, CONNECT_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.shortcut_connect))
            .setLongLabel(context.getString(R.string.shortcut_connect_long))
            .setIcon(Icon.createWithResource(context, R.drawable.icon_notification_connect))
            .setIntent(intent)
            .build()
    }

    private fun createDisconnectShortcut(): ShortcutInfo {
        val intent =
            Intent(context, ShortcutActivity::class.java).apply {
                action = KEY_DISCONNECT_ACTION
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }

        return ShortcutInfo.Builder(context, DISCONNECT_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.shortcut_disconnect))
            .setLongLabel(context.getString(R.string.shortcut_disconnect_long))
            .setIcon(Icon.createWithResource(context, R.drawable.icon_notification_disconnect))
            .setIntent(intent)
            .build()
    }
}
