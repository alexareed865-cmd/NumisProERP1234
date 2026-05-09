package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExpensesSort {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

data class ExpensesUiState(
    val expenses: List<OtherExpense> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val errorMessage: String = "",
    val selectedCategory: String = "",
    val sort: ExpensesSort = ExpensesSort.DATE_DESC,
    val categories: List<String> = emptyList()
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val expensesFlow = repository.getAllExpenses()
            val expenses = expensesFlow.first()
            val categories = expenses.map { it.category }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            _uiState.value = _uiState.value.copy(
                expenses = expenses,
                categories = categories,
                isLoading = false
            )
        }
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSort(sort: ExpensesSort) {
        _uiState.value = _uiState.value.copy(sort = sort)
    }

    fun visibleExpenses(): List<OtherExpense> {
        val state = _uiState.value
        val filtered = if (state.selectedCategory.isBlank()) state.expenses
        else state.expenses.filter { it.category == state.selectedCategory }
        return when (state.sort) {
            ExpensesSort.DATE_DESC -> filtered.sortedByDescending { it.date }
            ExpensesSort.DATE_ASC -> filtered.sortedBy { it.date }
            ExpensesSort.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            ExpensesSort.AMOUNT_ASC -> filtered.sortedBy { it.amount }
        }
    }

    fun toggleAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }

    fun addExpense(category: String, amount: String, comment: String) {
        viewModelScope.launch {
            val amountDouble = amount.toDoubleOrNull()

            if (category.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Введіть категорію витрати")
                return@launch
            }

            if (amountDouble == null || amountDouble <= 0) {
                _uiState.value = _uiState.value.copy(errorMessage = "Введіть коректну суму")
                return@launch
            }

            val expense = OtherExpense(
                expenseId = "EXP-${System.currentTimeMillis()}",
                date = System.currentTimeMillis(),
                category = category,
                amount = amountDouble,
                comment = comment
            )

            repository.insertOtherExpense(expense)

            _uiState.value = _uiState.value.copy(
                showAddDialog = false,
                showSuccessMessage = true,
                errorMessage = ""
            )

            loadExpenses()
        }
    }

    fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
