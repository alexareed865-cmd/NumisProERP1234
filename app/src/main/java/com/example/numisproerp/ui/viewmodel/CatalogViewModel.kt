package com.numisproerp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.CatalogItem
import com.numisproerp.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val items: List<CatalogItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "",
    val sortBy: String = "name",
    val showSortDialog: Boolean = false,
    val showFilterDialog: Boolean = false,
    val isDataLoaded: Boolean = false
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    /**
     * Поточна підписка на `itemsFlow`. Кожен виклик `loadItems()` скасовує
     * попередню підписку, щоб уникнути нагромадження collect-ів при кожному
     * заході на CatalogScreen чи зміні фільтра/сортування.
     */
    private var itemsJob: Job? = null

    fun loadItems() {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            try {
                val itemsFlow = if (_uiState.value.selectedCategory.isNotEmpty()) {
                    repository.getItemsByCategory(_uiState.value.selectedCategory)
                } else {
                    repository.getAllItems()
                }

                itemsFlow.collect { items ->
                    _uiState.value = _uiState.value.copy(
                        items = applySorting(items),
                        isLoading = false,
                        isDataLoaded = items.isNotEmpty()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDataLoaded = false
                )
            }
        }

        // Категорії підвантажуємо окремою корутиною (одноразово), щоб збір
        // основного `itemsFlow` не блокував їх.
        viewModelScope.launch {
            try {
                val count = repository.getCount()
                if (count > 0) {
                    val categories = repository.getDistinctCategories()
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, isLoading = true)
        loadItems()
    }

    fun clearCategory() {
        _uiState.value = _uiState.value.copy(selectedCategory = "", isLoading = true)
        loadItems()
    }

    fun setSortBy(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        loadItems()
    }

    fun toggleSortDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSortDialog = show)
    }

    fun toggleFilterDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFilterDialog = show)
    }

    fun importExcelFile(context: Context, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.importExcelFile(context, uri)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                onComplete(true, "Завантажено $count товарів")
                loadItems()
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Помилка завантаження")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun applySorting(items: List<CatalogItem>): List<CatalogItem> {
        return when (_uiState.value.sortBy) {
            "name" -> items.sortedBy { it.name }
            "date" -> items.sortedByDescending { it.dateIntroduced }
            "denomination" -> items.sortedBy { it.denomination.toDoubleOrNull() ?: 0.0 }
            else -> items
        }
    }
}
