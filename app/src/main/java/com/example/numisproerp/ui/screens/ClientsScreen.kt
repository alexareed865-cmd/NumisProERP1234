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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.numisproerp.data.dao.ClientWithBalance
import com.numisproerp.ui.i18n.tr
import com.numisproerp.data.dao.SaleWithProductName
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.ClientsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavHostController,
    viewModel: ClientsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<ClientWithBalance?>(null) }
    var editMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editTelegram by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var clientToDelete by remember { mutableStateOf<ClientWithBalance?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    LaunchedEffect(selectedClient) {
        if (selectedClient != null) {
            viewModel.loadPurchaseHistory(selectedClient!!.clientId)
        }
    }

    val phoneCopiedText = tr("Телефон скопійовано", "Phone copied")
    val telegramCopiedText = tr("Telegram скопійовано", "Telegram copied")
    val clientUpdatedText = tr("Клієнта оновлено", "Client updated")
    val clientDeletedText = tr("Клієнта видалено", "Client deleted")

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
                    Icons.Default.ArrowBack,
                    contentDescription = tr("Назад", "Back"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = tr("Клієнти", "Clients"),
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
                    placeholder = { Text(tr("Пошук за ім'ям, телефоном або містом...", "Search by name, phone or city...")) },
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
                        contentDescription = tr("Додати клієнта", "Add client"),
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
            } else if (uiState.clients.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("Немає клієнтів.\nНатисніть + щоб додати", "No clients.\nTap + to add"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.clients) { client ->
                        ClientCard(
                            client = client,
                            onClick = {
                                selectedClient = client
                                editMode = false
                                showDetailDialog = true
                            },
                            onCopyPhone = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("phone", client.phone))
                                Toast.makeText(context, phoneCopiedText, Toast.LENGTH_SHORT).show()
                            },
                            onCopyTelegram = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("telegram", client.telegram))
                                Toast.makeText(context, telegramCopiedText, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Діалог детального перегляду (залишається без змін)
    if (showDetailDialog && selectedClient != null) {
        AlertDialog(
            onDismissRequest = {
                showDetailDialog = false
                selectedClient = null
                editMode = false
            },
            title = {
                if (editMode) Text(tr("Редагувати клієнта", "Edit client"))
                else Text(selectedClient!!.name)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (editMode) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(tr("ПІБ / Нікнейм *", "Full name / Nickname *")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text(tr("Телефон", "Phone")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        )
                        OutlinedTextField(
                            value = editTelegram,
                            onValueChange = { editTelegram = it },
                            label = { Text("Telegram / Viber") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        )
                        OutlinedTextField(
                            value = editCity,
                            onValueChange = { editCity = it },
                            label = { Text(tr("Місто", "City")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        )
                        OutlinedTextField(
                            value = editNotes,
                            onValueChange = { editNotes = it },
                            label = { Text(tr("Нотатки", "Notes")) },
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
                                if (selectedClient!!.phone.isNotEmpty()) {
                                    Row {
                                        Text("${tr("Телефон", "Phone")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text(selectedClient!!.phone, fontSize = 13.sp, color = AccentOrange)
                                    }
                                }
                                if (selectedClient!!.telegram.isNotEmpty()) {
                                    Row {
                                        Text("Telegram: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text(selectedClient!!.telegram, fontSize = 13.sp, color = AccentBlue)
                                    }
                                }
                                if (selectedClient!!.city.isNotEmpty()) {
                                    Row {
                                        Text("${tr("Місто", "City")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text(selectedClient!!.city, fontSize = 13.sp)
                                    }
                                }
                                if (selectedClient!!.notes.isNotEmpty()) {
                                    Row {
                                        Text("${tr("Нотатки", "Notes")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text(selectedClient!!.notes, fontSize = 13.sp)
                                    }
                                }
                                Row {
                                    Text("${tr("Всього покупок", "Total purchases")}: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(
                                        text = String.format("%,.2f ₴", selectedClient!!.totalSpent),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGreen
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
                                        text = tr("Історія покупок", "Purchase history"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )

                                    LazyColumn(
                                        modifier = Modifier.height(200.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(uiState.purchaseHistory) { sale ->
                                            SaleHistoryItem(sale = sale)
                                        }
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
                                    text = tr("Немає історії покупок", "No purchase history"),
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (editMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    viewModel.updateClient(
                                        clientId = selectedClient!!.clientId,
                                        name = editName,
                                        phone = editPhone,
                                        telegram = editTelegram,
                                        city = editCity,
                                        notes = editNotes
                                    )
                                    editMode = false
                                    showDetailDialog = false
                                    selectedClient = null
                                    Toast.makeText(context, clientUpdatedText, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(tr("Зберегти", "Save"), color = AccentGreen)
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
                                editName = selectedClient!!.name
                                editPhone = selectedClient!!.phone
                                editTelegram = selectedClient!!.telegram
                                editCity = selectedClient!!.city
                                editNotes = selectedClient!!.notes
                                editMode = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text(tr("Редагувати", "Edit"))
                        }
                        TextButton(
                            onClick = {
                                clientToDelete = selectedClient
                                showDeleteConfirmation = true
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Text(tr("Видалити", "Delete"), color = AccentRed)
                        }
                    }
                }
            },
            dismissButton = {
                if (!editMode) {
                    TextButton(onClick = {
                        showDetailDialog = false
                        selectedClient = null
                    }) {
                        Text(tr("Закрити", "Close"))
                    }
                }
            }
        )
    }

    if (showDeleteConfirmation && clientToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                clientToDelete = null
            },
            title = { Text(tr("Видалити клієнта?", "Delete client?")) },
            text = {
                Text(
                    text = "${tr("Ви впевнені, що хочете видалити", "Are you sure you want to delete")} \"${clientToDelete!!.name}\"?\n${tr("Цю дію не можна скасувати.", "This action cannot be undone.")}",
                    color = AccentRed
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteClient(clientToDelete!!.clientId)
                            showDeleteConfirmation = false
                            showDetailDialog = false
                            selectedClient = null
                            clientToDelete = null
                            Toast.makeText(context, clientDeletedText, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(tr("Видалити", "Delete"), color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    clientToDelete = null
                }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }

    // Діалог додавання нового клієнта (з прокруткою)
    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddDialog(false) },
            title = { Text(tr("Додати клієнта", "Add client")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var name by remember { mutableStateOf("") }
                    var phone by remember { mutableStateOf("") }
                    var telegram by remember { mutableStateOf("") }
                    var city by remember { mutableStateOf("") }
                    var notes by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(tr("ПІБ / Нікнейм *", "Full name / Nickname *")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(tr("Телефон", "Phone")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )
                    OutlinedTextField(
                        value = telegram,
                        onValueChange = { telegram = it },
                        label = { Text("Telegram / Viber") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
                        placeholder = { Text("@username") }
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(tr("Місто", "City")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(tr("Нотатки", "Notes")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addClient(name, phone, telegram, city, notes)
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
fun ClientCard(
    client: ClientWithBalance,
    onClick: () -> Unit,
    onCopyPhone: () -> Unit,
    onCopyTelegram: () -> Unit
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
                        icon = Icons.Default.Person,
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = client.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (client.city.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = client.city,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${String.format("%,.0f", client.totalSpent)} ₴",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (client.phone.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCopyPhone() }
                    ) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = client.phone,
                            fontSize = 12.sp,
                            color = AccentOrange
                        )
                    }
                }

                if (client.telegram.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCopyTelegram() }
                    ) {
                        Icon(
                            Icons.Outlined.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = client.telegram,
                            fontSize = 12.sp,
                            color = AccentBlue
                        )
                    }
                }
            }

            if (client.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = client.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SaleHistoryItem(sale: SaleWithProductName) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadiusSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sale.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(sale.date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${sale.quantity} ${tr("шт.", "pcs")}",
                fontSize = 12.sp,
                modifier = Modifier.width(60.dp)
            )
            Text(
                text = String.format("%,.2f ₴", sale.totalAmount),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen,
                modifier = Modifier.width(100.dp)
            )
                    }
    }
}
