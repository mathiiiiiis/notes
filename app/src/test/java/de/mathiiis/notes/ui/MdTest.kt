package de.mathiiis.notes.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MdTest {
    // ==== refs ====

    @Test
    fun `refsIn finds every image ref`() {
        val body =
            """
            # title
            ![](images/a.jpg)
            some text
            ![alt text](images/b.png)
            ![](https://example.com/c.gif)
            """.trimIndent()

        assertEquals(
            setOf("images/a.jpg", "images/b.png", "https://example.com/c.gif"),
            Md.refsIn(body),
        )
    }

    @Test
    fun `refsIn ignores plain links`() {
        assertTrue(Md.refsIn("see [the docs](https://example.com)").isEmpty())
    }

    // ==== wrap ====

    @Test
    fun `wrap surrounds the selection and keeps it selected`() {
        val value = TextFieldValue("hello world", TextRange(6, 11))
        val result = Md.wrap(value, "**")

        assertEquals("hello **world**", result.text)
        assertEquals(TextRange(8, 13), result.selection)
    }

    @Test
    fun `wrap on an empty selection parks the caret between the tokens`() {
        val value = TextFieldValue("ab", TextRange(1))
        val result = Md.wrap(value, "*")

        assertEquals("a**b", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun `wrap unwraps instead of stacking a second pair`() {
        val value = TextFieldValue("hello **world**", TextRange(8, 13))
        val result = Md.wrap(value, "**")

        assertEquals("hello world", result.text)
        assertEquals(TextRange(6, 11), result.selection)
    }

    // ==== line prefix ====

    @Test
    fun `linePrefix adds the prefix to a single line`() {
        val value = TextFieldValue("todo", TextRange(4))
        val result = Md.linePrefix(value, "- ")

        assertEquals("- todo", result.text)
    }

    @Test
    fun `linePrefix covers every line the selection touches`() {
        val value = TextFieldValue("one\ntwo\nthree", TextRange(1, 9))
        val result = Md.linePrefix(value, "- ")

        assertEquals("- one\n- two\n- three", result.text)
    }

    @Test
    fun `linePrefix strips when every line already has it`() {
        val value = TextFieldValue("- one\n- two", TextRange(0, 11))
        val result = Md.linePrefix(value, "- ")

        assertEquals("one\ntwo", result.text)
    }

    @Test
    fun `linePrefix leaves untouched lines alone`() {
        val value = TextFieldValue("a\nb\nc", TextRange(0, 1))
        val result = Md.linePrefix(value, "# ")

        assertEquals("# a\nb\nc", result.text)
    }

    // ==== insert ====

    @Test
    fun `insert places the caret after the inserted text`() {
        val value = TextFieldValue("ab", TextRange(1))
        val result = Md.insert(value, "XY")

        assertEquals("aXYb", result.text)
        assertEquals(TextRange(3), result.selection)
    }
}
