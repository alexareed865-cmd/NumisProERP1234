package com.numisproerp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.numisproerp.data.entities.CollectionItem
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.viewmodel.MyCollectionViewModel
import com.numisproerp.utils.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MyCollectionScreen(
    navController: NavHostController,
    viewModel: MyCollectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var detailItem by remember { mutableStateOf<CollectionItem?>(null) }
    var deleteCandidate by remember { mutableStateOf<CollectionItem?>(null) }

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
                contentDescription = tr("Назад", "Back"),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = tr("Моя колекція", "My collection"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tr("Кількість позицій", "Item count"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            "${uiState.items.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AccentBlue
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(tr("Оціночна вартість", "Estimated value"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            "${String.format("%,.2f", uiState.totalEstimatedValue)} ₴",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AccentGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            tr("Колекція порожня", "Collection is empty"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            tr("Натисніть «+» щоб додати свій перший товар", "Tap «+» to add your first item"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.items) { item ->
                        CollectionItemCard(
                            item = item,
                            onClick = { detailItem = item },
                            onEdit = { viewModel.openEditDialog(item) },
                            onDelete = { deleteCandidate = item }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openAddDialog() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = tr("Додати товар у колекцію", "Add item to collection"))
        }
    }

    // Add/Edit dialog
    if (uiState.showAddDialog) {
        AddOrEditCollectionDialog(
            initial = uiState.editingItem,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.closeDialog() },
            onSave = { name, series, category, material, nominal, quality,
                       description, photoPath, estimatedValue, quantity ->
                viewModel.saveItem(
                    name = name,
                    series = series,
                    category = category,
                    material = material,
                    nominal = nominal,
                    quality = quality,
                    description = description,
                    photoPath = photoPath,
                    estimatedValue = estimatedValue,
                    quantity = quantity
                )
            }
        )
    }

    // Detail dialog
    val detail = detailItem
    if (detail != null) {
        AlertDialog(
            onDismissRequest = { detailItem = null },
            title = { Text(detail.name) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (detail.photoPath.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(detail.photoPath))
                                .crossfade(true)
                                .build(),
                            contentDescription = detail.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Fit
                        )
                    }
                    InfoLine(tr("Серія", "Series"), detail.series)
                    InfoLine(tr("Категорія", "Category"), detail.category)
                    InfoLine(tr("Матеріал", "Material"), detail.material)
                    InfoLine(tr("Номінал", "Nominal"), detail.nominal)
                    InfoLine(tr("Якість", "Quality"), detail.quality)
                    InfoLine(tr("Кількість", "Quantity"), detail.quantity.toString())
                    InfoLine(tr("Оціночна вартість", "Estimated value"), "${String.format("%,.2f", detail.estimatedValue)} ₴")
                    if (detail.description.isNotEmpty()) {
                        Text(tr("Опис:", "Description:"), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(detail.description, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailItem = null }) { Text(tr("Закрити", "Close")) }
            }
        )
    }

    // Delete confirmation
    val toDelete = deleteCandidate
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(tr("Видалити з колекції?", "Delete from collection?")) },
            text = { Text(tr("Товар «${toDelete.name}» буде видалено з колекції. Цю дію не можна скасувати.", "Item «${toDelete.name}» will be removed from collection. This action cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(toDelete)
                    deleteCandidate = null
                }) { Text(tr("Видалити", "Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text(tr("Скасувати", "Cancel")) }
            }
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Row {
        Text("$label: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text(value, fontSize = 13.sp)
    }
}

@Composable
private fun CollectionItemCard(
    item: CollectionItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo or placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.photoPath.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(item.photoPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (item.series.isNotEmpty()) {
                    Text(
                        item.series,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    "${tr("Кількість", "Quantity")}: ${item.quantity} • ${String.format("%,.2f", item.estimatedValue)} ₴",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = tr("Редагувати", "Edit"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = tr("Видалити", "Delete"),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddOrEditCollectionDialog(
    initial: CollectionItem?,
    errorMessage: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        series: String,
        category: String,
        material: String,
        nominal: String,
        quality: String,
        description: String,
        photoPath: String,
        estimatedValue: Double,
        quantity: Int
    ) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var series by remember { mutableStateOf(initial?.series ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var material by remember { mutableStateOf(initial?.material ?: "") }
    var nominal by remember { mutableStateOf(initial?.nominal ?: "") }
    var quality by remember { mutableStateOf(initial?.quality ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var photoPath by remember { mutableStateOf(initial?.photoPath ?: "") }
    var estimatedValueStr by remember {
        mutableStateOf(initial?.estimatedValue?.let { if (it == 0.0) "" else it.toString() } ?: "")
    }
    var quantityStr by remember { mutableStateOf((initial?.quantity ?: 1).toString()) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = ImageStorage.copyUriToInternalStorage(context, uri)
            if (saved != null) photoPath = saved
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) tr("Новий товар колекції", "New collection item") else tr("Редагувати товар", "Edit item")) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                photoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoPath.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(photoPath))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = tr("Фото", "Photo"),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(onClick = {
                        photoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text(if (photoPath.isEmpty()) tr("Вибрати фото", "Pick photo") else tr("Змінити фото", "Change photo"))
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr("Назва *", "Name *")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = series,
                    onValueChange = { series = it },
                    label = { Text(tr("Серія", "Series")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(tr("Категорія", "Category")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text(tr("Матеріал", "Material")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nominal,
                    onValueChange = { nominal = it },
                    label = { Text(tr("Номінал", "Nominal")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = quality,
                    onValueChange = { quality = it },
                    label = { Text(tr("Якість", "Quality")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it.filter(Char::isDigit) },
                        label = { Text(tr("Кількість *", "Quantity *")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = estimatedValueStr,
                        onValueChange = { estimatedValueStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text(tr("Оціночна, ₴", "Estimated, ₴")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(tr("Опис", "Description")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantityStr.toIntOrNull() ?: 0
                val est = estimatedValueStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                onSave(
                    name, series, category, material, nominal, quality,
                    description, photoPath, est, qty
                )
            }) {
                Text(if (initial == null) tr("Додати", "Add") else tr("Зберегти", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Скасувати", "Cancel")) }
        }
    )
}
