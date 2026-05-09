package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.dao.ProductWithStock
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class StockUiState(
    val products: List<ProductWithStock> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val categories: List<String> = emptyList(),
    val showAddProductDialog: Boolean = false
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadProducts()
    }

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadProducts()
    }

    fun toggleAddProductDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddProductDialog = show)
    }

    // ЗМІНЕНО: з private на public
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val products = if (_uiState.value.selectedCategory.isNotEmpty()) {
                repository.getProductsWithStockByCategory(_uiState.value.selectedCategory)
            } else {
                repository.getProductsWithStock(_uiState.value.searchQuery)
            }

            // Фільтруємо тільки товари з кількістю > 0
            val filteredProducts = products.first().filter { it.currentStock > 0 }

            _uiState.value = _uiState.value.copy(
                products = filteredProducts,
                isLoading = false
            )
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val categories = repository.getDistinctCategories()
            _uiState.value = _uiState.value.copy(categories = categories)
        }
    }

    suspend fun getProductDetails(catalogId: String): com.numisproerp.data.entities.Product? {
        return repository.getProductById(catalogId)
    }

    fun addProduct(product: com.numisproerp.data.entities.Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
            loadProducts()
        }
    }
}
