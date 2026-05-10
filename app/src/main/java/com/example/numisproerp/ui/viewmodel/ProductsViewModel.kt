package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.CollectionItem
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
    val catalogImageMap: Map<String, String> = emptyMap(),
    val catalogImagePairMap: Map<String, Pair<String, String>> = emptyMap(),
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
        loadCatalogImages()
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

    private fun loadCatalogImages() {
        viewModelScope.launch {
            val imageMap = repository.getCatalogImageMap()
            val imagePairMap = repository.getCatalogImagePairMap()
            _uiState.value = _uiState.value.copy(
                catalogImageMap = imageMap,
                catalogImagePairMap = imagePairMap
            )
        }
    }

    fun getProductImageUrl(product: Product): String {
        if (product.photoPath.isNotBlank()) return product.photoPath
        val map = _uiState.value.catalogImageMap
        return map[product.catalogId] ?: map[product.name] ?: ""
    }

    fun getProductImageUrls(product: Product): Pair<String, String> {
        if (product.photoPath.isNotBlank()) return Pair(product.photoPath, "")
        val map = _uiState.value.catalogImagePairMap
        return map[product.catalogId] ?: map[product.name] ?: Pair("", "")
    }

    /**
     * Додає товар до колекції без перезапису Product-запису та без
     * перетирання вже існуючого CollectionItem.
     * Повертає true при успіху, false якщо товар вже в колекції.
     */
    suspend fun addProductToCollection(product: Product): Boolean {
        // Підстраховка: якщо ініціалізація ще не встигла підвантажити карту
        // зображень каталогу — підвантажимо синхронно, щоб фото не загубились.
        if (_uiState.value.catalogImagePairMap.isEmpty()) {
            val imageMap = repository.getCatalogImageMap()
            val imagePairMap = repository.getCatalogImagePairMap()
            _uiState.value = _uiState.value.copy(
                catalogImageMap = imageMap,
                catalogImagePairMap = imagePairMap
            )
        }
        val imageUrls = getProductImageUrls(product)
        val item = CollectionItem(
            collectionId = product.catalogId,
            name = product.name,
            series = product.series,
            category = product.category,
            material = product.material,
            nominal = product.nominal,
            quality = product.quality,
            description = "",
            photoPath = imageUrls.first,
            estimatedValue = 0.0,
            quantity = 1,
            dateAdded = System.currentTimeMillis()
        )
        return repository.addExistingProductToCollection(item)
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
