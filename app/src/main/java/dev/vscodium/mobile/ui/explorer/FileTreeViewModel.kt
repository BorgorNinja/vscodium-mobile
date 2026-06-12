package dev.vscodium.mobile.ui.explorer

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vscodium.mobile.data.model.FileNode
import dev.vscodium.mobile.data.repository.FileRepository
import kotlinx.coroutines.launch

class FileTreeViewModel(private val fileRepository: FileRepository) : ViewModel() {

    var rootUri by mutableStateOf<Uri?>(null)
        private set

    var rootName by mutableStateOf("No folder opened")
        private set

    /** Flattened, depth-aware list reflecting current expansion state — drives the LazyColumn. */
    var visibleNodes by mutableStateOf(listOf<FileNode>())
        private set

    private val childrenCache = mutableMapOf<Uri, List<FileNode>>()
    private val expanded = mutableSetOf<Uri>()

    fun openFolder(uri: Uri) {
        fileRepository.takePersistablePermission(uri)
        rootUri = uri
        rootName = fileRepository.displayNameForTree(uri)
        childrenCache.clear()
        expanded.clear()
        expanded += uri
        viewModelScope.launch {
            val children = fileRepository.listChildren(uri, depth = 0)
            childrenCache[uri] = children
            rebuild()
        }
    }

    fun toggle(node: FileNode) {
        if (!node.isDirectory) return
        if (expanded.contains(node.uri)) {
            expanded -= node.uri
            rebuild()
        } else {
            expanded += node.uri
            if (childrenCache.containsKey(node.uri)) {
                rebuild()
            } else {
                viewModelScope.launch {
                    val children = fileRepository.listChildren(node.uri, depth = node.depth + 1)
                    childrenCache[node.uri] = children
                    rebuild()
                }
            }
        }
    }

    private fun rebuild() {
        val root = rootUri ?: return
        val result = mutableListOf<FileNode>()

        fun addChildren(parent: Uri) {
            val children = childrenCache[parent] ?: return
            for (child in children) {
                result += child.copy(isExpanded = expanded.contains(child.uri))
                if (child.isDirectory && expanded.contains(child.uri)) {
                    addChildren(child.uri)
                }
            }
        }

        addChildren(root)
        visibleNodes = result
    }
}
