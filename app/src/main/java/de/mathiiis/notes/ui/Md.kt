package de.mathiiis.notes.ui

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object Md {
    private val IMAGE = Regex("""!\[[^\]]*]\(([^)\s]+)[^)]*\)""")

    fun refsIn(content: String): Set<String> = IMAGE.findAll(content).map { it.groupValues[1] }.toSet()

    fun refsIn(contents: List<String>): Set<String> = contents.flatMapTo(mutableSetOf()) { refsIn(it) }

    fun forRender(
        context: Context,
        content: String,
    ): String =
        IMAGE.replace(content) { match ->
            val ref = match.groupValues[1]
            if (ImageStore.isManaged(ref)) {
                match.value.replace(ref, ImageStore.resolve(context, ref))
            } else {
                match.value
            }
        }

    fun wrap(
        value: TextFieldValue,
        token: String,
    ): TextFieldValue {
        val text = value.text
        val start = value.selection.min.coerceIn(0, text.length)
        val end = value.selection.max.coerceIn(0, text.length)
        val selected = text.substring(start, end)

        val outerStart = start - token.length
        val alreadyWrapped =
            outerStart >= 0 &&
                end + token.length <= text.length &&
                text.startsWith(token, outerStart) &&
                text.startsWith(token, end)

        if (alreadyWrapped) {
            val stripped =
                text.substring(0, outerStart) +
                    selected +
                    text.substring(end + token.length)
            return TextFieldValue(
                text = stripped,
                selection = TextRange(outerStart, outerStart + selected.length),
            )
        }

        val next = text.substring(0, start) + token + selected + token + text.substring(end)
        return if (selected.isEmpty()) {
            TextFieldValue(next, TextRange(start + token.length))
        } else {
            TextFieldValue(next, TextRange(start + token.length, end + token.length))
        }
    }

    fun linePrefix(
        value: TextFieldValue,
        prefix: String,
    ): TextFieldValue {
        val text = value.text
        val selStart = value.selection.min.coerceIn(0, text.length)
        val selEnd = value.selection.max.coerceIn(0, text.length)

        val lineStart =
            text
                .lastIndexOf('\n', (selStart - 1).coerceAtLeast(0))
                .let { if (it == -1 || selStart == 0) 0 else it + 1 }
        val lineEndRaw = text.indexOf('\n', selEnd)
        val lineEnd = if (lineEndRaw == -1) text.length else lineEndRaw

        val block = text.substring(lineStart, lineEnd)
        val lines = block.split('\n')
        val allPrefixed = lines.all { it.trimStart().startsWith(prefix.trim()) && it.isNotBlank() }

        val rebuiltLines =
            lines.map { line ->
                if (allPrefixed) {
                    line.replaceFirst(Regex("^\\s*" + Regex.escape(prefix.trim()) + "\\s?"), "")
                } else {
                    prefix + line
                }
            }
        val rebuilt = rebuiltLines.joinToString("\n")

        val next = text.substring(0, lineStart) + rebuilt + text.substring(lineEnd)
        val delta = rebuilt.length - block.length

        val firstDelta = rebuiltLines.first().length - lines.first().length
        return TextFieldValue(
            text = next,
            selection =
                TextRange(
                    (selStart + firstDelta).coerceIn(lineStart, next.length),
                    (selEnd + delta).coerceIn(0, next.length),
                ),
        )
    }

    fun insert(
        value: TextFieldValue,
        insert: String,
    ): TextFieldValue {
        val start = value.selection.max.coerceIn(0, value.text.length)
        val next = value.text.substring(0, start) + insert + value.text.substring(start)
        return TextFieldValue(next, TextRange(start + insert.length))
    }
}
