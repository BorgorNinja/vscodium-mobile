package dev.vscodium.mobile.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dev.vscodium.mobile.data.model.FileNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Reads and writes files via the Storage Access Framework so the app can
 * open any folder the user grants access to (no broad storage permission
 * needed on Android 11+).
 */
class FileRepository(private val context: Context) {

    /** Lists immediate children of [parentUri], directories first, alphabetically. */
    suspend fun listChildren(parentUri: Uri, depth: Int): List<FileNode> = withContext(Dispatchers.IO) {
        val dir = DocumentFile.fromTreeUri(context, parentUri) ?: return@withContext emptyList()
        dir.listFiles()
            .filter { it.name != null }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name?.lowercase() }))
            .map { f ->
                FileNode(
                    uri         = f.uri,
                    name        = f.name ?: "",
                    isDirectory = f.isDirectory,
                    depth       = depth,
                )
            }
    }

    /** Reads the full text content of a file. */
    suspend fun readFile(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        } ?: ""
    }

    /** Overwrites the full content of a file. */
    suspend fun writeFile(uri: Uri, content: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            OutputStreamWriter(output).use { it.write(content) }
        }
    }

    /**
     * Creates an empty file named [name] inside [parentUri] and returns its
     * URI, or `null` if the parent can't be resolved or the provider refuses
     * (e.g. a file with that name already exists).
     */
    suspend fun createFile(parentUri: Uri, name: String): Uri? = withContext(Dispatchers.IO) {
        val parent = DocumentFile.fromTreeUri(context, parentUri) ?: return@withContext null
        if (parent.findFile(name) != null) return@withContext null
        // "application/octet-stream" avoids document providers rewriting or
        // appending an extension to [name] based on a guessed MIME type.
        parent.createFile("application/octet-stream", name)?.uri
    }

    /**
     * Creates a subdirectory named [name] inside [parentUri] and returns its
     * URI, or `null` if the parent can't be resolved or a file/folder with
     * that name already exists.
     */
    suspend fun createDirectory(parentUri: Uri, name: String): Uri? = withContext(Dispatchers.IO) {
        val parent = DocumentFile.fromTreeUri(context, parentUri) ?: return@withContext null
        if (parent.findFile(name) != null) return@withContext null
        parent.createDirectory(name)?.uri
    }

    /** Persists read/write access to a tree the user picked via OpenDocumentTree. */
    fun takePersistablePermission(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    fun displayNameForTree(uri: Uri): String =
        DocumentFile.fromTreeUri(context, uri)?.name ?: uri.lastPathSegment ?: "Project"
}
