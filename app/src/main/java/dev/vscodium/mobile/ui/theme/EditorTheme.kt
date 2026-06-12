package dev.vscodium.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

// ── Syntax color set ──────────────────────────────────────────────────────────

data class SyntaxColors(
    val background:     Color,
    val foreground:     Color,
    val gutter:         Color,
    val gutterText:     Color,
    val currentLine:    Color,
    val selection:      Color,
    val keyword:        Color,
    val string:         Color,
    val number:         Color,
    val comment:        Color,
    val function:       Color,
    val type:           Color,
    val operator:       Color,
)

// ── Palette model (mirrors Aura Launcher's palette pattern) ──────────────────

data class EditorPaletteEntry(
    val key:         String,
    val label:       String,
    val swatchColor: Color,
    val isDark:      Boolean,
    val syntax:      SyntaxColors,
)

val EDITOR_PALETTES: List<EditorPaletteEntry> = listOf(
    // Dark+ — VS Code default dark
    EditorPaletteEntry(
        key = "dark_plus", label = "Dark+", swatchColor = Color(0xFF1E1E1E), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF1E1E1E), foreground = Color(0xFFD4D4D4),
            gutter      = Color(0xFF1E1E1E), gutterText = Color(0xFF858585),
            currentLine = Color(0xFF2A2D2E), selection  = Color(0xFF264F78),
            keyword     = Color(0xFF569CD6), string     = Color(0xFFCE9178),
            number      = Color(0xFFB5CEA8), comment    = Color(0xFF6A9955),
            function    = Color(0xFFDCDCAA), type       = Color(0xFF4EC9B0),
            operator    = Color(0xFFD4D4D4),
        )
    ),
    // Light+ — VS Code default light
    EditorPaletteEntry(
        key = "light_plus", label = "Light+", swatchColor = Color(0xFFFFFFFF), isDark = false,
        syntax = SyntaxColors(
            background  = Color(0xFFFFFFFF), foreground = Color(0xFF000000),
            gutter      = Color(0xFFFFFFFF), gutterText = Color(0xFF237893),
            currentLine = Color(0xFFF3F3F3), selection  = Color(0xFFADD6FF),
            keyword     = Color(0xFF0000FF), string     = Color(0xFFA31515),
            number      = Color(0xFF098658), comment    = Color(0xFF008000),
            function    = Color(0xFF795E26), type       = Color(0xFF267F99),
            operator    = Color(0xFF000000),
        )
    ),
    // Monokai
    EditorPaletteEntry(
        key = "monokai", label = "Monokai", swatchColor = Color(0xFF272822), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF272822), foreground = Color(0xFFF8F8F2),
            gutter      = Color(0xFF272822), gutterText = Color(0xFF90908A),
            currentLine = Color(0xFF3E3D32), selection  = Color(0xFF49483E),
            keyword     = Color(0xFFF92672), string     = Color(0xFFE6DB74),
            number      = Color(0xFFAE81FF), comment    = Color(0xFF75715E),
            function    = Color(0xFFA6E22E), type       = Color(0xFF66D9EF),
            operator    = Color(0xFFF92672),
        )
    ),
    // Dracula
    EditorPaletteEntry(
        key = "dracula", label = "Dracula", swatchColor = Color(0xFF282A36), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF282A36), foreground = Color(0xFFF8F8F2),
            gutter      = Color(0xFF282A36), gutterText = Color(0xFF6272A4),
            currentLine = Color(0xFF44475A), selection  = Color(0xFF44475A),
            keyword     = Color(0xFFFF79C6), string     = Color(0xFFF1FA8C),
            number      = Color(0xFFBD93F9), comment    = Color(0xFF6272A4),
            function    = Color(0xFF50FA7B), type       = Color(0xFF8BE9FD),
            operator    = Color(0xFFFF79C6),
        )
    ),
    // Solarized Dark
    EditorPaletteEntry(
        key = "solarized_dark", label = "Solarized Dark", swatchColor = Color(0xFF002B36), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF002B36), foreground = Color(0xFF839496),
            gutter      = Color(0xFF073642), gutterText = Color(0xFF586E75),
            currentLine = Color(0xFF073642), selection  = Color(0xFF274642),
            keyword     = Color(0xFF859900), string     = Color(0xFF2AA198),
            number      = Color(0xFFD33682), comment    = Color(0xFF586E75),
            function    = Color(0xFF268BD2), type       = Color(0xFFB58900),
            operator    = Color(0xFF839496),
        )
    ),
    // Nord
    EditorPaletteEntry(
        key = "nord", label = "Nord", swatchColor = Color(0xFF2E3440), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF2E3440), foreground = Color(0xFFD8DEE9),
            gutter      = Color(0xFF2E3440), gutterText = Color(0xFF4C566A),
            currentLine = Color(0xFF3B4252), selection  = Color(0xFF434C5E),
            keyword     = Color(0xFF81A1C1), string     = Color(0xFFA3BE8C),
            number      = Color(0xFFB48EAD), comment    = Color(0xFF616E88),
            function    = Color(0xFF88C0D0), type       = Color(0xFF8FBCBB),
            operator    = Color(0xFF81A1C1),
        )
    ),
    // One Dark Pro
    EditorPaletteEntry(
        key = "one_dark", label = "One Dark", swatchColor = Color(0xFF282C34), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF282C34), foreground = Color(0xFFABB2BF),
            gutter      = Color(0xFF282C34), gutterText = Color(0xFF495162),
            currentLine = Color(0xFF2C313C), selection  = Color(0xFF3E4451),
            keyword     = Color(0xFFC678DD), string     = Color(0xFF98C379),
            number      = Color(0xFFD19A66), comment    = Color(0xFF5C6370),
            function    = Color(0xFF61AFEF), type       = Color(0xFFE5C07B),
            operator    = Color(0xFF56B6C2),
        )
    ),
    // Night Owl
    EditorPaletteEntry(
        key = "night_owl", label = "Night Owl", swatchColor = Color(0xFF011627), isDark = true,
        syntax = SyntaxColors(
            background  = Color(0xFF011627), foreground = Color(0xFFD6DEEB),
            gutter      = Color(0xFF011627), gutterText = Color(0xFF4B6479),
            currentLine = Color(0xFF1D3B53), selection  = Color(0xFF1D3B53),
            keyword     = Color(0xFFC792EA), string     = Color(0xFFECC48D),
            number      = Color(0xFFF78C6C), comment    = Color(0xFF637777),
            function    = Color(0xFF82AAFF), type       = Color(0xFFFFCB8B),
            operator    = Color(0xFF7FDBCA),
        )
    ),
    // Quiet Light
    EditorPaletteEntry(
        key = "quiet_light", label = "Quiet Light", swatchColor = Color(0xFFF5F5F5), isDark = false,
        syntax = SyntaxColors(
            background  = Color(0xFFF5F5F5), foreground = Color(0xFF333333),
            gutter      = Color(0xFFF5F5F5), gutterText = Color(0xFF9E9E9E),
            currentLine = Color(0xFFE4F2FF), selection  = Color(0xFFC9D0D9),
            keyword     = Color(0xFF994CC3), string     = Color(0xFFC5A332),
            number      = Color(0xFF36ACAA), comment    = Color(0xFFAAAAAA),
            function    = Color(0xFF6C7BD0), type       = Color(0xFF7A3E9D),
            operator    = Color(0xFF333333),
        )
    ),
)

