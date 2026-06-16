package de.mathiiis.notes.ui

import java.text.DateFormat
import java.util.Date

/**
 * Display helpers for note rows
 *
 * > titleOf takes first non blank line, trimmed, or fallback when empty
 * > previewOf takes remaining lines joined into single spaced line
 * > shortDate formats millis timestamp using device locale
 */
object NoteText {
    private const val UNTITLED = "Untitled note"
    private val IMAGE = Regex("""!\[([^\]]*)\]\([^)]*\)""")

    private fun stripImages(line: String): String =
        IMAGE.replace(line) { match ->
            val alt = match.groupValues[1].trim()
            alt.ifEmpty { "image" }
        }

    fun titleOf(content: String): String {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() }?.trim()
        val cleaned =
            firstLine
                ?.trimStart('#', '*', '>', '-', ' ')
                ?.let { stripImages(it) }
                ?.replace(Regex("""\*{1,3}|_{1,3}|`"""), "")
                ?.trim()
        return if (cleaned.isNullOrEmpty()) UNTITLED else cleaned
    }

    fun previewOf(content: String): String {
        val lines = content.lines()
        val titleIndex = lines.indexOfFirst { it.isNotBlank() }
        if (titleIndex == -1) return ""
        return lines.drop(titleIndex + 1)
            .joinToString("\n") { stripImages(it) }
            .replace(Regex("\\n{3,}"), "\n\n")
            .trimEnd('\n')
    }

    fun shortDate(millis: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))
}
