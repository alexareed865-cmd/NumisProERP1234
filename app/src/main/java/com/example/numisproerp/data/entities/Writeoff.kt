package com.numisproerp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запис про списання товару зі складу (некондиція, брак, інше).
 *
 * `pricePerUnit` — це **закупочна** ціна на момент списання (середня по
 * залишку), а `totalAmount = quantity * pricePerUnit`. Списання впливає на
 * залишки на складі (мінусує кількість) та враховується як витрата у звітах.
 */
@Entity(tableName = "writeoffs")
data class Writeoff(
    @PrimaryKey
    val writeoffId: String,
    val date: Long,
    val catalogId: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalAmount: Double,
    val reason: String,
    val comment: String
)
