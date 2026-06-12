package dev.vscodium.mobile.data.model

import android.net.Uri

/** A single entry in the file explorer tree. */
data class FileNode(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val depth: Int = 0,
    val isExpanded: Boolean = false,
    val children: List<FileNode>? = null,
)
