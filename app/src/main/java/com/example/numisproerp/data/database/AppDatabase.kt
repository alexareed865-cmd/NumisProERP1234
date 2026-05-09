package com.numisproerp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.numisproerp.data.dao.CatalogDao
import com.numisproerp.data.dao.ClientDao
import com.numisproerp.data.dao.OtherExpenseDao
import com.numisproerp.data.dao.ProductDao
import com.numisproerp.data.dao.PurchaseDao
import com.numisproerp.data.dao.SaleDao
import com.numisproerp.data.dao.SupplierDao
import com.numisproerp.data.dao.WriteoffDao
import com.numisproerp.data.entities.CatalogItem
import com.numisproerp.data.entities.Client
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.entities.Product
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Sale
import com.numisproerp.data.entities.Supplier
import com.numisproerp.data.entities.Writeoff

@Database(
    entities = [
        Product::class,
        Client::class,
        Supplier::class,
        Purchase::class,
        Sale::class,
        OtherExpense::class,
        CatalogItem::class,
        Writeoff::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun otherExpenseDao(): OtherExpenseDao
    abstract fun catalogDao(): CatalogDao
    abstract fun writeoffDao(): WriteoffDao

    companion object {
        /**
         * Реєстр міграцій між версіями схеми БД.
         *
         * Поточна базова версія — 11 (з цього моменту схема експортується у
         * `app/schemas/com.numisproerp.data.database.AppDatabase/11.json`).
         *
         * При будь-якій майбутній зміні entities:
         *   1) збільшити `version` у `@Database`,
         *   2) додати у цей масив новий `Migration(from, to) { db -> ... }`.
         *
         * Не використовуй більше `fallbackToDestructiveMigration()` — він знищує
         * дані користувача. Якщо абсолютно немає сенсу мігрувати — додавай
         * `fallbackToDestructiveMigrationFrom(versionN)` тільки на конкретні
         * старі версії.
         */
        val MIGRATIONS: Array<Migration> = arrayOf(
            // 11 → 12: додано таблицю writeoffs (списання товарів зі складу).
            object : Migration(11, 12) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `writeoffs` (
                            `writeoffId` TEXT NOT NULL,
                            `date` INTEGER NOT NULL,
                            `catalogId` TEXT NOT NULL,
                            `quantity` INTEGER NOT NULL,
                            `pricePerUnit` REAL NOT NULL,
                            `totalAmount` REAL NOT NULL,
                            `reason` TEXT NOT NULL,
                            `comment` TEXT NOT NULL,
                            PRIMARY KEY(`writeoffId`)
                        )
                        """.trimIndent()
                    )
                }
            }
        )
    }
}
