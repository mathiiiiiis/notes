package de.mathiiis.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.mathiiis.notes.data.Note
import de.mathiiis.notes.data.NotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds note list and editor write actions
 *
 * > notes is list screen state, cold flow promoted to StateFlow
 * > create runs on coroutine and reports new id back through onCreated
 * > save and delete are fire and forget against repository
 */
class NotesViewModel(private val repo: NotesRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repo.notes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    // ==== actions ====

    fun create(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            onCreated(repo.create())
        }
    }

    suspend fun load(id: Long): Note? = repo.load(id)

    fun save(id: Long, content: String) {
        viewModelScope.launch { repo.save(id, content) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    /**
     * Builds viewmodel with its repository dependency
     *
     * > used from setContent since there is no dependency injection framework here
     */
    class Factory(private val repo: NotesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotesViewModel(repo) as T
    }
}
