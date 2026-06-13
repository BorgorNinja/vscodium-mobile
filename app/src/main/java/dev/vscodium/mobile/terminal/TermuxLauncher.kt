package dev.vscodium.mobile.terminal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Sends a RUN_COMMAND intent to Termux to start an ncat listener that the app
 * connects to via the device's own non-loopback IP.
 *
 * WHY 0.0.0.0:
 * Binding ncat to 127.0.0.1 means it only accepts on the loopback interface,
 * where Android's per-UID iptables rules silently drop cross-app packets.
 * Binding to 0.0.0.0 lets the app reach ncat via the WiFi/cellular IP instead,
 * which routes through wlan0/rmnet0 and bypasses the loopback UID filter.
 */
object TermuxLauncher {
    const val TERMUX_PACKAGE = "com.termux"
    const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"

    private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    private const val SHELL_PATH = "/data/data/com.termux/files/usr/bin/bash"
    private const val NCAT_PATH = "/data/data/com.termux/files/usr/bin/ncat"

    fun isTermuxInstalled(context: Context): Boolean =
        try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    fun hasRunCommandPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, RUN_COMMAND_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Starts: ncat -l 0.0.0.0 [PORT] -e bash
     *
     * ncat binds on all interfaces so the app can reach it via the device's
     * own WiFi/cellular address rather than the UID-filtered loopback.
     * [TermuxBridgeServer.findLocalIp] picks that address at connect time.
     */
    fun launchBridgeSession(context: Context): Boolean {
        val script =
            "$NCAT_PATH -l 0.0.0.0 ${TermuxBridgeServer.BRIDGE_PORT} -e $SHELL_PATH"

        val intent = Intent(ACTION_RUN_COMMAND).apply {
            setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE)
            putExtra("com.termux.RUN_COMMAND_PATH", SHELL_PATH)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", script))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
        }

        return try {
            ContextCompat.startForegroundService(context, intent)
            true
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalStateException) {
            false
        }
    }
}
