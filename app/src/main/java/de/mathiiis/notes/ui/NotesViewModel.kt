package de.mathiiis.notes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.mathiiis.notes.data.Note
import de.mathiiis.notes.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    application: Application,
    private val repo: NotesRepository,
) : AndroidViewModel(application) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val total: StateFlow<Int> =
        repo.notes
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val notes: StateFlow<List<Note>> =
        repo.notes
            .combine(_query) { notes, q ->
                if (q.isBlank()) notes else notes.filter { NoteText.matches(it.content, q) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    init {
        sweepOrphanImages()
    }

    // ==== search ====

    fun setQuery(value: String) {
        _query.value = value
    }

    // ==== actions ====

    suspend fun createNow(content: String): Long = repo.create(content)

    fun createDetached(content: String) {
        viewModelScope.launch { repo.create(content) }
    }

    suspend fun load(id: Long): Note? = repo.load(id)

    fun save(
        id: Long,
        content: String,
    ) {
        viewModelScope.launch { repo.save(id, content) }
    }

    fun setPinned(
        id: Long,
        pinned: Boolean,
    ) {
        viewModelScope.launch { repo.setPinned(id, pinned) }
    }

    suspend fun delete(id: Long): Note? = repo.delete(id)

    fun restore(note: Note) {
        viewModelScope.launch { repo.restore(note) }
    }

    private fun sweepOrphanImages() {
        viewModelScope.launch {
            val referenced = Md.refsIn(repo.allContent())
            ImageStore.sweepOrphans(getApplication<Application>(), referenced)
        }
    }

    class Factory(
        private val application: Application,
        private val repo: NotesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NotesViewModel(application, repo) as T
    }
}