fun editorPaletteForKey(key: String): EditorPaletteEntry =
    EDITOR_PALETTES.find { it.key == key } ?: EDITOR_PALETTES[0]

val LocalSyntaxColors = staticCompositionLocalOf { EDITOR_PALETTES[0].syntax }
val LocalEditorFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Monospace }

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun VSCodiumMobileTheme(
    paletteKey: String = "dark_plus",
    content: @Composable () -> Unit
) {
    val palette = editorPaletteForKey(paletteKey)
    val syntax = palette.syntax

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary    = syntax.function,
            secondary  = syntax.keyword,
            tertiary   = syntax.type,
            background = syntax.background,
            surface    = syntax.gutter,
            onBackground = syntax.foreground,
            onSurface    = syntax.foreground,
        )
    } else {
        lightColorScheme(
            primary    = syntax.function,
            secondary  = syntax.keyword,
            tertiary   = syntax.type,
            background = syntax.background,
            surface    = syntax.gutter,
            onBackground = syntax.foreground,
            onSurface    = syntax.foreground,
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalSyntaxColors provides syntax,
        LocalEditorFontFamily provides FontFamily.Monospace,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography(),
            content     = content
        )
    }
}

/** Convenience: default palette key based on system dark/light setting. */
@Composable
fun defaultPaletteKey(): String = if (isSystemInDarkTheme()) "dark_plus" else "light_plus"
