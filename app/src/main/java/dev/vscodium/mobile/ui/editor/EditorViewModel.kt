package dev.vscodium.mobile.ui.editor

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vscodium.mobile.data.model.EditorTab
import dev.vscodium.mobile.data.repository.FileRepository
import kotlinx.coroutines.launch

class EditorViewModel(private val fileRepository: FileRepository) : ViewModel() {

    var tabs by mutableStateOf(listOf<EditorTab>())
        private set

    var activeIndex by mutableStateOf(-1)
        private set

    val activeTab: EditorTab?
        get() = tabs.getOrNull(activeIndex)

    /** Opens [uri] in a new tab, or switches to it if already open. */
    fun openFile(uri: Uri, name: String) {
        val existingIndex = tabs.indexOfFirst { it.uri == uri }
        if (existingIndex >= 0) {
            activeIndex = existingIndex
            return
        }
        viewModelScope.launch {
            val content = fileRepository.readFile(uri)
            val tab = EditorTab(uri, name, content)
            tabs = tabs + tab
            activeIndex = tabs.lastIndex
        }
    }

    fun selectTab(index: Int) {
        if (index in tabs.indices) activeIndex = index
    }

    fun closeTab(index: Int) {
        if (index !in tabs.indices) return
        val newTabs = tabs.toMutableList().also { it.removeAt(index) }
        tabs = newTabs
        activeIndex = when {
            newTabs.isEmpty() -> -1
            activeIndex > index -> activeIndex - 1
            activeIndex >= newTabs.size -> newTabs.lastIndex
            else -> activeIndex
        }
    }

    fun onContentChanged(tab: EditorTab, newValue: TextFieldValue) {
        tab.field.value = newValue
        tab.isDirty.value = newValue.text != tab.savedContent
    }

    fun saveActiveTab() {
        val tab = activeTab ?: return
        viewModelScope.launch {
            fileRepository.writeFile(tab.uri, tab.field.value.text)
            tab.savedContent = tab.field.value.text
            tab.isDirty.value = false
        }
    }

    fun saveAll() {
        val current = tabs
        viewModelScope.launch {
            for (tab in current) {
                if (tab.isDirty.value) {
                    fileRepository.writeFile(tab.uri, tab.field.value.text)
                    tab.savedContent = tab.field.value.text
                    tab.isDirty.value = false
                }
            }
        }
    }
}
