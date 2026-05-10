package com.numisproerp.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.data.dao.PurchaseWithProductName
import com.numisproerp.ui.i18n.tr
import com.numisproerp.data.dao.SupplierWithBalance
import com.numisproerp.data.entities.Supplier
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.SuppliersViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    navController: NavHostController,
    viewModel: SuppliersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedSupplier by remember { mutableStateOf<SupplierWithBalance?>(null) }
    var editMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editContact by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("") }
    var editComment by remember { mutableStateOf("") }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var supplierToDelete by remember { mutableStateOf<SupplierWithBalance?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSuppliers()
    }

    LaunchedEffect(selectedSupplier) {
        if (selectedSupplier != null) {
            viewModel.loadPurchaseHistory(selectedSupplier!!.supplierId)
        }
    }

    val supplierUpdatedText = tr("Постачальника оновлено", "Supplier updated")
    val supplierDeletedText = tr("Постачальника видалено", "Supplier deleted")
    val supplierAddedText = tr("Постачальника додано", "Supplier added")
    val contactCopiedText = tr("Контакт скопійовано", "Contact copied")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Верхній рядок: кнопка назад + назва сторінки
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
                text = tr("Постачальники", "Suppliers"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Рядок: поле пошуку + кнопка додати
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(tr("Пошук за назвою, контактом або типом...", "Search by name, contact or type...")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = tr("Очистити", "Clear"))
                            }
                        }
                    },
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                )

                // Кнопка додати поруч з пошуком
                IconButton(
                    onClick = { viewModel.toggleAddDialog(true) },
                    modifier = Modifier.size(56.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                        containerColor = AccentBlue
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = tr("Додати постачальника", "Add supplier"),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.suppliers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("Немає постачальників.\nНатисніть + щоб додати", "No suppliers.\nTap + to add"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.suppliers) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onClick = {
                                selectedSupplier = supplier
                                editMode = false
                                showDetailDialog = true
                            },
                            onCopyContact = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("contact", supplier.contact))
                                Toast.makeText(context, contactCopiedText, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDetailDialog && selectedSupplier != null) {
        Dialog(
            onDismissRequest = {
                showDetailDialog = false
                selectedSupplier = null
                editMode = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editMode) tr("Редагувати постачальника", "Edit supplier") else selectedSupplier!!.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            showDetailDialog = false
                            selectedSupplier = null
                            editMode = false
                        }) {
                            Icon(Icons.Outlined.Clear, contentDescription = tr("Закрити", "Close"))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (editMode) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text(tr("Назва / Ім'я *", "Name *")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                            )
                            OutlinedTextField(
                                value = editContact,
                                onValueChange = { editContact = it },
                                label = { Text(tr("Контактні дані", "Contact info")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                            )
                            OutlinedTextField(
                                value = editType,
                                onValueChange = { editType = it },
                                label = { Text(tr("Тип", "Type")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                            )
                            OutlinedTextField(
                                value = editComment,
                                onValueChange = { editComment = it },
                                label = { Text(tr("Коментар", "Comment")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(tr("Основна інформація", "Main info"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (selectedSupplier!!.contact.isNotEmpty()) {
                                        Row {
                                            Text("${tr("Контакти", "Contacts")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                            Text(selectedSupplier!!.contact, fontSize = 13.sp, color = AccentBlue)
                                        }
                                    }
                                    if (selectedSupplier!!.type.isNotEmpty()) {
                                        Row {
                                            Text("${tr("Тип", "Type")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                            Text(selectedSupplier!!.type, fontSize = 13.sp)
                                        }
                                    }
                                    if (selectedSupplier!!.comment.isNotEmpty()) {
                                        Row {
                                            Text("${tr("Коментар", "Comment")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                            Text(selectedSupplier!!.comment, fontSize = 13.sp)
                                        }
                                    }
                                    Row {
                                        Text("${tr("Всього закуплено", "Total purchased")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text(
                                            text = String.format("%,.2f ₴", selectedSupplier!!.totalSpent),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentRed
                                        )
                                    }
                                }
                            }

                            if (uiState.purchaseHistory.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = AccentGreen.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = tr("Історія закупівель", "Purchase history"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )

                                        uiState.purchaseHistory.forEach { purchase ->
                                            PurchaseHistoryItem(purchase = purchase)
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                                ) {
                                    Text(
                                        text = tr("Немає історії закупівель", "No purchase history"),
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (editMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    if (editName.isNotBlank()) {
                                        viewModel.updateSupplier(
                                            supplierId = selectedSupplier!!.supplierId,
                                            name = editName,
                                            contact = editContact,
                                            type = editType,
                                            comment = editComment
                                        )
                                        editMode = false
                                        showDetailDialog = false
                                        selectedSupplier = null
                                        Toast.makeText(context, supplierUpdatedText, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(tr("Зберегти", "Save"))
                            }
                            TextButton(onClick = { editMode = false }) {
                                Text(tr("Скасувати", "Cancel"), color = AccentRed)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = {
                                    editName = selectedSupplier!!.name
                                    editContact = selectedSupplier!!.contact
                                    editType = selectedSupplier!!.type
                                    editComment = selectedSupplier!!.comment
                                    editMode = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text(tr("Редагувати", "Edit"))
                            }
                            TextButton(
                                onClick = {
                                    supplierToDelete = selectedSupplier
                                    showDeleteConfirmation = true
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Text(tr("Видалити", "Delete"), color = AccentRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && supplierToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                supplierToDelete = null
            },
            title = { Text(tr("Видалити постачальника?", "Delete supplier?")) },
            text = {
                Text(
                    text = "${tr("Ви впевнені, що хочете видалити", "Are you sure you want to delete")} \"${supplierToDelete!!.name}\"?\n${tr("Цю дію не можна скасувати.", "This action cannot be undone.")}",
                    color = AccentRed
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteSupplier(supplierToDelete!!.supplierId)
                            showDeleteConfirmation = false
                            showDetailDialog = false
                            selectedSupplier = null
                            supplierToDelete = null
                            Toast.makeText(context, supplierDeletedText, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(tr("Видалити", "Delete"), color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    supplierToDelete = null
                }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }

    // Діалог додавання нового постачальника (з прокруткою)
    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddDialog(false) },
            title = { Text(tr("Додати постачальника", "Add supplier")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var name by remember { mutableStateOf("") }
                    var contact by remember { mutableStateOf("") }
                    var type by remember { mutableStateOf("") }
                    var comment by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(tr("Назва / Ім'я *", "Name *")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text(tr("Контактні дані", "Contact info")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
                        placeholder = { Text(tr("Телефон, email, сайт...", "Phone, email, website...")) }
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text(tr("Тип", "Type")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
                        placeholder = { Text(tr("Офіційний, Партнер, Аукціон...", "Official, Partner, Auction...")) }
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(tr("Коментар", "Comment")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addSupplier(name, contact, type, comment)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tr("Зберегти", "Save"))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddDialog(false) }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun SupplierCard(
    supplier: SupplierWithBalance,
    onClick: () -> Unit,
    onCopyContact: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IOSIconChip(
                        icon = Icons.Default.Business,
                        tint = AccentOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = supplier.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (supplier.type.isNotEmpty()) {
                            Text(
                                text = supplier.type,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Text(
                    text = "${String.format("%,.0f", supplier.totalSpent)} ₴",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (supplier.contact.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCopyContact() }
                ) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = supplier.contact,
                        fontSize = 12.sp,
                        color = AccentBlue
                    )
                }
            }

            if (supplier.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = supplier.comment,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseHistoryItem(purchase: PurchaseWithProductName) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadiusSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = purchase.productName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(purchase.date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${tr("Куплено", "Bought")}: ${purchase.quantity} ${tr("шт.", "pcs")}",
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format("%,.2f ₴", purchase.totalAmount),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
        }
    }
}
