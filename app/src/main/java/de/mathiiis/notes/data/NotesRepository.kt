package de.mathiiis.notes.data

import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val dao: NoteDao,
) {
    val notes: Flow<List<Note>> = dao.observeAll()

    suspend fun create(initial: String = ""): Long = dao.insert(Note(content = initial))

    suspend fun load(id: Long): Note? = dao.getById(id)

    suspend fun save(
        id: Long,
        content: String,
    ) {
        dao.updateContent(id, content, System.currentTimeMillis())
    }

    suspend fun setPinned(
        id: Long,
        pinned: Boolean,
    ) {
        dao.updatePinned(id, pinned)
    }

    suspend fun delete(id: Long): Note? {
        val existing = dao.getById(id) ?: return null
        dao.deleteById(id)
        return existing
    }

    suspend fun restore(note: Note) {
        dao.insert(note)
    }

    suspend fun allContent(): List<String> = dao.allContent()
}
