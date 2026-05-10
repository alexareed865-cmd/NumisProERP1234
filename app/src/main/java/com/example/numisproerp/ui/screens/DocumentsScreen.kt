package com.numisproerp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.numisproerp.data.dao.ClientForSelection
import com.numisproerp.data.dao.SupplierForSelection
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.di.AppDatabaseEntryPoint
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.utils.PdfReportGenerator
import com.numisproerp.utils.ReceiptShareUtil
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    val pdfCreatedLabel = tr("PDF створено", "PDF created")
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    // Default range: last 30 days … today
    val defaultEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val defaultStart = remember {
        Calendar.getInstance().apply {
            timeInMillis = defaultEnd
            add(Calendar.DAY_OF_MONTH, -30)
        }.timeInMillis
    }

    var startDate by remember { mutableStateOf(defaultStart) }
    var endDate by remember { mutableStateOf(defaultEnd) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var clients by remember { mutableStateOf<List<ClientForSelection>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<SupplierForSelection>>(emptyList()) }
    var selectedClient by remember { mutableStateOf<ClientForSelection?>(null) }
    var selectedSupplier by remember { mutableStateOf<SupplierForSelection?>(null) }
    var clientMenuExpanded by remember { mutableStateOf(false) }
    var supplierMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        clients = database.clientDao().getClientsForSelection()
        suppliers = database.supplierDao().getSuppliersForSelection()
    }

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    val onSalesReport: () -> Unit = {
        scope.launch {
            val generator = PdfReportGenerator(database)
            val result = generator.generateSalesReport(
                context, startDate, endDate, selectedClient?.clientId
            )
            toast(if (result.success) "$pdfCreatedLabel: ${result.filePath}" else result.message)
            if (result.success && result.filePath.isNotEmpty()) {
                ReceiptShareUtil.openPdf(context, result.filePath)
            }
        }
    }
    val onPurchasesReport: () -> Unit = {
        scope.launch {
            val generator = PdfReportGenerator(database)
            val result = generator.generatePurchasesReport(
                context, startDate, endDate, selectedSupplier?.supplierId
            )
            toast(if (result.success) "$pdfCreatedLabel: ${result.filePath}" else result.message)
            if (result.success && result.filePath.isNotEmpty()) {
                ReceiptShareUtil.openPdf(context, result.filePath)
            }
        }
    }
    val onOperationsReport: () -> Unit = {
        scope.launch {
            val generator = PdfReportGenerator(database)
            val result = generator.generateOperationsReport(context)
            toast(if (result.success) "$pdfCreatedLabel: ${result.filePath}" else result.message)
            if (result.success && result.filePath.isNotEmpty()) {
                ReceiptShareUtil.openPdf(context, result.filePath)
            }
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
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = tr("Назад", "Back"),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = tr("Документи", "Documents"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr(
                    "Виберіть період і сформуйте PDF за продажами клієнтам або закупівлями у постачальників.",
                    "Select a period and generate a PDF report for sales to clients or purchases from suppliers."
                ),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ---- Date range picker ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        tr("Період", "Period"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Text(
                                "  ${tr("Від", "From")}: ${dateFormat.format(Date(startDate))}",
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Text(
                                "  ${tr("До", "To")}: ${dateFormat.format(Date(endDate))}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Sales report card ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        tr("Продажі клієнтам", "Sales to clients"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = clientMenuExpanded,
                        onExpandedChange = { clientMenuExpanded = !clientMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.name ?: tr("Усі клієнти", "All clients"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(tr("Клієнт", "Client"), fontSize = 12.sp) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = clientMenuExpanded,
                            onDismissRequest = { clientMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(tr("Усі клієнти", "All clients")) },
                                onClick = {
                                    selectedClient = null
                                    clientMenuExpanded = false
                                }
                            )
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedClient = c
                                        clientMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSalesReport,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Text(
                            tr("PDF звіт продажів", "Sales PDF report"),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Purchases report card ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        tr("Закупівлі у постачальників", "Purchases from suppliers"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = supplierMenuExpanded,
                        onExpandedChange = { supplierMenuExpanded = !supplierMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSupplier?.name ?: tr("Усі постачальники", "All suppliers"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(tr("Постачальник", "Supplier"), fontSize = 12.sp) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierMenuExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = supplierMenuExpanded,
                            onDismissRequest = { supplierMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(tr("Усі постачальники", "All suppliers")) },
                                onClick = {
                                    selectedSupplier = null
                                    supplierMenuExpanded = false
                                }
                            )
                            suppliers.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSupplier = s
                                        supplierMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onPurchasesReport,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Text(
                            tr("PDF звіт закупівель", "Purchases PDF report"),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- All operations report (existing behavior) ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        tr("Зведений звіт", "Combined operations report"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onOperationsReport,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Text(
                            tr("Звіт по всіх операціях", "All operations report"),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tr(
                    "Звіти зберігаються у папці Downloads.",
                    "Reports are saved to the Downloads folder."
                ),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { startDate = it }
                    showStartPicker = false
                }) { Text(tr("OK", "OK")) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
    if (showEndPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { endDate = it }
                    showEndPicker = false
                }) { Text(tr("OK", "OK")) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
