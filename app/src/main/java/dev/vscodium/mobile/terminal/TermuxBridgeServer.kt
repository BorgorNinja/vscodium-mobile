package dev.vscodium.mobile.terminal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket

enum class TermuxConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

/**
 * Connects out to a local ncat listener started inside Termux by [TermuxLauncher].
 *
 * Architecture reversal: the app is now the TCP *client*; Termux is the *server*.
 * This sidesteps Android's per-UID loopback isolation which blocks
 * Termux-UID → app-UID connections but permits app-UID → Termux-UID connections.
 *
 * Termux runs: ncat -l 127.0.0.1 [BRIDGE_PORT] -e bash
 * This app connects out to that port and pipes I/O through the socket.
 */
class TermuxBridgeServer(private val scope: CoroutineScope) {

    companion object {
        const val BRIDGE_PORT = 12399
        private const val CONNECT_RETRIES = 10
        private const val RETRY_DELAY_MS = 500L
    }

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var connectJob: Job? = null

    private val _state = MutableStateFlow(TermuxConnectionState.DISCONNECTED)
    val state: StateFlow<TermuxConnectionState> = _state

    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output

    /**
     * Attempts to connect to the ncat listener on [BRIDGE_PORT], retrying up to
     * [CONNECT_RETRIES] times with [RETRY_DELAY_MS] gaps to give Termux time to
     * start listening before we give up.
     */
    fun connect() {
        stop()
        _state.value = TermuxConnectionState.CONNECTING
        _output.value = ""

        connectJob = scope.launch(Dispatchers.IO) {
            var connected = false
            repeat(CONNECT_RETRIES) { attempt ->
                if (!isActive) return@launch
                try {
                    val s = Socket(InetAddress.getLoopbackAddress(), BRIDGE_PORT)
                    socket = s
                    outputStream = s.getOutputStream()
                    _state.value = TermuxConnectionState.CONNECTED
                    connected = true

                    val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                    val buffer = CharArray(4096)
                    while (isActive) {
                        val read = reader.read(buffer)
                        if (read == -1) break
                        _output.value += String(buffer, 0, read)
                    }
                    return@launch
                } catch (e: IOException) {
                    if (attempt < CONNECT_RETRIES - 1) delay(RETRY_DELAY_MS)
                }
            }
            if (!connected) _state.value = TermuxConnectionState.ERROR
        }
    }

    /** Sends a line of input to the connected bash session, appending a newline. */
    fun send(command: String) {
        val out = outputStream ?: return
        scope.launch(Dispatchers.IO) {
            try {
                out.write((command + "\n").toByteArray())
                out.flush()
            } catch (e: IOException) {
                _state.value = TermuxConnectionState.ERROR
            }
        }
    }

    fun clearOutput() { _output.value = "" }

    /** Closes the session and frees the socket. */
    fun stop() {
        _state.value = TermuxConnectionState.DISCONNECTED
        connectJob?.cancel()
        try { socket?.close() } catch (e: IOException) { /* ignore */ }
        socket = null
        outputStream = null
        connectJob = null
    }
}
