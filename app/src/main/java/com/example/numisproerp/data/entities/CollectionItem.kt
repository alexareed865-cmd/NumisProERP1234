package com.numisproerp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Товар з власної колекції користувача (за п. 12-13 ТЗ).
 *
 * - Не йде як закупівля → не зменшує грошовий баланс при додаванні.
 * - Падає на склад: впливає на залишки нарівні з закупленими товарами.
 * - При продажу собівартість = 0, тому весь дохід вважається чистим прибутком.
 * - `estimatedValue` — оціночна вартість, яку задає користувач при додаванні.
 *   Це довідкове поле, не використовується в розрахунках прибутку.
 */
@Entity(tableName = "collection_items")
data class CollectionItem(
    @PrimaryKey
    val collectionId: String,
    val name: String,
    val series: String = "",
    val category: String = "",
    val material: String = "",
    val nominal: String = "",
    val quality: String = "",
    val description: String = "",
    val photoPath: String = "",
    val estimatedValue: Double = 0.0,
    val quantity: Int = 1,
    val dateAdded: Long = System.currentTimeMillis()
)
