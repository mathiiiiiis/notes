package de.mathiiis.notes.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTextTest {
    // ==== titles ====

    @Test
    fun `titleOf takes the first non blank line`() {
        assertEquals("Groceries", NoteText.titleOf("\n\nGroceries\nmilk\neggs"))
    }

    @Test
    fun `titleOf strips markdown syntax`() {
        assertEquals("Heading", NoteText.titleOf("## **Heading**"))
    }

    @Test
    fun `titleOf falls back to image alt text`() {
        assertEquals("a chart", NoteText.titleOf("![a chart](images/x.png)"))
    }

    @Test
    fun `titleOf is null for a blank body`() {
        assertNull(NoteText.titleOf("   \n\n  "))
        assertNull(NoteText.titleOf(""))
    }

    // ==== previews ====

    @Test
    fun `previewOf skips the title line`() {
        assertEquals("milk\neggs", NoteText.previewOf("Groceries\nmilk\neggs"))
    }

    @Test
    fun `previewOf collapses long blank runs`() {
        assertEquals("a\n\nb", NoteText.previewOf("title\na\n\n\n\n\nb"))
    }

    @Test
    fun `previewOf is empty for a title only note`() {
        assertEquals("", NoteText.previewOf("just a title"))
    }

    // ==== search ====

    @Test
    fun `matches is case insensitive`() {
        assertTrue(NoteText.matches("Buy Milk", "milk"))
        assertTrue(NoteText.matches("Buy Milk", "MILK"))
        assertFalse(NoteText.matches("Buy Milk", "bread"))
    }

    @Test
    fun `matches treats a blank query as everything`() {
        assertTrue(NoteText.matches("anything", "   "))
    }
}
