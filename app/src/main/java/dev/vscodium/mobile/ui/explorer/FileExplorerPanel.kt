package dev.vscodium.mobile.ui.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vscodium.mobile.data.model.FileNode

@Composable
fun FileExplorerPanel(
    rootName: String,
    nodes: List<FileNode>,
    onNodeClick: (FileNode) -> Unit,
    onOpenFolderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenFolderClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.FolderOpen, contentDescription = "Open folder")
            Text(
                text = rootName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(nodes, key = { it.uri.toString() }) { node ->
                FileTreeRow(node = node, onClick = { onNodeClick(node) })
            }
        }
    }
}

@Composable
private fun FileTreeRow(node: FileNode, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (12 + node.depth * 16).dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            node.isDirectory && node.isExpanded -> Icon(Icons.Filled.ExpandMore, null)
            node.isDirectory -> Icon(Icons.Filled.ExpandLess, null)
            else -> Icon(Icons.Filled.Description, null)
        }
        if (node.isDirectory) {
            Icon(Icons.Filled.Folder, null)
        }
        Text(
            text = node.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
    }
}
