package dev.vscodium.mobile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.vscodium.mobile.data.model.EditorSettings
import dev.vscodium.mobile.ui.theme.EDITOR_PALETTES

@Composable
fun SettingsScreen(
    settings: EditorSettings,
    onPaletteSelected: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onWordWrapChange: (Boolean) -> Unit,
    onShowLineNumbersChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Color Theme", style = MaterialTheme.typography.titleMedium)
        Text(
            "Editor color schemes inspired by popular VS Code themes",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(EDITOR_PALETTES) { entry ->
                val selected = entry.key == settings.paletteKey
                Column(
                    modifier = Modifier.clickable { onPaletteSelected(entry.key) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(entry.swatchColor)
                            .border(2.dp, borderColor, CircleShape)
                    )
                    Text(entry.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Font Size: ${settings.fontSize}sp", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = settings.fontSize.toFloat(),
            onValueChange = { onFontSizeChange(it.toInt()) },
            valueRange = 10f..24f,
            steps = 13,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Word Wrap", style = MaterialTheme.typography.titleMedium)
            Switch(checked = settings.wordWrap, onCheckedChange = onWordWrapChange)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Line Numbers", style = MaterialTheme.typography.titleMedium)
            Switch(checked = settings.showLineNumbers, onCheckedChange = onShowLineNumbersChange)
        }
    }
}
