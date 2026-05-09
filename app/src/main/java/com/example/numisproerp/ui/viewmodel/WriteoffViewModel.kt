package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.dao.ProductInStock
import com.numisproerp.data.dao.WriteoffWithProductName
import com.numisproerp.data.entities.Writeoff
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

object WriteoffReasons {
    const val DEFECTIVE = "Брак"
    const val LOW_QUALITY = "Некондиція"
    const val LOST = "Втрата"
    const val DAMAGED = "Пошкодження"
    const val OTHER = "Інше"

    val ALL = listOf(DEFECTIVE, LOW_QUALITY, LOST, DAMAGED, OTHER)
}

data class WriteoffUiState(
    val productsInStock: List<ProductInStock> = emptyList(),
    val writeoffs: List<WriteoffWithProductName> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class WriteoffViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteoffUiState())
    val uiState: StateFlow<WriteoffUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val stock = repository.getProductsInStock().first()
            val writeoffs = repository.getAllWriteoffsWithProductName()
            _uiState.value = _uiState.value.copy(
                productsInStock = stock,
                writeoffs = writeoffs,
                isLoading = false
            )
        }
    }

    fun toggleAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showAddDialog = show,
            errorMessage = "",
            successMessage = ""
        )
    }

    fun submitWriteoff(
        catalogId: String,
        quantity: Int,
        reason: String,
        comment: String
    ) {
        viewModelScope.launch {
            val product = _uiState.value.productsInStock.find { it.catalogId == catalogId }
            if (product == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Товар не знайдено на складі")
                return@launch
            }
            if (quantity <= 0) {
                _uiState.value = _uiState.value.copy(errorMessage = "Кількість має бути більше 0")
                return@launch
            }
            if (quantity > product.currentStock) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Списується більше, ніж є на складі (${product.currentStock} шт.)"
                )
                return@launch
            }
            if (reason.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Виберіть причину списання")
                return@launch
            }

            val pricePerUnit = product.avgPurchasePrice
            val totalAmount = pricePerUnit * quantity
            val writeoff = Writeoff(
                writeoffId = UUID.randomUUID().toString(),
                date = System.currentTimeMillis(),
                catalogId = catalogId,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                totalAmount = totalAmount,
                reason = reason,
                comment = comment
            )
            repository.insertWriteoff(writeoff)

            _uiState.value = _uiState.value.copy(
                showAddDialog = false,
                successMessage = "Списано: ${product.name} × $quantity",
                errorMessage = ""
            )
            loadAll()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = "", successMessage = "")
    }
}
