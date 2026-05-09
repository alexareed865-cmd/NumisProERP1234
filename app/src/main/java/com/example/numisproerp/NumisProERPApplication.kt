package com.numisproerp

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NumisProERPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // PDFBox-Android вимагає одноразової ініціалізації для роботи зі шрифтами
        // та ресурсами при генерації PDF (накладні у DocumentsScreen).
        PDFBoxResourceLoader.init(applicationContext)
    }
}
