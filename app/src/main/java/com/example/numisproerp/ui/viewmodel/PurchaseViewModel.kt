package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.dao.ProductForSelection
import com.numisproerp.data.dao.SupplierForSelection
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class PurchaseUiState(
    val suppliers: List<SupplierForSelection> = emptyList(),
    val products: List<ProductForSelection> = emptyList(),
    val isLoading: Boolean = false,
    val selectedSupplierId: String = "",
    val showSuccessMessage: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                val suppliers = repository.getSuppliersForSelection()
                val products = repository.getProductsForSelection()

                _uiState.value = _uiState.value.copy(
                    suppliers = suppliers,
                    products = products,
                    isLoading = false,
                    selectedSupplierId = ""
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Помилка мережі. Перевірте підключення."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Помилка завантаження даних: ${e.message}"
                )
            }
        }
    }

    fun updateSelectedSupplier(supplierId: String) {
        _uiState.value = _uiState.value.copy(selectedSupplierId = supplierId)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
