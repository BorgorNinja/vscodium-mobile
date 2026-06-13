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
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.Socket

enum class TermuxConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

/**
 * Connects out to an ncat listener started inside Termux by [TermuxLauncher].
 *
 * WHY NOT 127.0.0.1:
 * Android (and especially Infinix's ROM) enforces per-UID loopback isolation via
 * iptables rules on the `lo` interface. Packets from one app UID to a socket owned
 * by a different app UID on 127.0.0.1 are silently dropped in both directions.
 *
 * THE FIX:
 * ncat binds on 0.0.0.0 and the app connects via the device's own non-loopback
 * IPv4 address (e.g. the WiFi or mobile-data IP). That traffic routes through
 * `wlan0`/`rmnet0` instead of `lo`, so the UID filter is never applied.
 */
class TermuxBridgeServer(private val scope: CoroutineScope) {

    companion object {
        const val BRIDGE_PORT = 12399
        private const val CONNECT_RETRIES = 12
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
     * Returns the device's first non-loopback, non-link-local IPv4 address.
     * Prefers `wlan0` (WiFi) but falls back to any live interface.
     * Returns null only if the device has no usable network interface up.
     */
    private fun findLocalIp(): String? = try {
        NetworkInterface.getNetworkInterfaces()
            ?.asSequence()
            ?.filter { it.isUp && !it.isLoopback }
            ?.flatMap { it.inetAddresses.asSequence() }
            ?.filterIsInstance<Inet4Address>()
            ?.filterNot { it.isLinkLocalAddress }
            ?.firstOrNull()
            ?.hostAddress
    } catch (e: Exception) {
        null
    }

    /**
     * Resolves the host, then retries the TCP connect up to [CONNECT_RETRIES]
     * times with [RETRY_DELAY_MS] gaps — giving Termux time to start ncat.
     */
    fun connect() {
        stop()
        _state.value = TermuxConnectionState.CONNECTING
        _output.value = ""

        connectJob = scope.launch(Dispatchers.IO) {
            val host = findLocalIp()
            if (host == null) {
                _state.value = TermuxConnectionState.ERROR
                return@launch
            }

            var connected = false
            repeat(CONNECT_RETRIES) { attempt ->
                if (!isActive) return@launch
                try {
                    val s = Socket(host, BRIDGE_PORT)
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

    /** Sends a line of input to the connected bash session. */
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

    fun stop() {
        _state.value = TermuxConnectionState.DISCONNECTED
        connectJob?.cancel()
        try { socket?.close() } catch (e: IOException) { /* ignore */ }
        socket = null
        outputStream = null
        connectJob = null
    }
}
