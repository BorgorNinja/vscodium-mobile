package dev.vscodium.mobile.data.model

data class EditorSettings(
    val paletteKey:      String  = "dark_plus",
    val fontSize:        Int     = 14,
    val tabSize:         Int     = 4,
    val showLineNumbers: Boolean = true,
    val wordWrap:        Boolean = false,
    val lastFolderUri:   String? = null,
)
