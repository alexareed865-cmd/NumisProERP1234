package com.numisproerp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.numisproerp.R
import com.numisproerp.BuildConfig
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.data.repository.Repository
import com.numisproerp.data.settings.AppLanguage
import com.numisproerp.data.settings.AppTheme
import com.numisproerp.data.settings.SettingsManager
import com.numisproerp.di.AppDatabaseEntryPoint
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.utils.ExcelExporter
import com.numisproerp.utils.ExcelImporter
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsManager: SettingsManager,
    private val repository: Repository
) : ViewModel() {
    val theme: AppTheme get() = settingsManager.theme
    val language: AppLanguage get() = settingsManager.language
    fun setTheme(theme: AppTheme) { settingsManager.theme = theme }
    fun setLanguage(language: AppLanguage) { settingsManager.language = language }
    fun setLowStockThreshold(value: Int) { settingsManager.lowStockThreshold = value }
    suspend fun clearAllData() { repository.clearAllData() }
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database: AppDatabase = remember {
        EntryPointAccessors
            .fromApplication(context.applicationContext, AppDatabaseEntryPoint::class.java)
            .appDatabase()
    }
    val settings = viewModel.settingsManager

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDataDialog by remember { mutableStateOf(false) }
    var showFontsDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetConfirmation by remember { mutableStateOf("") }
    val resetDoneText = tr("Усі дані видалено", "All data cleared")

    val importedTitle = tr("Імпорт завершено", "Import complete")
    val productsLbl = tr("Товарів", "Products")
    val clientsLbl = tr("Клієнтів", "Clients")
    val suppliersLbl = tr("Постачальників", "Suppliers")
    val purchasesLbl = tr("Закупівель", "Purchases")
    val salesLbl = tr("Продажів", "Sales")
    val expensesLbl = tr("Витрат", "Expenses")
    val writeoffsLbl = tr("Списань", "Writeoffs")
    val collectionLbl = tr("Колекція", "Collection")
    val exportDoneTitle = tr("Експорт завершено", "Export complete")

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val importer = ExcelImporter(database)
                val result = importer.importFromUri(context, uri)
                val message = if (result.success) {
                    "$importedTitle: $productsLbl:${result.productsCount}, $clientsLbl:${result.clientsCount}, " +
                        "$suppliersLbl:${result.suppliersCount}, $purchasesLbl:${result.purchasesCount}, " +
                        "$salesLbl:${result.salesCount}, $expensesLbl:${result.expensesCount}, " +
                        "$writeoffsLbl:${result.writeoffsCount}, $collectionLbl:${result.collectionCount}"
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
            Toast.makeText(
                context,
                if (result.success) "$exportDoneTitle: ${result.filePath}" else result.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val bgSetText = tr("Фон встановлено", "Background set")
    val bgErrorText = tr("Помилка завантаження", "Load error")
    val bgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val target = File(context.filesDir, "custom_bg.jpg")
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    if (stream != null) {
                        stream.use { input ->
                            FileOutputStream(target).use { output -> input.copyTo(output) }
                        }
                        settings.backgroundImagePath = target.absolutePath
                        Toast.makeText(context, bgSetText, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, bgErrorText, Toast.LENGTH_SHORT).show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(context, bgErrorText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = tr("Назад", "Back"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = tr("Налаштування", "Settings"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SettingsButton(
                    icon = Icons.Default.ColorLens,
                    title = tr("Тема", "Theme"),
                    subtitle = settings.theme.name,
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsButton(
                    icon = Icons.Default.Language,
                    title = tr("Мова", "Language"),
                    subtitle = settings.language.name,
                    onClick = { showLanguageDialog = true }
                )
            }
            item {
                SettingsButton(
                    icon = Icons.Default.Storage,
                    title = tr("Дані", "Data"),
                    subtitle = tr("Імпорт / Експорт", "Import / Export"),
                    onClick = { showDataDialog = true }
                )
            }
            item {
                SettingsButton(
                    icon = Icons.Default.FormatSize,
                    title = tr("Шрифти", "Fonts"),
                    subtitle = tr("Розмір, тип, колір", "Size, type, color"),
                    onClick = { showFontsDialog = true }
                )
            }
            item {
                SettingsButton(
                    icon = Icons.Default.Image,
                    title = tr("Фоновий малюнок", "Background image"),
                    subtitle = if (settings.backgroundImagePath.isNotBlank())
                        tr("Встановлено", "Set") else tr("Не вибрано", "Not set"),
                    onClick = { showBackgroundDialog = true }
                )
            }

            // Notifications threshold
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
                ) {
                    val currentThreshold by settings.lowStockThresholdState
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Поріг низького залишку", "Low-stock threshold"),
                                fontSize = 15.sp, fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (currentThreshold == 0) tr("вимкнено", "off")
                                else "$currentThreshold ${tr("шт.", "pcs")}",
                                fontSize = 14.sp, color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = currentThreshold.toFloat(),
                            onValueChange = { viewModel.setLowStockThreshold(it.toInt()) },
                            valueRange = 0f..SettingsManager.MAX_LOW_STOCK_THRESHOLD.toFloat(),
                            steps = SettingsManager.MAX_LOW_STOCK_THRESHOLD - 1
                        )
                    }
                }
            }

            // About
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text("NumisProERP", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${tr("Версія", "Version")} ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Danger zone
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { resetConfirmation = ""; showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Text(tr("Скинути всі дані", "Reset all data"), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }

    // ===== DIALOGS =====
    if (showThemeDialog) {
        ThemeDialog(
            current = settings.theme,
            onSelect = { viewModel.setTheme(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            current = settings.language,
            onSelect = { viewModel.setLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showDataDialog) {
        DataDialog(
            onImport = {
                importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            },
            onExport = exportAction,
            onDismiss = { showDataDialog = false }
        )
    }

    if (showFontsDialog) {
        FontsDialog(
            settings = settings,
            onDismiss = { showFontsDialog = false }
        )
    }

    if (showBackgroundDialog) {
        BackgroundImageDialog(
            currentPath = settings.backgroundImagePath,
            onPickImage = { bgPickerLauncher.launch("image/*") },
            onRemove = { settings.backgroundImagePath = "" },
            onDismiss = { showBackgroundDialog = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(tr("Видалити всі дані?", "Delete all data?")) },
            text = {
                Column {
                    Text(
                        tr(
                            "Введіть RESET у поле нижче для підтвердження.",
                            "Type RESET below to confirm."
                        ), fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetConfirmation,
                        onValueChange = { resetConfirmation = it },
                        label = { Text(tr("Підтвердження", "Confirmation")) },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = resetConfirmation == "RESET",
                    onClick = {
                        showResetDialog = false
                        scope.launch {
                            viewModel.clearAllData()
                            Toast.makeText(context, resetDoneText, Toast.LENGTH_LONG).show()
                        }
                    }
                ) { Text(tr("Видалити", "Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(tr("Скасувати", "Cancel")) }
            }
        )
    }
}

// ======================== Top-level settings button ========================

@Composable
private fun SettingsButton(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

// ======================== Theme dialog ========================

@Composable
private fun ThemeDialog(current: AppTheme, onSelect: (AppTheme) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Тема оформлення", "Theme")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOptionRow(tr("Стандартна", "Default"), selected = current == AppTheme.DEFAULT) { onSelect(AppTheme.DEFAULT) }
                ThemeOptionRow("OlegSmile", selected = current == AppTheme.OLEG_SMILE, emblem = R.drawable.oleg_smile_emblem) { onSelect(AppTheme.OLEG_SMILE) }
                ThemeOptionRow("OlegSmile v2", selected = current == AppTheme.OLEG_SMILE_V2, emblem = R.drawable.oleg_smile_emblem) { onSelect(AppTheme.OLEG_SMILE_V2) }
                ThemeOptionRow("OlegSmile Light", selected = current == AppTheme.OLEG_SMILE_LIGHT, emblem = R.drawable.oleg_smile_emblem) { onSelect(AppTheme.OLEG_SMILE_LIGHT) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Готово", "Done")) } }
    )
}

@Composable
private fun ThemeOptionRow(title: String, selected: Boolean, emblem: Int? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emblem != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = emblem),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

// ======================== Language dialog ========================

@Composable
private fun LanguageDialog(current: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Мова інтерфейсу", "App language")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LangRow("Українська", selected = current == AppLanguage.UA) { onSelect(AppLanguage.UA) }
                LangRow("English", selected = current == AppLanguage.EN) { onSelect(AppLanguage.EN) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Готово", "Done")) } }
    )
}

@Composable
private fun LangRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

// ======================== Data dialog ========================

@Composable
private fun DataDialog(onImport: () -> Unit, onExport: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Дані", "Data")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onImport(); onDismiss() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Імпорт з Excel", "Import from Excel"))
                }
                Button(onClick = { onExport(); onDismiss() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.ImportExport, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Експорт в Excel", "Export to Excel"))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Закрити", "Close")) } }
    )
}

// ======================== Fonts dialog ========================

@Composable
private fun FontsDialog(settings: SettingsManager, onDismiss: () -> Unit) {
    var fontSize by settings.fontSizeState
    var fontFamily by settings.fontFamilyState
    var fontColor by settings.fontColorState

    val families = listOf("system", "serif", "sans-serif", "monospace")
    val colors = listOf(
        "" to tr("За замовчуванням", "Default"),
        "FFFFFF" to tr("Білий", "White"),
        "000000" to tr("Чорний", "Black"),
        "FFD700" to tr("Золотий", "Gold"),
        "007AFF" to tr("Синій", "Blue"),
        "34C759" to tr("Зелений", "Green")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Шрифти", "Fonts")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Size
                Text(tr("Розмір шрифту: ${fontSize}sp", "Font size: ${fontSize}sp"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { settings.fontSize = it.toInt() },
                    valueRange = SettingsManager.MIN_FONT_SIZE.toFloat()..SettingsManager.MAX_FONT_SIZE.toFloat(),
                    steps = (SettingsManager.MAX_FONT_SIZE - SettingsManager.MIN_FONT_SIZE) - 1
                )
                Text(
                    tr("Рекомендовано 14–16sp для зручного читання", "Recommended 14–16sp for comfortable reading"),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(4.dp))
                // Family
                Text(tr("Тип шрифту", "Font type"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                families.forEach { family ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { settings.fontFamily = family }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(family, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        RadioButton(
                            selected = fontFamily == family,
                            onClick = { settings.fontFamily = family },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Text(
                    tr("Рекомендація: system — найбільш читабельний на вашому пристрої",
                        "Recommendation: system — most readable on your device"),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(4.dp))
                // Color
                Text(tr("Колір тексту", "Text color"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                colors.forEach { (hex, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { settings.fontColor = hex }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hex.isNotBlank()) {
                            Box(
                                modifier = Modifier.size(20.dp).clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor("#$hex")))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        RadioButton(
                            selected = fontColor == hex,
                            onClick = { settings.fontColor = hex },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Готово", "Done")) } }
    )
}

// ======================== Background image dialog ========================

@Composable
private fun BackgroundImageDialog(currentPath: String, onPickImage: () -> Unit, onRemove: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Фоновий малюнок", "Background image")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentPath.isNotBlank()) {
                    Text(
                        tr("Поточний: встановлено", "Current: set"),
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        tr("Фон не вибрано", "No background set"),
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Button(onClick = { onPickImage(); onDismiss() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Вибрати зображення…", "Choose image…"))
                }
                if (currentPath.isNotBlank()) {
                    OutlinedButton(onClick = { onRemove(); onDismiss() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(tr("Видалити фон", "Remove background"))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Закрити", "Close")) } }
    )
}
