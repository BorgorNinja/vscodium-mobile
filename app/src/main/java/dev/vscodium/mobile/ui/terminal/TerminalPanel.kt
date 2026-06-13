package dev.vscodium.mobile.ui.terminal

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vscodium.mobile.terminal.TermuxConnectionState
import dev.vscodium.mobile.terminal.TerminalViewModel

/** Integrated terminal panel: shows live Termux output and a command input. */
@Composable
fun TerminalPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: TerminalViewModel = viewModel()
    val output by viewModel.output.collectAsState()
    val state by viewModel.connectionState.collectAsState()
    val error by viewModel.error.collectAsState()
    var input by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val installed = remember { viewModel.isTermuxInstalled(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.launchSession(context) }

    LaunchedEffect(output) { scrollState.animateScrollTo(scrollState.maxValue) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Terminal", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            AssistChip(onClick = {}, label = { Text(state.label()) })
            IconButton(onClick = { viewModel.clearOutput() }) {
                Icon(Icons.Filled.ClearAll, contentDescription = "Clear output")
            }
            if (state == TermuxConnectionState.CONNECTED) {
                IconButton(onClick = { viewModel.disconnect() }) {
                    Icon(Icons.Filled.Stop, contentDescription = "Disconnect")
                }
            } else {
                IconButton(
                    onClick = {
                        if (viewModel.hasPermission(context)) {
                            viewModel.launchSession(context)
                        } else {
                            permissionLauncher.launch(viewModel.permissionName())
                        }
                    },
                    enabled = installed,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start session")
                }
            }
        }
        HorizontalDivider()

        when {
            !installed -> CenteredMessage("Termux isn't installed. Install Termux to use the integrated terminal.")
            error != null -> CenteredMessage(error.orEmpty())
            else -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = output.ifEmpty { "Tap \u25B6 to start a Termux session." },
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                )
            }
        }

        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Command") },
                singleLine = true,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                enabled = state == TermuxConnectionState.CONNECTED,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendCommand(input)
                        input = ""
                    }
                }),
            )
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendCommand(input)
                        input = ""
                    }
                },
                enabled = state == TermuxConnectionState.CONNECTED,
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun ColumnScope.CenteredMessage(message: String) {
    Box(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun TermuxConnectionState.label(): String = when (this) {
    TermuxConnectionState.DISCONNECTED -> "Disconnected"
    TermuxConnectionState.CONNECTING   -> "Connecting\u2026"
    TermuxConnectionState.CONNECTED    -> "Connected"
    TermuxConnectionState.ERROR        -> "Error"
}
