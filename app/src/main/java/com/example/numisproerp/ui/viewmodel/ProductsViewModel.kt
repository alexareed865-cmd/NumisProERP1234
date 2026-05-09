package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.Product
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val categories: List<String> = emptyList()
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val products = repository.getAllProducts().first()
            _uiState.value = _uiState.value.copy(
                products = products,
                isLoading = false
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = repository.getDistinctCategories()
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    fun filteredProducts(): List<Product> {
        val state = _uiState.value
        return state.products.filter { product ->
            val matchesSearch = state.searchQuery.isBlank() ||
                product.name.contains(state.searchQuery, ignoreCase = true) ||
                product.series.contains(state.searchQuery, ignoreCase = true) ||
                product.catalogId.contains(state.searchQuery, ignoreCase = true)
            val matchesCategory = state.selectedCategory.isBlank() ||
                product.category == state.selectedCategory
            matchesSearch && matchesCategory
        }
    }
}
