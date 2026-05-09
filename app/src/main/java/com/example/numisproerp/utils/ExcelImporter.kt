package com.numisproerp.utils

import android.content.Context
import android.net.Uri
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.data.entities.Client
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.entities.Product
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Sale
import com.numisproerp.data.entities.Supplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ExcelImporter(
    private val database: AppDatabase
) {

    data class ImportResult(
        val success: Boolean,
        val message: String,
        val productsCount: Int = 0,
        val clientsCount: Int = 0,
        val suppliersCount: Int = 0,
        val purchasesCount: Int = 0,
        val salesCount: Int = 0,
        val expensesCount: Int = 0
    )

    suspend fun importFromUri(context: Context, uri: Uri): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    return@withContext importFromInputStream(inputStream)
                } ?: ImportResult(false, "Не вдалося відкрити файл")
            } catch (e: Exception) {
                e.printStackTrace()
                ImportResult(false, "Помилка імпорту: ${e.message}")
            }
        }
    }

    private suspend fun importFromInputStream(inputStream: InputStream): ImportResult {
        var productsCount = 0
        var clientsCount = 0
        var suppliersCount = 0
        var purchasesCount = 0
        var salesCount = 0
        var expensesCount = 0

        try {
            val workbook = WorkbookFactory.create(inputStream)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // 1. Імпорт товарів (аркуш "Каталог Товарів")
            val productSheet = workbook.getSheet("Каталог Товарів")
            if (productSheet != null) {
                // Визначаємо формат по заголовку:
                //  - "PhotoPath" у колонці 8 → новий експорт NumisProERP
                //  - інакше — стара 13-колонкова структура (legacy)
                val headerRow = productSheet.getRow(0)
                val isNewFormat = (headerRow?.getCell(8)?.toString() ?: "")
                    .equals("PhotoPath", ignoreCase = true)

                val products = mutableListOf<Product>()
                for (rowIndex in 1 until productSheet.physicalNumberOfRows) {
                    val row = productSheet.getRow(rowIndex) ?: continue
                    val catalogId = row.getCell(0)?.toString() ?: continue
                    if (catalogId.isBlank()) continue

                    val product = if (isNewFormat) {
                        Product(
                            catalogId = catalogId,
                            name = row.getCell(1)?.toString() ?: "",
                            series = row.getCell(2)?.toString() ?: "",
                            material = row.getCell(3)?.toString() ?: "",
                            nominal = row.getCell(4)?.toString() ?: "",
                            category = row.getCell(5)?.toString() ?: "",
                            issueDate = row.getCell(6)?.toString() ?: "",
                            quality = row.getCell(7)?.toString() ?: "",
                            photoPath = row.getCell(8)?.toString() ?: ""
                        )
                    } else {
                        Product(
                            catalogId = catalogId,
                            name = row.getCell(1)?.toString() ?: "",
                            series = row.getCell(2)?.toString() ?: "",
                            issueDate = row.getCell(3)?.toString() ?: "",
                            material = row.getCell(4)?.toString() ?: "",
                            nominal = row.getCell(5)?.toString() ?: "",
                            diameter = row.getCell(6)?.toString() ?: "",
                            weight = row.getCell(7)?.toString() ?: "",
                            mintageAnnounced = row.getCell(8)?.toString() ?: "",
                            category = row.getCell(9)?.toString() ?: "",
                            quality = row.getCell(10)?.toString() ?: "",
                            artist = row.getCell(11)?.toString() ?: "",
                            sculptor = row.getCell(12)?.toString() ?: "",
                            photoPath = ""
                        )
                    }
                    products.add(product)
                }
                if (products.isNotEmpty()) {
                    database.productDao().deleteAll()
                    database.productDao().insertAll(products)
                    productsCount = products.size
                }
            }

            // 2. Імпорт клієнтів (аркуш "Клієнти")
            val clientsSheet = workbook.getSheet("Клієнти")
            if (clientsSheet != null) {
                val clients = mutableListOf<Client>()
                for (rowIndex in 1 until clientsSheet.physicalNumberOfRows) {
                    val row = clientsSheet.getRow(rowIndex) ?: continue
                    val clientId = row.getCell(0)?.toString() ?: continue
                    if (clientId.isBlank()) continue

                    val client = Client(
                        clientId = clientId,
                        name = row.getCell(1)?.toString() ?: "",
                        phone = row.getCell(2)?.toString() ?: "",
                        telegram = row.getCell(3)?.toString() ?: "",
                        city = row.getCell(4)?.toString() ?: "",
                        notes = row.getCell(5)?.toString() ?: ""
                    )
                    clients.add(client)
                }
                if (clients.isNotEmpty()) {
                    database.clientDao().deleteAll()
                    database.clientDao().insertAll(clients)
                    clientsCount = clients.size
                }
            }

            // 3. Імпорт постачальників (аркуш "Постачальники")
            val suppliersSheet = workbook.getSheet("Постачальники")
            if (suppliersSheet != null) {
                val suppliers = mutableListOf<Supplier>()
                for (rowIndex in 1 until suppliersSheet.physicalNumberOfRows) {
                    val row = suppliersSheet.getRow(rowIndex) ?: continue
                    val supplierId = row.getCell(0)?.toString() ?: continue
                    if (supplierId.isBlank()) continue

                    val supplier = Supplier(
                        supplierId = supplierId,
                        name = row.getCell(1)?.toString() ?: "",
                        contact = row.getCell(2)?.toString() ?: "",
                        type = row.getCell(3)?.toString() ?: "",
                        comment = row.getCell(4)?.toString() ?: ""
                    )
                    suppliers.add(supplier)
                }
                if (suppliers.isNotEmpty()) {
                    database.supplierDao().deleteAll()
                    database.supplierDao().insertAll(suppliers)
                    suppliersCount = suppliers.size
                }
            }

            // 4. Імпорт закупівель (аркуш "Закупівлі")
            val purchasesSheet = workbook.getSheet("Закупівлі")
            if (purchasesSheet != null) {
                val purchases = mutableListOf<Purchase>()
                for (rowIndex in 1 until purchasesSheet.physicalNumberOfRows) {
                    val row = purchasesSheet.getRow(rowIndex) ?: continue
                    val purchaseId = row.getCell(0)?.toString() ?: continue
                    if (purchaseId.isBlank()) continue

                    val dateStr = row.getCell(1)?.toString() ?: ""
                    val date = try {
                        dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    val purchase = Purchase(
                        purchaseId = purchaseId,
                        date = date,
                        catalogId = row.getCell(2)?.toString() ?: "",
                        supplierId = row.getCell(3)?.toString() ?: "",
                        quantity = row.getCell(4)?.toString()?.toIntOrNull() ?: 0,
                        pricePerUnit = row.getCell(5)?.toString()?.toDoubleOrNull() ?: 0.0,
                        additionalCosts = row.getCell(6)?.toString()?.toDoubleOrNull() ?: 0.0,
                        totalAmount = row.getCell(7)?.toString()?.toDoubleOrNull() ?: 0.0
                    )
                    purchases.add(purchase)
                }
                if (purchases.isNotEmpty()) {
                    database.purchaseDao().deleteAll()
                    purchases.forEach { database.purchaseDao().insert(it) }
                    purchasesCount = purchases.size
                }
            }

            // 5. Імпорт продажів (аркуш "Продажі")
            val salesSheet = workbook.getSheet("Продажі")
            if (salesSheet != null) {
                val sales = mutableListOf<Sale>()
                for (rowIndex in 1 until salesSheet.physicalNumberOfRows) {
                    val row = salesSheet.getRow(rowIndex) ?: continue
                    val saleId = row.getCell(0)?.toString() ?: continue
                    if (saleId.isBlank()) continue

                    val dateStr = row.getCell(1)?.toString() ?: ""
                    val date = try {
                        dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    val sale = Sale(
                        saleId = saleId,
                        date = date,
                        catalogId = row.getCell(2)?.toString() ?: "",
                        clientId = row.getCell(3)?.toString() ?: "",
                        quantity = row.getCell(4)?.toString()?.toIntOrNull() ?: 0,
                        pricePerUnit = row.getCell(5)?.toString()?.toDoubleOrNull() ?: 0.0,
                        additionalCosts = row.getCell(6)?.toString()?.toDoubleOrNull() ?: 0.0,
                        netProfit = row.getCell(7)?.toString()?.toDoubleOrNull() ?: 0.0,
                        totalAmount = row.getCell(8)?.toString()?.toDoubleOrNull() ?: 0.0
                    )
                    sales.add(sale)
                }
                if (sales.isNotEmpty()) {
                    database.saleDao().deleteAll()
                    sales.forEach { database.saleDao().insert(it) }
                    salesCount = sales.size
                }
            }

            // 6. Імпорт витрат (аркуш "Інші Витрати")
            val expensesSheet = workbook.getSheet("Інші Витрати")
            if (expensesSheet != null) {
                val expenses = mutableListOf<OtherExpense>()
                for (rowIndex in 1 until expensesSheet.physicalNumberOfRows) {
                    val row = expensesSheet.getRow(rowIndex) ?: continue
                    val expenseId = row.getCell(0)?.toString() ?: continue
                    if (expenseId.isBlank()) continue

                    val dateStr = row.getCell(1)?.toString() ?: ""
                    val date = try {
                        dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    val expense = OtherExpense(
                        expenseId = expenseId,
                        date = date,
                        category = row.getCell(2)?.toString() ?: "",
                        amount = row.getCell(3)?.toString()?.toDoubleOrNull() ?: 0.0,
                        comment = row.getCell(4)?.toString() ?: ""
                    )
                    expenses.add(expense)
                }
                if (expenses.isNotEmpty()) {
                    database.otherExpenseDao().deleteAll()
                    expenses.forEach { database.otherExpenseDao().insert(it) }
                    expensesCount = expenses.size
                }
            }

            workbook.close()

            // Після імпорту products повторно синхронізуємо «дзеркальні» Product-и
            // для всіх товарів з «Моєї колекції». Інакше вони зникали би зі складу
            // (бо `productDao().deleteAll()` стирає їх, а Excel-імпорт не знає про
            // collection_items). Записуємо з актуальним `photoPath` із колекції.
            try {
                val collectionItems = database.collectionItemDao().getAllSync()
                if (collectionItems.isNotEmpty()) {
                    val collectionProducts = collectionItems.map { ci ->
                        Product(
                            catalogId = ci.collectionId,
                            name = ci.name,
                            series = ci.series,
                            material = ci.material,
                            nominal = ci.nominal,
                            category = ci.category,
                            quality = ci.quality,
                            photoPath = ci.photoPath
                        )
                    }
                    database.productDao().insertAll(collectionProducts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ImportResult(
                success = true,
                message = "Імпорт завершено успішно",
                productsCount = productsCount,
                clientsCount = clientsCount,
                suppliersCount = suppliersCount,
                purchasesCount = purchasesCount,
                salesCount = salesCount,
                expensesCount = expensesCount
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ImportResult(false, "Помилка імпорту: ${e.message}")
        }
    }
}