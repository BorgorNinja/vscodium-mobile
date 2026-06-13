package dev.vscodium.mobile.ui.statusbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vscodium.mobile.data.model.EditorTab

/**
 * VS Code's signature blue status bar, trimmed down to what's useful on a
 * phone: the active file's dirty state, cursor position, encoding, line
 * ending and language mode — mirroring the bottom bar of desktop VSCodium.
 *
 * The bar sits in the Scaffold's bottomBar slot, which (in an edge-to-edge
 * layout) is drawn behind the system navigation bar by default. It applies
 * the navigation bar inset itself so it never ends up underneath the
 * gesture/3-button nav area, and unions that with the IME inset so that when
 * the on-screen keyboard appears, the bar rides up to sit just above it
 * instead of staying pinned under (now hidden) navigation buttons.
 */
@Composable
fun StatusBar(activeTab: EditorTab?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF007ACC))
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .height(28.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = activeTab?.let { "${it.name}${if (it.isDirty.value) " \u25CF" else ""}" }
                ?: "VSCodium Mobile",
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
        )

        if (activeTab != null) {
            val (line, col) = cursorPosition(activeTab.field.value)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Ln $line, Col $col", color = Color.White, fontSize = 12.sp)
                Text("UTF-8", color = Color.White, fontSize = 12.sp)
                Text("LF", color = Color.White, fontSize = 12.sp)
                Text(languageLabel(activeTab.language), color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

/** 1-based line/column of the cursor (selection end), VS Code style. */
private fun cursorPosition(value: TextFieldValue): Pair<Int, Int> {
    val cursor = value.selection.end.coerceIn(0, value.text.length)
    var line = 1
    var lastNewline = -1
    for (i in 0 until cursor) {
        if (value.text[i] == '\n') {
            line++
            lastNewline = i
        }
    }
    val col = cursor - lastNewline
    return line to col
}

private fun languageLabel(language: String): String = when (language) {
    "kotlin"     -> "Kotlin"
    "java"       -> "Java"
    "javascript" -> "JavaScript"
    "typescript" -> "TypeScript"
    "python"     -> "Python"
    "c"          -> "C"
    "cpp"        -> "C++"
    "json"       -> "JSON"
    "xml"        -> "XML"
    "html"       -> "HTML"
    "css"        -> "CSS"
    "markdown"   -> "Markdown"
    "shell"      -> "Shell Script"
    "yaml"       -> "YAML"
    else         -> "Plain Text"
}
