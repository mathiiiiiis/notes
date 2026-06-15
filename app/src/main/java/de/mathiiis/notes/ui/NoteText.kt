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

    fun titleOf(content: String): String {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() }?.trim()
        return if (firstLine.isNullOrEmpty()) UNTITLED else firstLine
    }

    fun previewOf(content: String): String {
        val lines = content.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        return if (lines.size <= 1) "" else lines.drop(1).joinToString(" ")
    }

    fun shortDate(millis: Long): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))
}
