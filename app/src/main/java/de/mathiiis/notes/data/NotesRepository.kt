package de.mathiiis.notes.data

import kotlinx.coroutines.flow.Flow

/**
 * Thin layer over dao so viewmodel does not touch Room directly
 *
 * > notes is live ordered list
 * > create makes an empty note and hands back id for navigation
 * > save stamps updatedAt and writes new content
 * > delete loads then removes matching row
 */
class NotesRepository(
    private val dao: NoteDao,
) {
    val notes: Flow<List<Note>> = dao.observeAll()

    suspend fun create(): Long = dao.insert(Note())

    suspend fun load(id: Long): Note? = dao.getById(id)

    suspend fun save(
        id: Long,
        content: String,
    ) {
        val existing = dao.getById(id) ?: return
        dao.update(existing.copy(content = content, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: Long) {
        val existing = dao.getById(id) ?: return
        dao.delete(existing)
    }
}
