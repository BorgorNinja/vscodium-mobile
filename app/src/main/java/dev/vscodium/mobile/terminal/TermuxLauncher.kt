package dev.vscodium.mobile.terminal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Talks to the Termux app via its `RUN_COMMAND` intent API to start an ncat
 * listener that the app then connects out to.
 *
 * Requires Termux installed, "Allow External Apps" enabled in termux.properties,
 * and the `com.termux.permission.RUN_COMMAND` permission granted to this app.
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
     * Asks Termux to start an ncat listener on [TermuxBridgeServer.BRIDGE_PORT].
     * Ncat's -e flag hands stdin/stdout directly to bash, giving the app a clean
     * interactive shell once it connects out to that port.
     *
     * Connection direction: app (client) → Termux ncat (server)
     * This direction is permitted by Android's loopback UID isolation.
     */
    fun launchBridgeSession(context: Context): Boolean {
        val script = "$NCAT_PATH -l 127.0.0.1 ${TermuxBridgeServer.BRIDGE_PORT} -e $SHELL_PATH"

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
