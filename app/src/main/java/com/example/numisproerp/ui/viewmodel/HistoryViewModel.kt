package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryEntryType(val label: String) {
    PURCHASE("Закупівля"),
    SALE("Продаж"),
    WRITEOFF("Списання"),
    EXPENSE("Витрата")
}

enum class HistorySort {
    DATE_DESC,
    DATE_ASC
}

/**
 * Уніфікований запис історії. `sign` показує, як ця операція впливає на
 * грошовий потік: +1 для надходжень, -1 для витрат/списань.
 */
data class HistoryEntry(
    val id: String,
    val type: HistoryEntryType,
    val date: Long,
    val productName: String,
    val counterparty: String,
    val quantity: Int?,
    val amount: Double,
    val sign: Int,
    val comment: String
)

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTypes: Set<HistoryEntryType> = HistoryEntryType.values().toSet(),
    val sort: HistorySort = HistorySort.DATE_DESC
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val entries = mutableListOf<HistoryEntry>()

            repository.getAllPurchasesWithDetails().forEach { p ->
                entries.add(
                    HistoryEntry(
                        id = p.purchaseId,
                        type = HistoryEntryType.PURCHASE,
                        date = p.date,
                        productName = p.productName,
                        counterparty = p.supplierName,
                        quantity = null,
                        amount = p.totalAmount,
                        sign = -1,
                        comment = ""
                    )
                )
            }
            repository.getAllSalesWithDetails().forEach { s ->
                entries.add(
                    HistoryEntry(
                        id = s.saleId,
                        type = HistoryEntryType.SALE,
                        date = s.date,
                        productName = s.productName,
                        counterparty = s.clientName,
                        quantity = null,
                        amount = s.totalAmount,
                        sign = +1,
                        comment = ""
                    )
                )
            }
            repository.getAllWriteoffsWithProductName().forEach { w ->
                entries.add(
                    HistoryEntry(
                        id = w.writeoffId,
                        type = HistoryEntryType.WRITEOFF,
                        date = w.date,
                        productName = w.productName,
                        counterparty = w.reason,
                        quantity = w.quantity,
                        amount = w.totalAmount,
                        sign = -1,
                        comment = w.comment
                    )
                )
            }
            repository.getAllExpensesSync().forEach { e ->
                entries.add(
                    HistoryEntry(
                        id = e.expenseId,
                        type = HistoryEntryType.EXPENSE,
                        date = e.date,
                        productName = "",
                        counterparty = e.category,
                        quantity = null,
                        amount = e.amount,
                        sign = -1,
                        comment = e.comment
                    )
                )
            }

            _uiState.value = _uiState.value.copy(
                entries = entries,
                isLoading = false
            )
        }
    }

    fun toggleType(type: HistoryEntryType) {
        val current = _uiState.value.selectedTypes.toMutableSet()
        if (current.contains(type)) current.remove(type) else current.add(type)
        // Якщо користувач зняв усі — повертаємо повний набір, щоб не показувати
        // порожній список без причини.
        val next = if (current.isEmpty()) HistoryEntryType.values().toSet() else current
        _uiState.value = _uiState.value.copy(selectedTypes = next)
    }

    fun setSort(sort: HistorySort) {
        _uiState.value = _uiState.value.copy(sort = sort)
    }

    fun visibleEntries(): List<HistoryEntry> {
        val state = _uiState.value
        val filtered = state.entries.filter { it.type in state.selectedTypes }
        return when (state.sort) {
            HistorySort.DATE_DESC -> filtered.sortedByDescending { it.date }
            HistorySort.DATE_ASC -> filtered.sortedBy { it.date }
        }
    }
}
