package dev.vscodium.mobile.ui.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vscodium.mobile.data.model.FileNode

/** What a "New File" / "New Folder" action should be created inside. `null` means the project root. */
private data class CreationRequest(val parent: FileNode?, val isFolder: Boolean)

@Composable
fun FileExplorerPanel(
    rootName: String,
    hasRoot: Boolean,
    nodes: List<FileNode>,
    onNodeClick: (FileNode) -> Unit,
    onOpenFolderClick: () -> Unit,
    onCreateFile: (FileNode?, String) -> Unit,
    onCreateFolder: (FileNode?, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingCreation by remember { mutableStateOf<CreationRequest?>(null) }
    var menuForUri by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenFolderClick)
                .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.FolderOpen, contentDescription = "Open folder")
            Text(
                text = rootName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            if (hasRoot) {
                IconButton(
                    onClick = { pendingCreation = CreationRequest(parent = null, isFolder = false) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.NoteAdd, contentDescription = "New file", modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = { pendingCreation = CreationRequest(parent = null, isFolder = true) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.CreateNewFolder, contentDescription = "New folder", modifier = Modifier.size(18.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(nodes, key = { it.uri.toString() }) { node ->
                FileTreeRow(
                    node = node,
                    onClick = { onNodeClick(node) },
                    menuExpanded = menuForUri == node.uri.toString(),
                    onMenuOpenChange = { open -> menuForUri = if (open) node.uri.toString() else null },
                    onNewFile = { pendingCreation = CreationRequest(parent = node, isFolder = false) },
                    onNewFolder = { pendingCreation = CreationRequest(parent = node, isFolder = true) },
                )
            }
        }
    }

    pendingCreation?.let { request ->
        NameInputDialog(
            title = if (request.isFolder) "New Folder" else "New File",
            label = if (request.isFolder) "Folder name" else "File name",
            onConfirm = { name ->
                if (request.isFolder) onCreateFolder(request.parent, name) else onCreateFile(request.parent, name)
                pendingCreation = null
            },
            onDismiss = { pendingCreation = null },
        )
    }
}

@Composable
private fun FileTreeRow(
    node: FileNode,
    onClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (12 + node.depth * 16).dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
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
            modifier = Modifier.weight(1f),
        )
        // Folders get a small overflow menu for creating new files/folders inside them.
        if (node.isDirectory) {
            Box {
                IconButton(onClick = { onMenuOpenChange(true) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More actions", modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { onMenuOpenChange(false) }) {
                    DropdownMenuItem(
                        text = { Text("New File") },
                        onClick = {
                            onMenuOpenChange(false)
                            onNewFile()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        onClick = {
                            onMenuOpenChange(false)
                            onNewFolder()
                        },
                    )
                }
            }
        }
    }
}

/** Simple text-entry dialog used for naming a new file or folder. */
@Composable
private fun NameInputDialog(
    title: String,
    label: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank(),
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
