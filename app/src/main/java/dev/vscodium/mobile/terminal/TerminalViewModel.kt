package dev.vscodium.mobile.terminal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    /**
     * Fires the RUN_COMMAND intent to start ncat in Termux, waits briefly for
     * the listener to be ready, then connects the bridge.
     */
    fun launchSession(context: Context) {
        _error.value = null
        val ok = TermuxLauncher.launchBridgeSession(context)
        if (!ok) {
            _error.value = "Termux refused the request. " +
                "Grant the RUN_COMMAND permission and try again."
            return
        }
        // Allow Termux a moment to start ncat before the app connects out
        viewModelScope.launch {
            delay(400)
            bridge.connect()
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
