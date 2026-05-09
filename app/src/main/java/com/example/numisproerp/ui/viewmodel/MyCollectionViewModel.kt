package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.CollectionItem
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MyCollectionUiState(
    val items: List<CollectionItem> = emptyList(),
    val isLoading: Boolean = false,
    val totalEstimatedValue: Double = 0.0,
    val showAddDialog: Boolean = false,
    val editingItem: CollectionItem? = null,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class MyCollectionViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyCollectionUiState())
    val uiState: StateFlow<MyCollectionUiState> = _uiState.asStateFlow()

    init {
        observeCollection()
    }

    private fun observeCollection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllCollectionItems().collectLatest { items ->
                val total = items.sumOf { it.estimatedValue * it.quantity }
                _uiState.value = _uiState.value.copy(
                    items = items,
                    totalEstimatedValue = total,
                    isLoading = false
                )
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingItem = null, errorMessage = "")
    }

    fun openEditDialog(item: CollectionItem) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingItem = item, errorMessage = "")
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingItem = null, errorMessage = "")
    }

    fun saveItem(
        name: String,
        series: String,
        category: String,
        material: String,
        nominal: String,
        quality: String,
        description: String,
        photoPath: String,
        estimatedValue: Double,
        quantity: Int
    ) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Введіть назву товару")
            return
        }
        if (quantity <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Кількість має бути > 0")
            return
        }

        viewModelScope.launch {
            val editing = _uiState.value.editingItem
            if (editing == null) {
                val newItem = CollectionItem(
                    collectionId = "COL_${UUID.randomUUID().toString().take(8).uppercase()}",
                    name = name.trim(),
                    series = series.trim(),
                    category = category.trim(),
                    material = material.trim(),
                    nominal = nominal.trim(),
                    quality = quality.trim(),
                    description = description.trim(),
                    photoPath = photoPath,
                    estimatedValue = estimatedValue,
                    quantity = quantity,
                    dateAdded = System.currentTimeMillis()
                )
                repository.addCollectionItem(newItem)
                _uiState.value = _uiState.value.copy(
                    showAddDialog = false,
                    editingItem = null,
                    successMessage = "Товар «${newItem.name}» додано до колекції"
                )
            } else {
                val updated = editing.copy(
                    name = name.trim(),
                    series = series.trim(),
                    category = category.trim(),
                    material = material.trim(),
                    nominal = nominal.trim(),
                    quality = quality.trim(),
                    description = description.trim(),
                    photoPath = photoPath,
                    estimatedValue = estimatedValue,
                    quantity = quantity
                )
                repository.updateCollectionItem(updated)
                _uiState.value = _uiState.value.copy(
                    showAddDialog = false,
                    editingItem = null,
                    successMessage = "Зміни збережено"
                )
            }
        }
    }

    fun deleteItem(item: CollectionItem) {
        viewModelScope.launch {
            repository.deleteCollectionItem(item)
            _uiState.value = _uiState.value.copy(successMessage = "Товар видалено з колекції")
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = "", successMessage = "")
    }
}
