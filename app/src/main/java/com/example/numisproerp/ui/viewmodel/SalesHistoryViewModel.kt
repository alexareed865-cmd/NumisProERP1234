package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.dao.SaleWithClientName
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesHistoryUiState(
    val allSales: List<SaleWithClientName> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesHistoryUiState())
    val uiState: StateFlow<SalesHistoryUiState> = _uiState.asStateFlow()

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val sales = repository.getAllSalesWithClientName()
            _uiState.value = _uiState.value.copy(allSales = sales, isLoading = false)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun filteredSales(): List<SaleWithClientName> {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return _uiState.value.allSales
        return _uiState.value.allSales.filter {
            it.productName.contains(query, ignoreCase = true) ||
                    it.clientName.contains(query, ignoreCase = true)
        }
    }
}
