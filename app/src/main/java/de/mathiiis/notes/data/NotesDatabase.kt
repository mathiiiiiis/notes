package de.mathiiis.notes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app sqlite database
 *
 * > schema version starts at 1
 * > get returns process wide singleton, opened lazily on first use
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: NotesDatabase? = null

        fun get(context: Context): NotesDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): NotesDatabase =
            Room
                .databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes.db",
                ).build()
    }
}
