package dev.vscodium.mobile.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.vscodium.mobile.data.model.EditorSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vscodium_mobile_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PALETTE   = stringPreferencesKey("palette_key")
        val FONT_SIZE = intPreferencesKey("font_size")
        val TAB_SIZE  = intPreferencesKey("tab_size")
        val LINE_NUMS = booleanPreferencesKey("show_line_numbers")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val LAST_DIR  = stringPreferencesKey("last_folder_uri")
    }

    val settingsFlow: Flow<EditorSettings> = context.dataStore.data.map { prefs ->
        EditorSettings(
            paletteKey      = prefs[Keys.PALETTE] ?: "dark_plus",
            fontSize        = prefs[Keys.FONT_SIZE] ?: 14,
            tabSize         = prefs[Keys.TAB_SIZE] ?: 4,
            showLineNumbers = prefs[Keys.LINE_NUMS] ?: true,
            wordWrap        = prefs[Keys.WORD_WRAP] ?: false,
            lastFolderUri   = prefs[Keys.LAST_DIR],
        )
    }

    suspend fun setPalette(key: String) {
        context.dataStore.edit { it[Keys.PALETTE] = key }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun setTabSize(size: Int) {
        context.dataStore.edit { it[Keys.TAB_SIZE] = size }
    }

    suspend fun setShowLineNumbers(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LINE_NUMS] = enabled }
    }

    suspend fun setWordWrap(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WORD_WRAP] = enabled }
    }

    suspend fun setLastFolderUri(uri: String?) {
        context.dataStore.edit {
            if (uri == null) it.remove(Keys.LAST_DIR) else it[Keys.LAST_DIR] = uri
        }
    }
}
