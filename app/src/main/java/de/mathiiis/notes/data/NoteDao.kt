package de.mathiiis.notes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // ==== reads ====

    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    @Query("SELECT content FROM notes")
    suspend fun allContent(): List<String>

    // ==== writes ====

    @Insert
    suspend fun insert(note: Note): Long

    @Query("UPDATE notes SET content = :content, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateContent(
        id: Long,
        content: String,
        updatedAt: Long,
    )

    @Query("UPDATE notes SET pinned = :pinned WHERE id = :id")
    suspend fun updatePinned(
        id: Long,
        pinned: Boolean,
    )

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
