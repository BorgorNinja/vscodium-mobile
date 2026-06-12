package dev.vscodium.mobile.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import dev.vscodium.mobile.ui.theme.SyntaxColors

private val KEYWORDS_BY_LANGUAGE: Map<String, Set<String>> = mapOf(
    "kotlin" to setOf(
        "fun", "val", "var", "if", "else", "when", "for", "while", "do", "return",
        "class", "object", "interface", "is", "as", "in", "!in", "!is", "null", "true", "false",
        "package", "import", "private", "public", "protected", "internal", "override",
        "companion", "data", "sealed", "enum", "abstract", "open", "const", "suspend",
        "this", "super", "try", "catch", "finally", "throw", "lateinit", "init", "by",
    ),
    "java" to setOf(
        "public", "private", "protected", "class", "interface", "extends", "implements",
        "static", "final", "void", "int", "long", "double", "float", "boolean", "char",
        "new", "return", "if", "else", "for", "while", "do", "switch", "case", "break",
        "continue", "try", "catch", "finally", "throw", "throws", "import", "package",
        "this", "super", "null", "true", "false", "enum", "abstract",
    ),
    "javascript" to setOf(
        "function", "const", "let", "var", "if", "else", "for", "while", "do", "return",
        "class", "extends", "new", "this", "super", "import", "export", "default", "from",
        "try", "catch", "finally", "throw", "async", "await", "typeof", "instanceof",
        "null", "undefined", "true", "false", "switch", "case", "break", "continue",
    ),
    "typescript" to setOf(
        "function", "const", "let", "var", "if", "else", "for", "while", "do", "return",
        "class", "extends", "implements", "interface", "type", "new", "this", "super",
        "import", "export", "default", "from", "try", "catch", "finally", "throw",
        "async", "await", "typeof", "instanceof", "null", "undefined", "true", "false",
        "public", "private", "protected", "readonly", "enum", "as",
    ),
    "python" to setOf(
        "def", "class", "if", "elif", "else", "for", "while", "return", "import", "from",
        "as", "try", "except", "finally", "raise", "with", "lambda", "yield", "pass",
        "break", "continue", "global", "nonlocal", "is", "in", "not", "and", "or",
        "None", "True", "False", "self", "async", "await",
    ),
    "c" to setOf(
        "int", "long", "double", "float", "char", "void", "if", "else", "for", "while",
        "do", "return", "struct", "typedef", "static", "const", "switch", "case",
        "break", "continue", "sizeof", "enum", "union", "unsigned", "signed",
    ),
    "cpp" to setOf(
        "int", "long", "double", "float", "char", "void", "if", "else", "for", "while",
        "do", "return", "struct", "class", "typedef", "static", "const", "switch", "case",
        "break", "continue", "sizeof", "enum", "namespace", "using", "template", "public",
        "private", "protected", "new", "delete", "this", "virtual", "override", "auto",
    ),
    "shell" to setOf(
        "if", "then", "else", "elif", "fi", "for", "while", "do", "done", "case", "esac",
        "function", "return", "export", "local", "echo", "in",
    ),
    "yaml" to setOf("true", "false", "null"),
)

private val NUMBER_REGEX = Regex("\\b0[xX][0-9a-fA-F]+\\b|\\b\\d+(\\.\\d+)?[fFLl]?\\b")

// Matches strings (single/double/triple quoted), line comments, block comments.
private val STRING_REGEX = Regex("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'")
private val LINE_COMMENT_REGEX = Regex("//[^\n]*|#[^\n]*")
private val BLOCK_COMMENT_REGEX = Regex("/\\*[\\s\\S]*?\\*/")
private val FUNCTION_CALL_REGEX = Regex("\\b([A-Za-z_][A-Za-z0-9_]*)\\s*(?=\\()")
private val TYPE_REGEX = Regex("\\b([A-Z][A-Za-z0-9_]*)\\b")

private data class Token(val range: IntRange, val style: SpanStyle)

/**
 * Lightweight regex-based highlighter — not a full language parser, but
 * covers keywords, strings, comments, numbers, function calls and
 * capitalized type names well enough for readable code on a phone screen.
 */
fun highlight(text: String, language: String, colors: SyntaxColors): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    val tokens = mutableListOf<Token>()
    val taken = BooleanArray(text.length)

    fun addTokens(regex: Regex, style: SpanStyle, groupIndex: Int = 0) {
        for (match in regex.findAll(text)) {
            val group = match.groups[groupIndex] ?: continue
            val range = group.range
            if (range.first > range.last) continue
            if ((range.first until range.last + 1).any { taken[it] }) continue
            tokens += Token(range, style)
            for (i in range) taken[i] = true
        }
    }

    // Order matters: comments and strings first so keyword/number matching
    // inside them is suppressed.
    val isLineCommentLang = language != "c" && language != "cpp" && language != "java" &&
        language != "kotlin" && language != "javascript" && language != "typescript"

    if (language == "c" || language == "cpp" || language == "java" ||
        language == "kotlin" || language == "javascript" || language == "typescript") {
        addTokens(BLOCK_COMMENT_REGEX, SpanStyle(color = colors.comment))
    }
    addTokens(LINE_COMMENT_REGEX, SpanStyle(color = colors.comment))
    addTokens(STRING_REGEX, SpanStyle(color = colors.string))
    addTokens(NUMBER_REGEX, SpanStyle(color = colors.number))

    val keywords = KEYWORDS_BY_LANGUAGE[language]
    if (keywords != null) {
        val pattern = Regex("\\b(${keywords.joinToString("|") { Regex.escape(it) }})\\b")
        addTokens(pattern, SpanStyle(color = colors.keyword, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
    }

    addTokens(FUNCTION_CALL_REGEX, SpanStyle(color = colors.function), groupIndex = 1)
    addTokens(TYPE_REGEX, SpanStyle(color = colors.type), groupIndex = 1)

    return AnnotatedString.Builder(text).apply {
        for (token in tokens.sortedBy { it.range.first }) {
            addStyle(token.style, token.range.first, token.range.last + 1)
        }
    }.toAnnotatedString()
}
