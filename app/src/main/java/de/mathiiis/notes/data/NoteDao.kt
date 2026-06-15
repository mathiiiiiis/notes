package de.mathiiis.notes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data access for notes
 *
 * > observeAll streams full list newest first, list screen collects this
 * > getById loads one note for editor
 * > insert creates blank note and returns new id
 * > update persists edits to existing note
 * > delete removes note
 */
@Dao
interface NoteDao {
    // ==== reads ====

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    // ==== writes ====

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
