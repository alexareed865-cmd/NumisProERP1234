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

/**
 * Стан UI складу. Поле `selectedCategory` лишилось для зворотної сумісності з
 * іншими екранами, але основний фільтр тепер деревовидний:
 * `filterMaterial`, `filterCategory`, `filterQuality`, `filterSeries`,
 * `filterNominal` — кожен з них незалежно звужує список товарів.
 *
 * Для UI потрібні також списки можливих значень кожного критерію:
 * `materials`, `categories`, `qualities`, `seriesList`, `nominals`.
 */
data class StockUiState(
    val products: List<ProductWithStock> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val showAddProductDialog: Boolean = false,
    val sortBy: String = "name",
    val showSortDialog: Boolean = false,
    val showFilterDialog: Boolean = false,
    val filterMaterial: String = "",
    val filterCategory: String = "",
    val filterQuality: String = "",
    val filterSeries: String = "",
    val filterNominal: String = "",
    val materials: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val qualities: List<String> = emptyList(),
    val seriesList: List<String> = emptyList(),
    val nominals: List<String> = emptyList(),
    val catalogImagePairMap: Map<String, Pair<String, String>> = emptyMap()
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
        loadCatalogImages()
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

    fun toggleFilterDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFilterDialog = show)
    }

    fun updateFilterMaterial(material: String) {
        _uiState.value = _uiState.value.copy(filterMaterial = material)
        loadProducts()
    }

    fun updateFilterCategory(value: String) {
        _uiState.value = _uiState.value.copy(filterCategory = value)
        loadProducts()
    }

    fun updateFilterQuality(value: String) {
        _uiState.value = _uiState.value.copy(filterQuality = value)
        loadProducts()
    }

    fun updateFilterSeries(value: String) {
        _uiState.value = _uiState.value.copy(filterSeries = value)
        loadProducts()
    }

    fun updateFilterNominal(value: String) {
        _uiState.value = _uiState.value.copy(filterNominal = value)
        loadProducts()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            filterMaterial = "",
            filterCategory = "",
            filterQuality = "",
            filterSeries = "",
            filterNominal = "",
            selectedCategory = "",
            searchQuery = ""
        )
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val products = if (_uiState.value.selectedCategory.isNotEmpty()) {
                repository.getProductsWithStockByCategory(_uiState.value.selectedCategory)
            } else {
                repository.getProductsWithStock(_uiState.value.searchQuery)
            }

            var filteredProducts = products.first().filter { it.currentStock > 0 }

            val s = _uiState.value
            if (s.filterMaterial.isNotEmpty()) {
                filteredProducts = filteredProducts.filter { it.material == s.filterMaterial }
            }
            if (s.filterCategory.isNotEmpty()) {
                filteredProducts = filteredProducts.filter { it.category == s.filterCategory }
            }
            if (s.filterQuality.isNotEmpty()) {
                filteredProducts = filteredProducts.filter { it.quality == s.filterQuality }
            }
            // Series/nominal are not exposed on ProductWithStock; we look them up via product entity below.
            // Build sub-filter sources from currently visible set so they always reflect available options.

            // Auto-populate available filter values from full result set (before sub-filters), so that
            // the user can see what's pickable even after applying other filters.
            val baseForLists = products.first().filter { it.currentStock > 0 }
            val materials = baseForLists.map { it.material }.filter { it.isNotBlank() }.distinct().sorted()
            val categories = baseForLists.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
            val qualities = baseForLists.map { it.quality }.filter { it.isNotBlank() }.distinct().sorted()
            val seriesList = baseForLists.map { it.series }.filter { it.isNotBlank() }.distinct().sorted()
            val nominals = baseForLists.map { it.nominal }.filter { it.isNotBlank() }.distinct().sorted()

            if (s.filterSeries.isNotEmpty()) {
                filteredProducts = filteredProducts.filter { it.series == s.filterSeries }
            }
            if (s.filterNominal.isNotEmpty()) {
                filteredProducts = filteredProducts.filter { it.nominal == s.filterNominal }
            }

            val sortedProducts = when (s.sortBy) {
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
                materials = materials,
                categories = categories,
                qualities = qualities,
                seriesList = seriesList,
                nominals = nominals,
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

    private fun loadCatalogImages() {
        viewModelScope.launch {
            val imagePairMap = repository.getCatalogImagePairMap()
            _uiState.value = _uiState.value.copy(catalogImagePairMap = imagePairMap)
        }
    }

    suspend fun getProductDetails(catalogId: String): com.numisproerp.data.entities.Product? {
        return repository.getProductById(catalogId)
    }

    fun getProductImageUrls(product: com.numisproerp.data.entities.Product): Pair<String, String> {
        if (product.photoPath.isNotBlank()) return Pair(product.photoPath, "")
        val map = _uiState.value.catalogImagePairMap
        return map[product.catalogId] ?: map[product.name] ?: Pair("", "")
    }

    fun addProduct(product: com.numisproerp.data.entities.Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
            loadProducts()
        }
    }
}
