package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ReportsUiState(
    val isLoading: Boolean = false,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val stockValue: Double = 0.0,
    val topProducts: List<TopProduct> = emptyList(),
    val monthlyData: List<MonthlyStats> = emptyList(),
    val startDate: Long = getStartOfMonthStatic(),
    val endDate: Long = System.currentTimeMillis()
)

data class TopProduct(
    val catalogId: String,
    val name: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class MonthlyStats(
    val month: String,
    val revenue: Double,
    val expenses: Double,
    val profit: Double
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val startDate = _uiState.value.startDate
            val endDate = _uiState.value.endDate

            val purchasesSum = repository.getPurchasesSumByDateRange(startDate, endDate)
            val salesSum = repository.getSalesSumByDateRange(startDate, endDate)
            val otherExpensesSum = repository.getTotalOtherExpensesSum()
            val writeoffsSum = repository.getTotalWriteoffsSum()

            val totalRevenue = salesSum
            val totalExpenses = purchasesSum + otherExpensesSum + writeoffsSum
            val netProfit = totalRevenue - totalExpenses

            val stockValue = calculateStockValue()
            val topProducts = getTopProducts()
            val monthlyData = getMonthlyStats()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                totalRevenue = totalRevenue,
                totalExpenses = totalExpenses,
                netProfit = netProfit,
                stockValue = stockValue,
                topProducts = topProducts,
                monthlyData = monthlyData
            )
        }
    }

    fun updateDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
        loadReports()
    }

    private suspend fun calculateStockValue(): Double {
        val products = repository.getProductsInStock().first()
        var total = 0.0
        for (product in products) {
            total += product.currentStock * product.avgPurchasePrice
        }
        return total
    }

    private suspend fun getTopProducts(limit: Int = 5): List<TopProduct> {
        val allSales = repository.getAllSales()
        val productMap = mutableMapOf<String, TopProduct>()

        for (sale in allSales) {
            val productName = getProductName(sale.catalogId)
            val existing = productMap[productName]
            if (existing != null) {
                productMap[productName] = TopProduct(
                    catalogId = sale.catalogId,
                    name = productName,
                    quantitySold = existing.quantitySold + sale.quantity,
                    totalRevenue = existing.totalRevenue + sale.totalAmount
                )
            } else {
                productMap[productName] = TopProduct(
                    catalogId = sale.catalogId,
                    name = productName,
                    quantitySold = sale.quantity,
                    totalRevenue = sale.totalAmount
                )
            }
        }

        val sortedList = productMap.values.sortedByDescending { it.totalRevenue }
        return sortedList.take(limit)
    }

    private suspend fun getProductName(catalogId: String): String {
        val products = repository.getProductsForSelection()
        for (product in products) {
            if (product.catalogId == catalogId) {
                return product.name
            }
        }
        return "Невідомий товар"
    }

    private suspend fun getMonthlyStats(): List<MonthlyStats> {
        val result = mutableListOf<MonthlyStats>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("LLLL yyyy", Locale("uk"))

        // Поточний місяць зверху, далі вниз по спаду (за ТЗ п. 5).
        for (monthOffset in 0 until 6) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MONTH, -monthOffset)
            val startOfMonth = getStartOfMonthStatic(calendar.timeInMillis)
            val endOfMonth = getEndOfMonthStatic(calendar.timeInMillis)

            val revenue = repository.getSalesSumByDateRange(startOfMonth, endOfMonth)
            val purchases = repository.getPurchasesSumByDateRange(startOfMonth, endOfMonth)
            val writeoffs = repository.getWriteoffsSumByDateRange(startOfMonth, endOfMonth)
            val expenses = purchases + writeoffs

            result.add(
                MonthlyStats(
                    month = dateFormat.format(Date(calendar.timeInMillis))
                        .replaceFirstChar { it.titlecase(Locale("uk")) },
                    revenue = revenue,
                    expenses = expenses,
                    profit = revenue - expenses
                )
            )
        }

        return result
    }

    suspend fun getStockBreakdown(): List<com.numisproerp.data.dao.ProductInStock> {
        return repository.getProductsInStock().first()
    }

    fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}

fun getStartOfMonthStatic(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getEndOfMonthStatic(timestamp: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}
