package dev.vscodium.mobile.terminal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Drives a [TermuxBridgeServer] session and exposes its state to the UI. */
class TerminalViewModel : ViewModel() {
    private val bridge = TermuxBridgeServer(viewModelScope)

    val output: StateFlow<String> get() = bridge.output
    val connectionState: StateFlow<TermuxConnectionState> get() = bridge.state

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun isTermuxInstalled(context: Context) = TermuxLauncher.isTermuxInstalled(context)
    fun hasPermission(context: Context) = TermuxLauncher.hasRunCommandPermission(context)
    fun permissionName(): String = TermuxLauncher.RUN_COMMAND_PERMISSION

    /** Opens the loopback bridge, then asks Termux to attach a shell to it. */
    fun launchSession(context: Context) {
        _error.value = null
        val port = bridge.startListening()
        if (port == null) {
            _error.value = "Couldn't open the local terminal bridge socket. " +
                "Make sure the app has network access and try again."
            return
        }
        val ok = TermuxLauncher.launchBridgeSession(context, port)
        if (!ok) {
            _error.value = "Termux refused the request. Grant the RUN_COMMAND permission and try again."
            bridge.stop()
        }
    }

    fun sendCommand(command: String) = bridge.send(command)
    fun clearOutput() = bridge.clearOutput()
    fun disconnect() = bridge.stop()

    override fun onCleared() {
        bridge.stop()
        super.onCleared()
    }
}
