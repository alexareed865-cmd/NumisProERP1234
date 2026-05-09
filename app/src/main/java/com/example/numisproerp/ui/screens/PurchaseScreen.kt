package com.numisproerp.ui.screens

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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.data.dao.ProductForSelection
import com.numisproerp.ui.i18n.tr
import com.numisproerp.data.dao.SupplierForSelection
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Supplier
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.viewmodel.PurchaseViewModel
import kotlinx.coroutines.launch

data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val additionalCosts: Double,
    val totalAmount: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    navController: NavHostController,
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredProducts by remember { mutableStateOf<List<ProductForSelection>>(emptyList()) }
    var selectedProductForCart by remember { mutableStateOf<ProductForSelection?>(null) }
    var cartQuantity by remember { mutableStateOf("") }
    var cartPrice by remember { mutableStateOf("") }
    var cartAdditionalCosts by remember { mutableStateOf("") }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var newSupplierName by remember { mutableStateOf("") }
    var newSupplierContact by remember { mutableStateOf("") }
    var newSupplierType by remember { mutableStateOf("") }
    var newSupplierComment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(searchQuery, uiState.products) {
        if (searchQuery.isNotBlank()) {
            filteredProducts = uiState.products.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                        product.series.contains(searchQuery, ignoreCase = true)
            }
        } else {
            filteredProducts = emptyList()
        }
    }

    val totalPurchaseAmount = cartItems.sumOf { it.totalAmount }

    val purchaseDoneText = tr("Закупівлю проведено успішно", "Purchase completed successfully")
    val supplierAddedText = tr("Постачальника додано", "Supplier added")

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
                text = tr("Закупівля", "Purchase"),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SupplierDropdown(
                        modifier = Modifier.weight(1f),
                        selectedSupplierId = uiState.selectedSupplierId,
                        suppliers = uiState.suppliers,
                        onSupplierSelected = { viewModel.updateSelectedSupplier(it) }
                    )
                    IconButton(
                        onClick = { showSupplierDialog = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = tr("Новий постачальник", "New supplier"))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Товари в закупівлі", "Items in purchase"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddProductDialog = true },
                        enabled = uiState.selectedSupplierId.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = tr("Додати товар", "Add item"))
                        Text(tr("Додати", "Add"), modifier = Modifier.padding(start = 4.dp))
                    }
                }

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
                            CartItemCard(
                                item = item,
                                onRemove = {
                                    cartItems = cartItems.filter { it.productId != item.productId }
                                }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentOrange.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tr("Загальна сума:", "Total amount:"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%,.2f ₴", totalPurchaseAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (cartItems.isNotEmpty() && uiState.selectedSupplierId.isNotEmpty()) {
                            scope.launch {
                                for (item in cartItems) {
                                    val purchase = Purchase(
                                        purchaseId = "PUR-${System.currentTimeMillis()}-${item.productId}",
                                        date = System.currentTimeMillis(),
                                        catalogId = item.productId,
                                        supplierId = uiState.selectedSupplierId,
                                        quantity = item.quantity,
                                        pricePerUnit = item.pricePerUnit,
                                        additionalCosts = item.additionalCosts,
                                        totalAmount = item.totalAmount
                                    )
                                    viewModel.repository.insertPurchase(purchase)
                                }
                                cartItems = emptyList()
                                viewModel.clearSuccessMessage()
                                navController.popBackStack()
                                Toast.makeText(context, purchaseDoneText, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
                    enabled = cartItems.isNotEmpty() && uiState.selectedSupplierId.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(tr("Провести закупівлю", "Submit purchase"), modifier = Modifier.padding(start = 8.dp))
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

    // Діалог додавання товару в кошик (з пошуком)
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text(tr("Додати товар", "Add item")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
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
                                                "${product.series} • ${product.category}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedProductForCart != null) {
                        Text(
                            text = "${tr("Вибрано", "Selected")}: ${selectedProductForCart!!.name}",
                            color = AccentGreen
                        )

                        OutlinedTextField(
                            value = cartQuantity,
                            onValueChange = { cartQuantity = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Кількість", "Quantity")) },
                            placeholder = { Text(tr("шт.", "pcs")) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = cartPrice,
                            onValueChange = { cartPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Ціна за одиницю", "Unit price")) },
                            placeholder = { Text("₴") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = cartAdditionalCosts,
                            onValueChange = { cartAdditionalCosts = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Додаткові витрати", "Additional costs")) },
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
                            val additional = cartAdditionalCosts.toDoubleOrNull() ?: 0.0
                            val newItem = CartItem(
                                productId = selectedProductForCart!!.catalogId,
                                productName = selectedProductForCart!!.name,
                                quantity = qty,
                                pricePerUnit = price,
                                additionalCosts = additional,
                                totalAmount = (price * qty) + additional
                            )
                            cartItems = cartItems + newItem
                            selectedProductForCart = null
                            cartQuantity = ""
                            cartPrice = ""
                            cartAdditionalCosts = ""
                            showAddProductDialog = false
                        }
                    },
                    enabled = selectedProductForCart != null &&
                            cartQuantity.toIntOrNull() != null &&
                            cartQuantity.toIntOrNull()!! > 0 &&
                            cartPrice.toDoubleOrNull() != null &&
                            cartPrice.toDoubleOrNull()!! > 0
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

    // Діалог додавання нового постачальника
    if (showSupplierDialog) {
        AlertDialog(
            onDismissRequest = { showSupplierDialog = false },
            title = { Text(tr("Новий постачальник", "New supplier")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newSupplierName,
                        onValueChange = { newSupplierName = it },
                        label = { Text(tr("Назва / Ім'я *", "Name *")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newSupplierContact,
                        onValueChange = { newSupplierContact = it },
                        label = { Text(tr("Контактні дані", "Contact info")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newSupplierType,
                        onValueChange = { newSupplierType = it },
                        label = { Text(tr("Тип", "Type")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newSupplierComment,
                        onValueChange = { newSupplierComment = it },
                        label = { Text(tr("Коментар", "Comment")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSupplierName.isNotBlank()) {
                            scope.launch {
                                val newSupplier = Supplier(
                                    supplierId = "SUP-${System.currentTimeMillis()}",
                                    name = newSupplierName,
                                    contact = newSupplierContact,
                                    type = newSupplierType,
                                    comment = newSupplierComment
                                )
                                viewModel.repository.insertSupplier(newSupplier)
                                viewModel.loadData()
                                newSupplierName = ""
                                newSupplierContact = ""
                                newSupplierType = ""
                                newSupplierComment = ""
                                showSupplierDialog = false
                                Toast.makeText(context, supplierAddedText, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text(tr("Зберегти", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupplierDialog = false }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
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
                Text(
                    text = "${tr("Сума", "Sum")}: ${String.format("%,.2f", item.totalAmount)} ₴",
                    fontSize = 12.sp,
                    color = AccentGreen
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = tr("Видалити", "Delete"))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDropdown(
    modifier: Modifier = Modifier,
    selectedSupplierId: String,
    suppliers: List<SupplierForSelection>,
    onSupplierSelected: (String) -> Unit
) {
    val selectedSupplier = suppliers.find { it.supplierId == selectedSupplierId }
    var query by remember { mutableStateOf(selectedSupplier?.name ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var suppressSync by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSupplierId) {
        if (suppressSync) {
            suppressSync = false
        } else {
            query = selectedSupplier?.name ?: ""
        }
    }

    val filtered = remember(query, suppliers) {
        if (query.isBlank()) suppliers
        else suppliers.filter { it.name.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                query = newValue
                expanded = true
                if (selectedSupplierId.isNotEmpty() && newValue != selectedSupplier?.name) {
                    suppressSync = true
                    onSupplierSelected("")
                }
            },
            label = { Text(tr("Постачальник *", "Supplier *")) },
            placeholder = { Text(tr("Введіть для пошуку...", "Type to search...")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier.menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        if (filtered.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filtered.forEach { supplier ->
                    DropdownMenuItem(
                        text = { Text(supplier.name) },
                        onClick = {
                            onSupplierSelected(supplier.supplierId)
                            query = supplier.name
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDropdown(
    selectedProductId: String,
    products: List<ProductForSelection>,
    onProductSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProduct = products.find { it.catalogId == selectedProductId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedProduct?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(tr("Товар *", "Item *")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.forEach { product ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${product.series} • ${product.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    onClick = {
                        onProductSelected(product.catalogId)
                        expanded = false
                    }
                )
            }
        }
    }
}
