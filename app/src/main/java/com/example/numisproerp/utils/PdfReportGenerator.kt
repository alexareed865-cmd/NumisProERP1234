package com.numisproerp.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.numisproerp.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Генератор звітних PDF на основі вмісту бази даних.
 *
 * Використовує `android.graphics.pdf.PdfDocument` (вбудований у Android),
 * який підтримує кирилицю через системні шрифти Canvas. Завдяки цьому не
 * потрібно поставляти TTF-файли разом з додатком.
 */
class PdfReportGenerator(
    private val database: AppDatabase
) {

    data class Result(
        val success: Boolean,
        val message: String,
        val filePath: String = ""
    )

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    suspend fun generateOperationsReport(context: Context): Result = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageWidth = 595  // A4 portrait, 72 DPI ~= 595x842
            val pageHeight = 842
            val margin = 36f
            val usableWidth = pageWidth - margin * 2

            val titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                color = 0xFF000000.toInt()
            }
            val sectionPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
                color = 0xFF000000.toInt()
                typeface = Typeface.DEFAULT_BOLD
            }
            val bodyPaint = Paint().apply {
                textSize = 10f
                color = 0xFF222222.toInt()
            }
            val mutedPaint = Paint().apply {
                textSize = 9f
                color = 0xFF666666.toInt()
            }

            data class PageState(
                var page: PdfDocument.Page,
                var pageNumber: Int,
                var y: Float
            )

            fun newPage(state: PageState): PageState {
                document.finishPage(state.page)
                val nextNumber = state.pageNumber + 1
                val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, nextNumber).create()
                val nextPage = document.startPage(info)
                return PageState(nextPage, nextNumber, margin)
            }

            val firstInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val firstPage = document.startPage(firstInfo)
            var state = PageState(firstPage, 1, margin)

            fun ensureSpace(needed: Float) {
                if (state.y + needed > pageHeight - margin) {
                    state = newPage(state)
                }
            }

            fun drawText(text: String, paint: Paint, indent: Float = 0f) {
                ensureSpace(paint.textSize + 4f)
                state.page.canvas.drawText(text, margin + indent, state.y + paint.textSize, paint)
                state.y += paint.textSize + 4f
            }

            fun drawDivider() {
                ensureSpace(8f)
                state.page.canvas.drawLine(
                    margin, state.y + 4f,
                    margin + usableWidth, state.y + 4f,
                    mutedPaint
                )
                state.y += 8f
            }

            fun drawSection(title: String) {
                state.y += 6f
                drawText(title, sectionPaint)
                drawDivider()
            }

            // ===== Header =====
            drawText("NumisProERP — Звіт по операціях", titlePaint)
            drawText("Сформовано: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}", mutedPaint)
            drawDivider()

            // ===== Закупівлі =====
            val purchases = database.purchaseDao().getAllPurchases()
            val totalPurchases = purchases.sumOf { it.totalAmount }
            drawSection("Закупівлі (${purchases.size}) — Сума: ${"%,.2f".format(totalPurchases)} ₴")
            for (p in purchases) {
                val productName = database.productDao().getProductById(p.catalogId)?.name ?: p.catalogId
                drawText(
                    "${dateFormat.format(Date(p.date))}  •  $productName  •  ${p.quantity} шт. × ${"%,.2f".format(p.pricePerUnit)} = ${"%,.2f".format(p.totalAmount)} ₴",
                    bodyPaint
                )
            }

            // ===== Продажі =====
            val sales = database.saleDao().getAllSales()
            val totalSales = sales.sumOf { it.totalAmount }
            drawSection("Продажі (${sales.size}) — Сума: ${"%,.2f".format(totalSales)} ₴")
            for (s in sales) {
                val productName = database.productDao().getProductById(s.catalogId)?.name ?: s.catalogId
                drawText(
                    "${dateFormat.format(Date(s.date))}  •  $productName  •  ${s.quantity} шт. × ${"%,.2f".format(s.pricePerUnit)} = ${"%,.2f".format(s.totalAmount)} ₴",
                    bodyPaint
                )
            }

            // ===== Списання =====
            val writeoffs = database.writeoffDao().getAll()
            val totalWriteoffs = writeoffs.sumOf { it.totalAmount }
            drawSection("Списання (${writeoffs.size}) — Сума: ${"%,.2f".format(totalWriteoffs)} ₴")
            for (w in writeoffs) {
                val productName = database.productDao().getProductById(w.catalogId)?.name ?: w.catalogId
                drawText(
                    "${dateFormat.format(Date(w.date))}  •  $productName  •  ${w.quantity} шт. ×  ${"%,.2f".format(w.pricePerUnit)} = ${"%,.2f".format(w.totalAmount)} ₴  •  ${w.reason}",
                    bodyPaint
                )
            }

            // ===== Інші витрати =====
            val expenses = database.otherExpenseDao().getAllExpensesSync()
            val totalExpenses = expenses.sumOf { it.amount }
            drawSection("Витрати (${expenses.size}) — Сума: ${"%,.2f".format(totalExpenses)} ₴")
            for (e in expenses) {
                drawText(
                    "${dateFormat.format(Date(e.date))}  •  ${e.category}  •  ${"%,.2f".format(e.amount)} ₴  •  ${e.comment}",
                    bodyPaint
                )
            }

            // ===== Моя колекція =====
            val collection = database.collectionItemDao().getAllSync()
            val totalEstimated = collection.sumOf { it.estimatedValue * it.quantity }
            drawSection("Моя колекція (${collection.size}) — Оціночна вартість: ${"%,.2f".format(totalEstimated)} ₴")
            for (c in collection) {
                drawText(
                    "${c.name}  •  ${c.quantity} шт.  •  ${"%,.2f".format(c.estimatedValue)} ₴/шт.",
                    bodyPaint
                )
            }

            // ===== Підсумки =====
            drawSection("Підсумки")
            drawText("Дохід (продажі): ${"%,.2f".format(totalSales)} ₴", bodyPaint)
            drawText("Витрати на закупівлі: ${"%,.2f".format(totalPurchases)} ₴", bodyPaint)
            drawText("Інші витрати: ${"%,.2f".format(totalExpenses)} ₴", bodyPaint)
            drawText("Списання: ${"%,.2f".format(totalWriteoffs)} ₴", bodyPaint)
            val netProfit = totalSales - totalPurchases - totalExpenses - totalWriteoffs
            drawText("Чистий прибуток: ${"%,.2f".format(netProfit)} ₴", sectionPaint)

            document.finishPage(state.page)

            val fileName = "NumisProERP_Report_${System.currentTimeMillis()}.pdf"
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            Result(true, "PDF звіт збережено", file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result(false, "Помилка генерації PDF: ${e.message}")
        }
    }
}
