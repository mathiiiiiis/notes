package de.mathiiis.notes.ui

import android.text.format.DateUtils
import java.text.DateFormat
import java.util.Date

object NoteText {
    private val IMAGE = Regex("""!\[([^\]]*)]\([^)]*\)""")
    private val EMPHASIS = Regex("""\*{1,3}|_{1,3}|`""")

    private fun stripImages(line: String): String =
        IMAGE.replace(line) { match ->
            val alt = match.groupValues[1].trim()
            alt.ifEmpty { "image" }
        }

    fun titleOf(content: String): String? {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() }?.trim()
        val cleaned =
            firstLine
                ?.trimStart('#', '*', '>', '-', ' ')
                ?.let { stripImages(it) }
                ?.replace(EMPHASIS, "")
                ?.trim()
        return cleaned?.takeIf { it.isNotEmpty() }
    }

    fun previewOf(content: String): String {
        val lines = content.lines()
        val titleIndex = lines.indexOfFirst { it.isNotBlank() }
        if (titleIndex == -1) return ""
        return lines
            .drop(titleIndex + 1)
            .joinToString("\n") { stripImages(it) }
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim('\n')
    }

    fun timestamp(
        millis: Long,
        now: Long = System.currentTimeMillis(),
    ): String =
        if (now - millis < DateUtils.WEEK_IN_MILLIS) {
            DateUtils
                .getRelativeTimeSpanString(millis, now, DateUtils.MINUTE_IN_MILLIS)
                .toString()
        } else {
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))
        }

    fun matches(
        content: String,
        query: String,
    ): Boolean {
        val q = query.trim()
        return q.isEmpty() || content.contains(q, ignoreCase = true)
    }
}
