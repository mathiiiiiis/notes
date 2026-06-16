package de.mathiiis.notes.ui

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Copies picked images into app private storage so notes reference a stable path.
 *
 * > persist streams the picked content uri into filesDir/images and returns a file uri string
 * > the returned value is embedded into the note markdown as an image link
 * > runs on the io dispatcher, returns null when the copy fails
 */
object ImageStore {

    suspend fun persist(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "images").apply { mkdirs() }
            val target = File(dir, UUID.randomUUID().toString() + extensionFor(context, uri))
            val ok = context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false
            if (!ok) return@runCatching null
            "file://" + target.absolutePath
        }.getOrNull()
    }

    private fun extensionFor(context: Context, uri: Uri): String =
        when (context.contentResolver.getType(uri)) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            else -> ".jpg"
        }
}
