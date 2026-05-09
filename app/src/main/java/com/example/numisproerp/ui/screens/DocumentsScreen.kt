package com.numisproerp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.di.AppDatabaseEntryPoint
import com.numisproerp.ui.theme.IOSDesign
import dagger.hilt.android.EntryPointAccessors
import com.numisproerp.utils.ExcelExporter
import com.numisproerp.utils.ExcelImporter
import com.numisproerp.utils.PdfReportGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database: AppDatabase = remember {
        EntryPointAccessors
            .fromApplication(context.applicationContext, AppDatabaseEntryPoint::class.java)
            .appDatabase()
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val importer = ExcelImporter(database)
                val result = importer.importFromUri(context, uri)
                val message = if (result.success) {
                    "Імпорт завершено: Товарів:${result.productsCount}, Клієнтів:${result.clientsCount}, " +
                            "Постачальників:${result.suppliersCount}, Закупівель:${result.purchasesCount}, " +
                            "Продажів:${result.salesCount}, Витрат:${result.expensesCount}"
                } else {
                    result.message
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val exportAction: () -> Unit = {
        scope.launch {
            val exporter = ExcelExporter(database)
            val result = exporter.exportToExcelDefault(context)
            Toast.makeText(context, if (result.success) "Експорт завершено: ${result.filePath}" else result.message, Toast.LENGTH_LONG).show()
        }
    }

    val pdfReportAction: () -> Unit = {
        scope.launch {
            val generator = PdfReportGenerator(database)
            val result = generator.generateOperationsReport(context)
            Toast.makeText(
                context,
                if (result.success) "PDF створено: ${result.filePath}" else result.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevationRaised)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Документи та звіти",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = pdfReportAction,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Text("PDF звіт по операціях", modifier = Modifier.padding(start = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Outlined.Publish, contentDescription = null)
                        Text("Імпорт з Excel", modifier = Modifier.padding(start = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = exportAction,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Outlined.ImportExport, contentDescription = null)
                        Text("Експорт в Excel", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
