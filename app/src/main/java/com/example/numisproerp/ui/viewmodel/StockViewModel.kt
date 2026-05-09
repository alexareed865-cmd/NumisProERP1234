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
    val showAddProductDialog: Boolean = false,
    val sortBy: String = "name",
    val showSortDialog: Boolean = false,
    val showMaterialDialog: Boolean = false,
    val filterMaterial: String = "",
    val materials: List<String> = emptyList()
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

    fun toggleSortDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSortDialog = show)
    }

    fun setSortBy(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        loadProducts()
    }

    fun toggleMaterialFilter() {
        _uiState.value = _uiState.value.copy(showMaterialDialog = !_uiState.value.showMaterialDialog)
    }

    fun updateFilterMaterial(material: String) {
        _uiState.value = _uiState.value.copy(filterMaterial = material, showMaterialDialog = false)
        loadProducts()
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

            var filteredProducts = products.first().filter { it.currentStock > 0 }

            if (_uiState.value.filterMaterial.isNotEmpty()) {
                filteredProducts = filteredProducts.filter { it.material == _uiState.value.filterMaterial }
            }

            val sortedProducts = when (_uiState.value.sortBy) {
                "name" -> filteredProducts.sortedBy { it.name }
                "quantity_desc" -> filteredProducts.sortedByDescending { it.currentStock }
                "quantity_asc" -> filteredProducts.sortedBy { it.currentStock }
                "price_desc" -> filteredProducts.sortedByDescending { it.avgPurchasePrice }
                "price_asc" -> filteredProducts.sortedBy { it.avgPurchasePrice }
                "category" -> filteredProducts.sortedBy { it.category }
                "material" -> filteredProducts.sortedBy { it.material }
                else -> filteredProducts
            }

            _uiState.value = _uiState.value.copy(
                products = sortedProducts,
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

    fun loadMaterials() {
        viewModelScope.launch {
            val materials = repository.getDistinctMaterials()
            _uiState.value = _uiState.value.copy(materials = materials)
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
