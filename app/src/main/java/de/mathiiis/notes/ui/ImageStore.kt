package de.mathiiis.notes.ui

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object ImageStore {
    private const val DIR = "images"
    private const val PREFIX = "$DIR/"

    fun dir(context: Context): File = File(context.filesDir, DIR)

    suspend fun persist(
        context: Context,
        uri: Uri,
    ): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = dir(context).apply { mkdirs() }
                val name = UUID.randomUUID().toString() + extensionFor(context, uri)
                val target = File(dir, name)

                val copied =
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                        true
                    } ?: false

                if (!copied) {
                    target.delete()
                    null
                } else {
                    PREFIX + name
                }
            }.getOrNull()
        }

    fun resolve(
        context: Context,
        ref: String,
    ): String =
        if (ref.startsWith(PREFIX)) {
            "file://" + File(context.filesDir, ref).absolutePath
        } else {
            ref
        }

    fun isManaged(ref: String): Boolean = ref.startsWith(PREFIX)

    suspend fun sweepOrphans(
        context: Context,
        referenced: Set<String>,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val dir = dir(context)
            if (!dir.isDirectory) return@runCatching
            val keep = referenced.filter { isManaged(it) }.map { it.removePrefix(PREFIX) }.toSet()
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.name !in keep) file.delete()
            }
        }
    }

    private fun extensionFor(
        context: Context,
        uri: Uri,
    ): String =
        when (context.contentResolver.getType(uri)) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            "image/heic", "image/heif" -> ".heic"
            else -> ".jpg"
        }
}
