package com.numisproerp.data.repository

import android.content.Context
import android.net.Uri
import com.numisproerp.data.dao.CatalogDao
import com.numisproerp.data.entities.CatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val catalogDao: CatalogDao
) {

    fun getAllItems(): Flow<List<CatalogItem>> = catalogDao.getAllItems()

    fun getItemsByCategory(category: String): Flow<List<CatalogItem>> = catalogDao.getItemsByCategory(category)

    suspend fun getDistinctCategories(): List<String> = withContext(Dispatchers.IO) {
        try {
            catalogDao.getDistinctCategories()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getItemById(id: String): CatalogItem? = withContext(Dispatchers.IO) {
        catalogDao.getItemById(id)
    }

    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        try {
            catalogDao.getCount()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            catalogDao.deleteAll()
        }
    }

    suspend fun importExcelFile(context: Context, uri: Uri): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val items = parseExcel(inputStream)
                    catalogDao.deleteAll()
                    if (items.isNotEmpty()) {
                        catalogDao.insertAll(items)
                    }
                    return@withContext Result.success(items.size)
                } ?: Result.failure(Exception("Не вдалося відкрити файл"))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private fun parseExcel(inputStream: InputStream): List<CatalogItem> {
        val items = mutableListOf<CatalogItem>()

        try {
            val workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                try {
                    val row = sheet.getRow(rowIndex) ?: continue

                    val nameCell = row.getCell(0)
                    val name = nameCell?.toString() ?: ""
                    if (name.isBlank()) continue

                    val websiteUrl = try {
                        nameCell?.hyperlink?.address ?: ""
                    } catch (e: Exception) {
                        ""
                    }

                    val series = getCellValue(row, 1)
                    // ВИПРАВЛЕНО: замість replace з Regex використовуємо просте очищення
                    val id = "${name}_${series}".replace(" ", "_").replace(",", "").replace(".", "").replace("-", "_")

                    val imageUrlFront = extractImageUrl(websiteUrl, "a")
                    val imageUrlBack = extractImageUrl(websiteUrl, "r")

                    val item = CatalogItem(
                        id = id,
                        name = name,
                        series = series,
                        dateIntroduced = getCellValue(row, 2),
                        material = getCellValue(row, 3),
                        denomination = getCellValue(row, 4),
                        diameter = getCellValue(row, 5),
                        weight = getCellValue(row, 6),
                        mintage = getCellValue(row, 7),
                        category = getCellValue(row, 8),
                        quality = getCellValue(row, 9),
                        artist = getCellValue(row, 10),
                        sculptor = getCellValue(row, 11),
                        websiteUrl = websiteUrl,
                        imageUrlFront = imageUrlFront,
                        imageUrlBack = imageUrlBack
                    )
                    items.add(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return items
    }

    private fun getCellValue(row: org.apache.poi.ss.usermodel.Row, index: Int): String {
        return try {
            row.getCell(index)?.toString() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractImageUrl(websiteUrl: String, type: String): String {
        if (websiteUrl.isBlank()) return ""
        val codeMatch = Regex("code=([A-Za-z0-9]+)").find(websiteUrl)
        val code = codeMatch?.groupValues?.get(1) ?: return ""
        return "https://bank.gov.ua/files/coins_images/${code}${type}.png?v=17"
    }
}
