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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.numisproerp.R
import com.numisproerp.data.database.AppDatabase
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
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsManager: SettingsManager
) : ViewModel() {
    val theme: AppTheme
        get() = settingsManager.theme

    val language: AppLanguage
        get() = settingsManager.language

    fun setTheme(theme: AppTheme) {
        settingsManager.theme = theme
    }

    fun setLanguage(language: AppLanguage) {
        settingsManager.language = language
    }
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.settingsManager.themeState
    val currentLanguage by viewModel.settingsManager.languageState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database: AppDatabase = remember {
        EntryPointAccessors
            .fromApplication(context.applicationContext, AppDatabaseEntryPoint::class.java)
            .appDatabase()
    }

    // Texts (collected here to avoid calling tr() inside non-Composable lambdas)
    val importedTitleUa = "Імпорт завершено"
    val importedTitleEn = "Import complete"
    val productsLbl = tr("Товарів", "Products")
    val clientsLbl = tr("Клієнтів", "Clients")
    val suppliersLbl = tr("Постачальників", "Suppliers")
    val purchasesLbl = tr("Закупівель", "Purchases")
    val salesLbl = tr("Продажів", "Sales")
    val expensesLbl = tr("Витрат", "Expenses")
    val importedTitle = tr(importedTitleUa, importedTitleEn)
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
                        "$salesLbl:${result.salesCount}, $expensesLbl:${result.expensesCount}"
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
                    Icons.Default.ArrowBack,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== Theme =====
            item {
                Text(
                    text = tr("Тема оформлення", "Theme"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            item {
                ThemeOptionCard(
                    title = tr("Стандартна", "Default"),
                    subtitle = tr(
                        "iOS-стиль зі світлою/темною темою системи",
                        "iOS-style with system light/dark theme"
                    ),
                    selected = currentTheme == AppTheme.DEFAULT,
                    onClick = { viewModel.setTheme(AppTheme.DEFAULT) }
                )
            }
            item {
                ThemeOptionCard(
                    title = "OlegSmile",
                    subtitle = tr(
                        "Чорно-золота фірмова тема з емблемою лева",
                        "Black-and-gold branded theme with lion emblem"
                    ),
                    emblem = R.drawable.oleg_smile_emblem,
                    selected = currentTheme == AppTheme.OLEG_SMILE,
                    onClick = { viewModel.setTheme(AppTheme.OLEG_SMILE) }
                )
            }

            // ===== Language =====
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr("Мова інтерфейсу", "App language"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            item {
                LanguageOptionCard(
                    title = "Українська",
                    subtitle = "UA",
                    selected = currentLanguage == AppLanguage.UA,
                    onClick = { viewModel.setLanguage(AppLanguage.UA) }
                )
            }
            item {
                LanguageOptionCard(
                    title = "English",
                    subtitle = "EN",
                    selected = currentLanguage == AppLanguage.EN,
                    onClick = { viewModel.setLanguage(AppLanguage.EN) }
                )
            }

            // ===== Data: Import / Export =====
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr("Дані", "Data"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Button(
                            onClick = {
                                importLauncher.launch(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        ) {
                            Icon(Icons.Outlined.Publish, contentDescription = null)
                            Text(
                                tr("Імпорт з Excel", "Import from Excel"),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = exportAction,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        ) {
                            Icon(Icons.Outlined.ImportExport, contentDescription = null)
                            Text(
                                tr("Експорт в Excel", "Export to Excel"),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    emblem: Int? = null,
    onClick: () -> Unit
) {
    OptionCard(
        title = title,
        subtitle = subtitle,
        selected = selected,
        emblem = emblem,
        onClick = onClick
    )
}

@Composable
private fun LanguageOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OptionCard(title = title, subtitle = subtitle, selected = selected, onClick = onClick)
}

@Composable
private fun OptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    emblem: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emblem != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = emblem),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
                Spacer(modifier = Modifier.size(12.dp))
            }
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                RadioButton(
                    selected = false,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}
