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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.outlined.Clear
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.data.dao.ClientForSelection
import com.numisproerp.ui.i18n.tr
import com.numisproerp.data.dao.ProductInStock
import com.numisproerp.data.entities.Client
import com.numisproerp.data.entities.Sale
import com.numisproerp.di.AppDatabaseEntryPoint
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.viewmodel.SaleViewModel
import com.numisproerp.utils.PdfReportGenerator
import com.numisproerp.utils.ReceiptShareUtil
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

data class SaleCartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val additionalCosts: Double,
    val totalAmount: Double,
    val netProfit: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    navController: NavHostController,
    viewModel: SaleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()

    var cartItems by remember { mutableStateOf<List<SaleCartItem>>(emptyList()) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var receiptDialogPath by remember { mutableStateOf<String?>(null) }

    val database = remember {
        EntryPointAccessors
            .fromApplication(context.applicationContext, AppDatabaseEntryPoint::class.java)
            .appDatabase()
    }

    var searchQuery by remember { mutableStateOf("") }
    var filteredProducts by remember { mutableStateOf<List<ProductInStock>>(emptyList()) }
    var selectedProductForCart by remember { mutableStateOf<ProductInStock?>(null) }
    var cartQuantity by remember { mutableStateOf("") }
    var cartPrice by remember { mutableStateOf("") }
    var cartAdditionalCosts by remember { mutableStateOf("") }

    var showClientDialog by remember { mutableStateOf(false) }
    var newClientName by remember { mutableStateOf("") }
    var newClientPhone by remember { mutableStateOf("") }
    var newClientTelegram by remember { mutableStateOf("") }
    var newClientCity by remember { mutableStateOf("") }
    var newClientNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(searchQuery, uiState.products) {
        if (searchQuery.isNotBlank()) {
            filteredProducts = uiState.products.filter { product ->
                product.currentStock > 0 && (
                        product.name.contains(searchQuery, ignoreCase = true) ||
                                product.series.contains(searchQuery, ignoreCase = true)
                        )
            }
        } else {
            filteredProducts = emptyList()
        }
    }

    val totalSaleAmount = cartItems.sumOf { it.totalAmount }
    val totalNetProfit = cartItems.sumOf { it.netProfit }

    val saleDoneText = tr("Продаж проведено успішно", "Sale completed successfully")
    val notEnoughStockText = tr("Недостатньо товару на складі", "Not enough item in stock")
    val fillAllFieldsText = tr("Заповніть всі поля", "Fill in all fields")
    val clientAddedText = tr("Клієнта додано", "Client added")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Верхній рядок: кнопка назад + назва сторінки по центру
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = tr("Назад", "Back"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = tr("Продаж", "Sale"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Пустий Spacer для балансу (щоб назва була по центру)
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                // Пошук клієнта
                OutlinedTextField(
                    value = uiState.clientSearchQuery,
                    onValueChange = { viewModel.updateClientSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(tr("Пошук клієнта за ім'ям...", "Search client by name...")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.clientSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateClientSearchQuery("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = tr("Очистити", "Clear"))
                            }
                        }
                    },
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                )

                if (uiState.clientSearchQuery.isNotEmpty() && uiState.filteredClients.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                    ) {
                        Column {
                            uiState.filteredClients.forEach { client ->
                                TextButton(
                                    onClick = {
                                        viewModel.updateSelectedClient(client.clientId)
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(client.name, fontWeight = FontWeight.Medium)
                                        if (uiState.selectedClientId == client.clientId) {
                                            Text(tr("Вибрано", "Selected"), color = AccentGreen, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Відображення вибраного клієнта
                if (uiState.selectedClientId.isNotEmpty()) {
                    val selectedClient = uiState.clients.find { it.clientId == uiState.selectedClientId }
                    if (selectedClient != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentGreen.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${tr("Вибрано", "Selected")}: ${selectedClient.name}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = { viewModel.updateSelectedClient("") }) {
                                        Text(tr("Змінити", "Change"), color = AccentRed)
                                    }
                                }
                            }
                            IconButton(
                                onClick = { showClientDialog = true },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = tr("Новий клієнт", "New client"))
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showClientDialog = true },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = tr("Новий клієнт", "New client"))
                        }
                    }
                }

                // Товари в продажу
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Товари в продажу", "Items in sale"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedProductForCart = null
                            cartQuantity = ""
                            cartPrice = ""
                            cartAdditionalCosts = ""
                            showAddProductDialog = true
                        },
                        enabled = uiState.selectedClientId.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = tr("Додати товар", "Add item"))
                        Text(tr("Додати", "Add"), modifier = Modifier.padding(start = 4.dp))
                    }
                }

                // Кошик
                if (cartItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                    ) {
                        Text(
                            text = tr("Кошик порожній. Додайте товари", "Cart is empty. Add items"),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(cartItems) { item ->
                            SaleCartItemCard(
                                item = item,
                                onRemove = {
                                    cartItems = cartItems.filter { it.productId != item.productId }
                                }
                            )
                        }
                    }
                }

                // Загальна сума та прибуток
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentOrange.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
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
                            Text(
                                text = tr("Загальна сума:", "Total amount:"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%,.2f ₴", totalSaleAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tr("Очікуваний прибуток:", "Expected profit:"),
                                fontSize = 14.sp
                            )
                            Text(
                                text = String.format("%,.2f ₴", totalNetProfit),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalNetProfit > 0) AccentGreen else AccentRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (cartItems.isNotEmpty() && uiState.selectedClientId.isNotEmpty()) {
                            scope.launch {
                                val saleDate = System.currentTimeMillis()
                                val itemsSnapshot = cartItems.toList()
                                for (item in itemsSnapshot) {
                                    val sale = Sale(
                                        saleId = "SAL-${saleDate}-${item.productId}",
                                        date = saleDate,
                                        catalogId = item.productId,
                                        clientId = uiState.selectedClientId,
                                        quantity = item.quantity,
                                        pricePerUnit = item.pricePerUnit,
                                        additionalCosts = item.additionalCosts,
                                        netProfit = item.netProfit,
                                        totalAmount = item.totalAmount
                                    )
                                    viewModel.repository.insertSale(sale)
                                }

                                val clientName = uiState.clients
                                    .firstOrNull { it.clientId == uiState.selectedClientId }?.name
                                    ?: ""
                                val receiptItems = itemsSnapshot.map {
                                    PdfReportGenerator.ReceiptItem(
                                        productName = it.productName,
                                        quantity = it.quantity,
                                        pricePerUnit = it.pricePerUnit,
                                        additionalCosts = it.additionalCosts,
                                        totalAmount = it.totalAmount
                                    )
                                }
                                val generator = PdfReportGenerator(database)
                                val result = generator.generateSaleReceipt(
                                    context, clientName, saleDate, receiptItems
                                )
                                cartItems = emptyList()
                                viewModel.clearSuccessMessage()
                                Toast.makeText(context, saleDoneText, Toast.LENGTH_LONG).show()
                                if (result.success) {
                                    receiptDialogPath = result.filePath
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
                    enabled = cartItems.isNotEmpty() && uiState.selectedClientId.isNotEmpty()
                ) {
                    Icon(Icons.Default.Sell, contentDescription = null)
                    Text(tr("Провести продаж", "Submit sale"), modifier = Modifier.padding(start = 8.dp))
                }

                if (uiState.showSuccessMessage) {
                    LaunchedEffect(Unit) {
                        viewModel.clearSuccessMessage()
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    // Авто-відкриття PDF-чека одразу після продажу.
    LaunchedEffect(receiptDialogPath) {
        receiptDialogPath?.let { path ->
            ReceiptShareUtil.openPdf(context, path)
        }
    }

    // Діалог пост-продажу: чек PDF, дії — поділитися/зберегти/не зберігати
    receiptDialogPath?.let { path ->
        val downloadedText = tr("Збережено в Downloads", "Saved to Downloads")
        AlertDialog(
            onDismissRequest = {},
            title = { Text(tr("Чек продажу", "Sale receipt")) },
            text = {
                Text(
                    tr(
                        "Чек у форматі PDF готовий. Як вчинити з ним?",
                        "PDF receipt is ready. What would you like to do with it?"
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ReceiptShareUtil.share(context, path)
                }) {
                    Text(tr("Поділитися", "Share"))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        val saved = ReceiptShareUtil.saveToDownloads(context, path)
                        if (saved != null) {
                            Toast.makeText(context, "$downloadedText: $saved", Toast.LENGTH_LONG).show()
                        }
                        receiptDialogPath = null
                        navController.popBackStack()
                    }) {
                        Text(tr("Зберегти", "Save"))
                    }
                    TextButton(onClick = {
                        ReceiptShareUtil.discard(path)
                        receiptDialogPath = null
                        navController.popBackStack()
                    }) {
                        Text(tr("Не зберігати", "Discard"))
                    }
                }
            }
        )
    }

    // Діалог додавання товару (з прокруткою)
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text(tr("Додати товар", "Add item")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isNotEmpty()) {
                                selectedProductForCart = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(tr("Пошук товару...", "Search item...")) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    selectedProductForCart = null
                                }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = tr("Очистити", "Clear"))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (searchQuery.isNotEmpty() && filteredProducts.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                filteredProducts.forEach { product ->
                                    TextButton(
                                        onClick = {
                                            selectedProductForCart = product
                                            searchQuery = ""
                                            filteredProducts = emptyList()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                product.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "${tr("В наявності", "In stock")}: ${product.currentStock} ${tr("шт.", "pcs")} • ${product.series}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (searchQuery.isNotEmpty() && filteredProducts.isEmpty()) {
                        Text(
                            text = tr("Товарів не знайдено або немає в наявності", "No items found or none in stock"),
                            modifier = Modifier.padding(8.dp),
                            color = AccentRed,
                            fontSize = 14.sp
                        )
                    }

                    if (selectedProductForCart != null) {
                        Text(
                            text = "${tr("Вибрано", "Selected")}: ${selectedProductForCart!!.name}",
                            color = AccentGreen
                        )

                        Text(
                            text = "${tr("Доступно на складі", "Available in stock")}: ${selectedProductForCart!!.currentStock} ${tr("шт.", "pcs")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        OutlinedTextField(
                            value = cartQuantity,
                            onValueChange = { cartQuantity = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Кількість", "Quantity")) },
                            placeholder = { Text(tr("шт.", "pcs")) },
                            isError = cartQuantity.toIntOrNull()?.let { it > (selectedProductForCart?.currentStock ?: 0) } == true,
                            supportingText = {
                                if (cartQuantity.toIntOrNull()?.let { it > (selectedProductForCart?.currentStock ?: 0) } == true) {
                                    Text(tr("Недостатньо на складі", "Not enough in stock"), color = AccentRed)
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = cartPrice,
                            onValueChange = { cartPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Ціна продажу за одиницю", "Sale price per unit")) },
                            placeholder = { Text("₴") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = cartAdditionalCosts,
                            onValueChange = { cartAdditionalCosts = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Витрати на упаковку/доставку", "Packaging/Delivery costs")) },
                            placeholder = { Text("₴ (необов'язково)") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = cartQuantity.toIntOrNull()
                        val price = cartPrice.toDoubleOrNull()
                        if (selectedProductForCart != null && qty != null && qty > 0 && price != null && price > 0) {
                            if (qty <= (selectedProductForCart?.currentStock ?: 0)) {
                                val additional = cartAdditionalCosts.toDoubleOrNull() ?: 0.0
                                val totalAmount = (price * qty) + additional
                                val netProfit = (price - (selectedProductForCart?.avgPurchasePrice ?: 0.0)) * qty - additional
                                val newItem = SaleCartItem(
                                    productId = selectedProductForCart!!.catalogId,
                                    productName = selectedProductForCart!!.name,
                                    quantity = qty,
                                    pricePerUnit = price,
                                    additionalCosts = additional,
                                    totalAmount = totalAmount,
                                    netProfit = netProfit
                                )
                                cartItems = cartItems + newItem
                                selectedProductForCart = null
                                cartQuantity = ""
                                cartPrice = ""
                                cartAdditionalCosts = ""
                                showAddProductDialog = false
                            } else {
                                Toast.makeText(context, notEnoughStockText, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, fillAllFieldsText, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedProductForCart != null &&
                            cartQuantity.toIntOrNull() != null &&
                            cartQuantity.toIntOrNull()!! > 0 &&
                            cartPrice.toDoubleOrNull() != null &&
                            cartPrice.toDoubleOrNull()!! > 0 &&
                            (cartQuantity.toIntOrNull() ?: 0) <= (selectedProductForCart?.currentStock ?: 0)
                ) {
                    Text(tr("Додати", "Add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }

    // Діалог додавання нового клієнта (з прокруткою)
    if (showClientDialog) {
        AlertDialog(
            onDismissRequest = { showClientDialog = false },
            title = { Text(tr("Новий клієнт", "New client")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newClientName,
                        onValueChange = { newClientName = it },
                        label = { Text(tr("Ім'я / Назва *", "Name *")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newClientPhone,
                        onValueChange = { newClientPhone = it },
                        label = { Text(tr("Телефон", "Phone")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newClientTelegram,
                        onValueChange = { newClientTelegram = it },
                        label = { Text("Telegram") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newClientCity,
                        onValueChange = { newClientCity = it },
                        label = { Text(tr("Місто", "City")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newClientNotes,
                        onValueChange = { newClientNotes = it },
                        label = { Text(tr("Нотатки", "Notes")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newClientName.isNotBlank()) {
                            scope.launch {
                                val newClient = Client(
                                    clientId = "CLI-${System.currentTimeMillis()}",
                                    name = newClientName,
                                    phone = newClientPhone,
                                    telegram = newClientTelegram,
                                    city = newClientCity,
                                    notes = newClientNotes
                                )
                                viewModel.repository.insertClient(newClient)
                                viewModel.loadData()
                                newClientName = ""
                                newClientPhone = ""
                                newClientTelegram = ""
                                newClientCity = ""
                                newClientNotes = ""
                                showClientDialog = false
                                Toast.makeText(context, clientAddedText, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text(tr("Зберегти", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClientDialog = false }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun SaleCartItemCard(
    item: SaleCartItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "${item.quantity} шт. × ${String.format("%,.2f", item.pricePerUnit)} ₴",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (item.additionalCosts > 0) {
                    Text(
                        text = "${tr("Витрати", "Costs")}: ${String.format("%,.2f", item.additionalCosts)} ₴",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${tr("Сума", "Sum")}: ${String.format("%,.2f", item.totalAmount)} ₴",
                        fontSize = 12.sp,
                        color = AccentGreen
                    )
                    Text(
                        text = "${tr("Прибуток", "Profit")}: ${String.format("%,.2f", item.netProfit)} ₴",
                        fontSize = 12.sp,
                        color = if (item.netProfit > 0) AccentGreen else AccentRed
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = tr("Видалити", "Delete"))
            }
        }
    }
}
