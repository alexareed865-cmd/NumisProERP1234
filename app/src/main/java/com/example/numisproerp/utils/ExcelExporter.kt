package com.numisproerp.utils

import android.content.Context
import android.os.Environment
import com.numisproerp.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelExporter(
    private val database: AppDatabase
) {

    data class ExportResult(
        val success: Boolean,
        val message: String,
        val filePath: String = ""
    )

    suspend fun exportToExcelDefault(context: Context): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                // Аркуш "Каталог Товарів"
                createProductsSheet(workbook, dateFormat)

                // Аркуш "Клієнти"
                createClientsSheet(workbook)

                // Аркуш "Постачальники"
                createSuppliersSheet(workbook)

                // Аркуш "Закупівлі"
                createPurchasesSheet(workbook, dateFormat)

                // Аркуш "Продажі"
                createSalesSheet(workbook, dateFormat)

                // Аркуш "Витрати"
                createExpensesSheet(workbook, dateFormat)

                // Аркуш "Склад" — снапшот залишків (за п. 3 ТЗ)
                createStockSheet(workbook)

                val fileName = "NumisProERP_Backup_${System.currentTimeMillis()}.xlsx"
                val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                val file = File(folder, fileName)

                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()

                ExportResult(true, "Експорт завершено успішно", file.absolutePath)

            } catch (e: Exception) {
                e.printStackTrace()
                ExportResult(false, "Помилка експорту: ${e.message}")
            }
        }
    }

    private suspend fun createProductsSheet(workbook: XSSFWorkbook, dateFormat: SimpleDateFormat) {
        val sheet = workbook.createSheet("Каталог Товарів")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("CatalogID")
        header.createCell(1).setCellValue("Найменування")
        header.createCell(2).setCellValue("Серія")
        header.createCell(3).setCellValue("Матеріал")
        header.createCell(4).setCellValue("Номінал")
        header.createCell(5).setCellValue("Категорія")
        header.createCell(6).setCellValue("Дата випуску")

        val products = database.productDao().getAllProductsSync()
        var rowNum = 1
        for (product in products) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(product.catalogId)
            row.createCell(1).setCellValue(product.name)
            row.createCell(2).setCellValue(product.series)
            row.createCell(3).setCellValue(product.material)
            row.createCell(4).setCellValue(product.nominal)
            row.createCell(5).setCellValue(product.category)
            row.createCell(6).setCellValue(product.issueDate)
            rowNum++
        }
    }

    private suspend fun createClientsSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Клієнти")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ClientID")
        header.createCell(1).setCellValue("ПІБ / Нікнейм")
        header.createCell(2).setCellValue("Телефон")
        header.createCell(3).setCellValue("Telegram")
        header.createCell(4).setCellValue("Місто")
        header.createCell(5).setCellValue("Нотатки")

        val clients = database.clientDao().getAllClientsSync()
        var rowNum = 1
        for (client in clients) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(client.clientId)
            row.createCell(1).setCellValue(client.name)
            row.createCell(2).setCellValue(client.phone)
            row.createCell(3).setCellValue(client.telegram)
            row.createCell(4).setCellValue(client.city)
            row.createCell(5).setCellValue(client.notes)
            rowNum++
        }
    }

    private suspend fun createSuppliersSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Постачальники")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("SupplierID")
        header.createCell(1).setCellValue("Назва/Ім'я")
        header.createCell(2).setCellValue("Контактні дані")
        header.createCell(3).setCellValue("Тип")
        header.createCell(4).setCellValue("Коментар")

        val suppliers = database.supplierDao().getAllSuppliersSync()
        var rowNum = 1
        for (supplier in suppliers) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(supplier.supplierId)
            row.createCell(1).setCellValue(supplier.name)
            row.createCell(2).setCellValue(supplier.contact)
            row.createCell(3).setCellValue(supplier.type)
            row.createCell(4).setCellValue(supplier.comment)
            rowNum++
        }
    }

    private suspend fun createPurchasesSheet(workbook: XSSFWorkbook, dateFormat: SimpleDateFormat) {
        val sheet = workbook.createSheet("Закупівлі")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("PurchaseID")
        header.createCell(1).setCellValue("Дата")
        header.createCell(2).setCellValue("CatalogID")
        header.createCell(3).setCellValue("Кількість")
        header.createCell(4).setCellValue("Ціна")
        header.createCell(5).setCellValue("Дод.витрати")
        header.createCell(6).setCellValue("Сума")

        val purchases = database.purchaseDao().getAllPurchases()
        var rowNum = 1
        for (purchase in purchases) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(purchase.purchaseId)
            row.createCell(1).setCellValue(dateFormat.format(Date(purchase.date)))
            row.createCell(2).setCellValue(purchase.catalogId)
            row.createCell(3).setCellValue(purchase.quantity.toDouble())
            row.createCell(4).setCellValue(purchase.pricePerUnit)
            row.createCell(5).setCellValue(purchase.additionalCosts)
            row.createCell(6).setCellValue(purchase.totalAmount)
            rowNum++
        }
    }

    private suspend fun createSalesSheet(workbook: XSSFWorkbook, dateFormat: SimpleDateFormat) {
        val sheet = workbook.createSheet("Продажі")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("SaleID")
        header.createCell(1).setCellValue("Дата")
        header.createCell(2).setCellValue("CatalogID")
        header.createCell(3).setCellValue("ClientID")
        header.createCell(4).setCellValue("Кількість")
        header.createCell(5).setCellValue("Ціна")
        header.createCell(6).setCellValue("Сума")

        val sales = database.saleDao().getAllSales()
        var rowNum = 1
        for (sale in sales) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(sale.saleId)
            row.createCell(1).setCellValue(dateFormat.format(Date(sale.date)))
            row.createCell(2).setCellValue(sale.catalogId)
            row.createCell(3).setCellValue(sale.clientId)
            row.createCell(4).setCellValue(sale.quantity.toDouble())
            row.createCell(5).setCellValue(sale.pricePerUnit)
            row.createCell(6).setCellValue(sale.totalAmount)
            rowNum++
        }
    }

    private suspend fun createExpensesSheet(workbook: XSSFWorkbook, dateFormat: SimpleDateFormat) {
        val sheet = workbook.createSheet("Інші Витрати")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ExpenseID")
        header.createCell(1).setCellValue("Дата")
        header.createCell(2).setCellValue("Категорія")
        header.createCell(3).setCellValue("Сума")
        header.createCell(4).setCellValue("Коментар")

        val expenses = database.otherExpenseDao().getAllExpensesSync()
        var rowNum = 1
        for (expense in expenses) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(expense.expenseId)
            row.createCell(1).setCellValue(dateFormat.format(Date(expense.date)))
            row.createCell(2).setCellValue(expense.category)
            row.createCell(3).setCellValue(expense.amount)
            row.createCell(4).setCellValue(expense.comment)
            rowNum++
        }
    }

    private suspend fun createStockSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Склад")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("CatalogID")
        header.createCell(1).setCellValue("Найменування")
        header.createCell(2).setCellValue("Серія")
        header.createCell(3).setCellValue("Категорія")
        header.createCell(4).setCellValue("Матеріал")
        header.createCell(5).setCellValue("Кількість")
        header.createCell(6).setCellValue("Сер.закуп.ціна")
        header.createCell(7).setCellValue("Загальна вартість")

        val stock = database.productDao().getProductsInStock().first()
        var rowNum = 1
        for (item in stock) {
            val row = sheet.createRow(rowNum)
            row.createCell(0).setCellValue(item.catalogId)
            row.createCell(1).setCellValue(item.name)
            row.createCell(2).setCellValue(item.series)
            row.createCell(3).setCellValue(item.category)
            row.createCell(4).setCellValue(item.material)
            row.createCell(5).setCellValue(item.currentStock.toDouble())
            row.createCell(6).setCellValue(item.avgPurchasePrice)
            row.createCell(7).setCellValue(item.currentStock * item.avgPurchasePrice)
            rowNum++
        }
    }
}