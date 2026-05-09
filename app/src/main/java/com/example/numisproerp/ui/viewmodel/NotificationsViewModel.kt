package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class NotificationSeverity { CRITICAL, WARNING }

data class NotificationItem(
    val id: String,
    val titleUa: String,
    val titleEn: String,
    val descriptionUa: String,
    val descriptionEn: String,
    val severity: NotificationSeverity
)

private const val LOW_STOCK_THRESHOLD = 3

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val productsWithStock = repository.getProductsWithStock("").first()

            val items = mutableListOf<NotificationItem>()

            // Out of stock — товари, які раніше були закуплені, але повністю вичерпані
            productsWithStock
                .filter { it.totalPurchased > 0 && it.currentStock <= 0 }
                .forEach { p ->
                    items.add(
                        NotificationItem(
                            id = "out_${p.catalogId}",
                            titleUa = "Закінчився товар: ${p.name}",
                            titleEn = "Out of stock: ${p.name}",
                            descriptionUa = "Залишок 0. Розгляньте можливість поповнення.",
                            descriptionEn = "Stock is 0. Consider restocking.",
                            severity = NotificationSeverity.CRITICAL
                        )
                    )
                }

            // Low stock — товари з малим залишком (>0 і <=THRESHOLD)
            productsWithStock
                .filter { it.currentStock in 1..LOW_STOCK_THRESHOLD }
                .forEach { p ->
                    items.add(
                        NotificationItem(
                            id = "low_${p.catalogId}",
                            titleUa = "Низький залишок: ${p.name}",
                            titleEn = "Low stock: ${p.name}",
                            descriptionUa = "Залишилось ${p.currentStock} шт.",
                            descriptionEn = "Only ${p.currentStock} pcs left.",
                            severity = NotificationSeverity.WARNING
                        )
                    )
                }

            _notifications.value = items
        }
    }
}
