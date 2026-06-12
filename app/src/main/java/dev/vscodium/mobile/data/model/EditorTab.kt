package dev.vscodium.mobile.data.model

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue

/** An open file tab in the editor. */
class EditorTab(
    val uri: Uri,
    val name: String,
    initialContent: String,
) {
    val language: String = languageForFileName(name)

    var field = mutableStateOf(TextFieldValue(initialContent))
    var isDirty = mutableStateOf(false)
    var savedContent: String = initialContent
}

/** Best-effort language detection from file extension, used by the syntax highlighter. */
fun languageForFileName(name: String): String {
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "kt", "kts"            -> "kotlin"
        "java"                 -> "java"
        "js", "mjs", "cjs"     -> "javascript"
        "ts", "tsx"            -> "typescript"
        "py"                   -> "python"
        "c", "h"               -> "c"
        "cpp", "cc", "hpp"     -> "cpp"
        "json"                 -> "json"
        "xml"                  -> "xml"
        "html", "htm"          -> "html"
        "css"                  -> "css"
        "md", "markdown"       -> "markdown"
        "sh", "bash"           -> "shell"
        "gradle"               -> "kotlin"
        "yml", "yaml"          -> "yaml"
        else                   -> "plaintext"
    }
}
