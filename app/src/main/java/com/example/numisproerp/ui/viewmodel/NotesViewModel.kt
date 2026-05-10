package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.Note
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val editingNote: Note? = null,
    val errorMessage: String = ""
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllNotes().collectLatest { notes ->
                _uiState.value = _uiState.value.copy(
                    notes = notes,
                    isLoading = false
                )
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingNote = null, errorMessage = "")
    }

    fun openEditDialog(note: Note) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingNote = note, errorMessage = "")
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingNote = null, errorMessage = "")
    }

    fun saveNote(title: String, text: String, reminderDate: Long?) {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Введіть заголовок")
            return
        }
        viewModelScope.launch {
            val editing = _uiState.value.editingNote
            if (editing == null) {
                val newNote = Note(
                    noteId = "NOTE_${UUID.randomUUID().toString().take(8).uppercase()}",
                    title = title.trim(),
                    text = text.trim(),
                    reminderDate = reminderDate,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertNote(newNote)
            } else {
                repository.updateNote(
                    editing.copy(
                        title = title.trim(),
                        text = text.trim(),
                        reminderDate = reminderDate
                    )
                )
            }
            _uiState.value = _uiState.value.copy(showAddDialog = false, editingNote = null, errorMessage = "")
        }
    }

    fun toggleCompleted(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isCompleted = !note.isCompleted))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}
