package dev.vscodium.mobile.terminal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Talks to the Termux app via its `RUN_COMMAND` intent API to attach an
 * interactive bash session to [TermuxBridgeServer]'s loopback socket.
 *
 * This requires the Termux app to be installed, "Allow external apps" enabled
 * in Termux settings, and the `com.termux.permission.RUN_COMMAND` permission
 * granted to this app.
 */
object TermuxLauncher {
    const val TERMUX_PACKAGE = "com.termux"
    const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"

    private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    private const val SHELL_PATH = "/data/data/com.termux/files/usr/bin/bash"

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
     * Asks Termux to run a bash shell that immediately attaches itself
     * (stdin/stdout/stderr) to the loopback bridge listening on [port].
     * Returns false if Termux rejected the request.
     */
    fun launchBridgeSession(context: Context, port: Int): Boolean {
        // Open fd 3 as a duplex pipe to the bridge socket, then replace the
        // shell with an interactive bash whose stdio is that socket.
        val script = "exec 3<>/dev/tcp/127.0.0.1/$port; exec bash -i <&3 >&3 2>&3"

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
