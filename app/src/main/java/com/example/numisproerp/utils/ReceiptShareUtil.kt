package com.numisproerp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * Допоміжний клас для роботи з PDF-чеком після продажу.
 *
 * Чек спочатку зберігається у `cacheDir/receipts` (через [PdfReportGenerator]).
 * Звідти його можна:
 *   - [share] — відправити через Telegram, Viber тощо;
 *   - [saveToDownloads] — скопіювати у публічну папку Downloads;
 *   - [discard] — просто видалити файл.
 */
object ReceiptShareUtil {

    fun share(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "NumisProERP — чек")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Поділитися чеком").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Копіює чек у Downloads (`/storage/emulated/0/Download/...`).
     * Повертає шлях до новоствореного файлу або null у випадку помилки.
     */
    fun saveToDownloads(context: Context, filePath: String): String? {
        val src = File(filePath)
        if (!src.exists()) return null
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val target = File(downloadsDir, src.name)
        return try {
            src.inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun discard(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) file.delete()
        } catch (_: Exception) {
            // best-effort cleanup
        }
    }

    /**
     * Відкриває PDF у зовнішньому переглядачі (Adobe / Google PDF Viewer тощо).
     * Якщо такого додатка не знайдено — відкриває chooser шерінгу як фолбек.
     */
    fun openPdf(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            val chooser = Intent.createChooser(viewIntent, "Відкрити PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Якщо немає переглядача PDF — фолбек до шерінгу.
            share(context, filePath)
        }
    }
}
