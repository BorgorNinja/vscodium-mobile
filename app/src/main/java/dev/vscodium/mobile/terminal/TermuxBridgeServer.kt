package dev.vscodium.mobile.terminal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

enum class TermuxConnectionState { DISCONNECTED, LISTENING, CONNECTED, ERROR }

/**
 * Local loopback TCP bridge that acts as the "API" between this app and a
 * Termux session. The app listens on a free [port]; a shell launched inside
 * Termux (see [TermuxLauncher]) connects back to that port via `/dev/tcp` and
 * pipes its stdin/stdout/stderr through the socket, giving the app a live,
 * interactive shell session it can read from and write to.
 */
class TermuxBridgeServer(private val scope: CoroutineScope) {

    var port: Int = 0
        private set

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var outputStream: OutputStream? = null
    private var listenJob: Job? = null

    private val _state = MutableStateFlow(TermuxConnectionState.DISCONNECTED)
    val state: StateFlow<TermuxConnectionState> = _state

    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output

    /** Opens a loopback server socket on a free port and starts listening for the Termux session. */
    fun startListening(): Int {
        stop()
        val socket = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        serverSocket = socket
        port = socket.localPort
        _state.value = TermuxConnectionState.LISTENING
        _output.value = ""

        listenJob = scope.launch(Dispatchers.IO) {
            try {
                val client = socket.accept()
                clientSocket = client
                outputStream = client.getOutputStream()
                _state.value = TermuxConnectionState.CONNECTED

                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val buffer = CharArray(4096)
                while (isActive) {
                    val read = reader.read(buffer)
                    if (read == -1) break
                    _output.value += String(buffer, 0, read)
                }
            } catch (e: IOException) {
                if (_state.value == TermuxConnectionState.CONNECTED || _state.value == TermuxConnectionState.LISTENING) {
                    _state.value = TermuxConnectionState.ERROR
                }
            } finally {
                if (_state.value != TermuxConnectionState.DISCONNECTED) {
                    _state.value = TermuxConnectionState.DISCONNECTED
                }
                outputStream = null
            }
        }
        return port
    }

    /** Sends a line of input to the connected Termux shell, appending a newline. */
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

    fun clearOutput() {
        _output.value = ""
    }

    /** Closes the session and frees the socket. */
    fun stop() {
        _state.value = TermuxConnectionState.DISCONNECTED
        listenJob?.cancel()
        try { clientSocket?.close() } catch (e: IOException) { /* ignore */ }
        try { serverSocket?.close() } catch (e: IOException) { /* ignore */ }
        clientSocket = null
        outputStream = null
        serverSocket = null
        listenJob = null
    }
}
