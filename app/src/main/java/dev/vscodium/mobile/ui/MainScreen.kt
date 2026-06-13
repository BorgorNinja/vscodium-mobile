package dev.vscodium.mobile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vscodium.mobile.data.model.EditorSettings
import dev.vscodium.mobile.data.repository.FileRepository
import dev.vscodium.mobile.data.repository.SettingsRepository
import dev.vscodium.mobile.ui.editor.CodeEditorField
import dev.vscodium.mobile.ui.editor.EditorViewModel
import dev.vscodium.mobile.ui.explorer.FileExplorerPanel
import dev.vscodium.mobile.ui.explorer.FileTreeViewModel
import dev.vscodium.mobile.ui.navigation.EditorTabBar
import dev.vscodium.mobile.ui.settings.SettingsScreen
import dev.vscodium.mobile.ui.statusbar.StatusBar
import kotlinx.coroutines.launch

private enum class RightPanel { NONE, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val fileRepository = remember(context) { FileRepository(context) }
    val settingsRepository = remember(context) { SettingsRepository(context) }

    val fileTreeViewModel: FileTreeViewModel = viewModel(factory = factoryFor { FileTreeViewModel(fileRepository) })
    val editorViewModel: EditorViewModel = viewModel(factory = factoryFor { EditorViewModel(fileRepository) })

    val settings by settingsRepository.settingsFlow.collectAsState(initial = EditorSettings())
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var rightPanel by remember { mutableStateOf(RightPanel.NONE) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            fileTreeViewModel.openFolder(uri)
            scope.launch { settingsRepository.setLastFolderUri(uri.toString()) }
        }
    }

    // Restore the last opened folder, if any.
    LaunchedEffect(settings.lastFolderUri) {
        val saved = settings.lastFolderUri
        if (saved != null && fileTreeViewModel.rootUri == null) {
            fileTreeViewModel.openFolder(Uri.parse(saved))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                FileExplorerPanel(
                    rootName = fileTreeViewModel.rootName,
                    hasRoot = fileTreeViewModel.rootUri != null,
                    nodes = fileTreeViewModel.visibleNodes,
                    onNodeClick = { node ->
                        if (node.isDirectory) {
                            fileTreeViewModel.toggle(node)
                        } else {
                            editorViewModel.openFile(node.uri, node.name)
                            scope.launch { drawerState.close() }
                        }
                    },
                    onOpenFolderClick = { folderPicker.launch(null) },
                    onCreateFile = { parent, name ->
                        fileTreeViewModel.createFile(parent, name) { uri ->
                            editorViewModel.openFile(uri, name)
                            scope.launch { drawerState.close() }
                        }
                    },
                    onCreateFolder = { parent, name -> fileTreeViewModel.createFolder(parent, name) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("VSCodium Mobile") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Explorer")
                        }
                    },
                    actions = {
                        IconButton(onClick = { folderPicker.launch(null) }) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = "Open folder")
                        }
                        IconButton(onClick = { editorViewModel.saveActiveTab() }) {
                            Icon(Icons.Filled.Save, contentDescription = "Save")
                        }
                        IconButton(onClick = {
                            rightPanel = if (rightPanel == RightPanel.SETTINGS) RightPanel.NONE else RightPanel.SETTINGS
                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = {
                // VS Code's signature blue status bar, present across both the
                // editor and settings panels.
                StatusBar(activeTab = editorViewModel.activeTab)
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (rightPanel == RightPanel.SETTINGS) {
                    SettingsScreen(
                        settings = settings,
                        onPaletteSelected = { key -> scope.launch { settingsRepository.setPalette(key) } },
                        onFontSizeChange = { size -> scope.launch { settingsRepository.setFontSize(size) } },
                        onWordWrapChange = { enabled -> scope.launch { settingsRepository.setWordWrap(enabled) } },
                        onShowLineNumbersChange = { enabled -> scope.launch { settingsRepository.setShowLineNumbers(enabled) } },
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (editorViewModel.tabs.isNotEmpty()) {
                            EditorTabBar(
                                tabs = editorViewModel.tabs,
                                activeIndex = editorViewModel.activeIndex,
                                onSelect = editorViewModel::selectTab,
                                onClose = editorViewModel::closeTab,
                            )
                            HorizontalDivider()
                        }

                        val activeTab = editorViewModel.activeTab
                        if (activeTab != null) {
                            CodeEditorField(
                                value = activeTab.field.value,
                                onValueChange = { editorViewModel.onContentChanged(activeTab, it) },
                                language = activeTab.language,
                                fontSize = settings.fontSize,
                                showLineNumbers = settings.showLineNumbers,
                                wordWrap = settings.wordWrap,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            EmptyState(onOpenFolderClick = { folderPicker.launch(null) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onOpenFolderClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Open a folder to start editing",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            androidx.compose.material3.Button(onClick = onOpenFolderClick) {
                Text("Open Folder")
            }
        }
    }
}

/** Small helper to build a [androidx.lifecycle.ViewModelProvider.Factory] from a creation lambda. */
private inline fun <reified VM : androidx.lifecycle.ViewModel> factoryFor(
    crossinline create: () -> VM
): androidx.lifecycle.ViewModelProvider.Factory =
    object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = create() as T
    }
