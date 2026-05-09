package com.numisproerp.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import android.widget.Toast
import com.numisproerp.data.dao.ProductInStock
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.WriteoffReasons
import com.numisproerp.ui.viewmodel.WriteoffViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteoffScreen(
    navController: NavHostController,
    viewModel: WriteoffViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty() && !uiState.showAddDialog) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
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
                contentDescription = tr("Назад", "Back"),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        FloatingActionButton(
            onClick = { viewModel.toggleAddDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = AccentRed
        ) {
            Icon(Icons.Default.Add, contentDescription = tr("Списати товар", "Write off item"))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = tr("Списання", "Writeoff"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            val total = uiState.writeoffs.sumOf { it.totalAmount }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tr("Загальні списання", "Total writeoffs")} (${uiState.writeoffs.size}):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%,.2f ₴", total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.writeoffs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = tr("Списань немає.\nНатисніть + щоб списати товар", "No writeoffs.\nTap + to write off an item"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.writeoffs) { item ->
                        WriteoffRow(item)
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddWriteoffDialog(
            productsInStock = uiState.productsInStock,
            errorMessage = uiState.errorMessage,
            onSubmit = { catalogId, qty, reason, comment ->
                viewModel.submitWriteoff(catalogId, qty, reason, comment)
            },
            onDismiss = { viewModel.toggleAddDialog(false) }
        )
    }
}

@Composable
private fun WriteoffRow(item: com.numisproerp.data.dao.WriteoffWithProductName) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IOSIconChip(icon = Icons.Outlined.RemoveCircle, tint = AccentRed)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${item.reason} • ${item.quantity} шт.",
                    fontSize = 12.sp,
                    color = AccentOrange
                )
                if (item.comment.isNotEmpty()) {
                    Text(
                        text = item.comment,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = dateFormat.format(Date(item.date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "-${String.format("%,.2f", item.totalAmount)} ₴",
                fontWeight = FontWeight.Bold,
                color = AccentRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWriteoffDialog(
    productsInStock: List<ProductInStock>,
    errorMessage: String,
    onSubmit: (String, Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var productExpanded by remember { mutableStateOf(false) }
    var productQuery by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf("") }

    var reasonExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf(WriteoffReasons.DEFECTIVE) }

    var quantityText by remember { mutableStateOf("1") }
    var comment by remember { mutableStateOf("") }

    val filteredProducts = remember(productQuery, productsInStock) {
        if (productQuery.isBlank()) productsInStock
        else productsInStock.filter { it.name.contains(productQuery, ignoreCase = true) }
    }
    val selectedProduct = productsInStock.find { it.catalogId == selectedProductId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Списання товару", "Write off item")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value = productQuery,
                        onValueChange = {
                            productQuery = it
                            productExpanded = true
                            if (it.isBlank()) selectedProductId = ""
                        },
                        label = { Text(tr("Товар *", "Item *")) },
                        placeholder = { Text(tr("Введіть для пошуку...", "Type to search...")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )
                    if (filteredProducts.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            filteredProducts.forEach { p ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(p.name, fontSize = 13.sp)
                                            Text(
                                                "${tr("В наявності", "In stock")}: ${p.currentStock} ${tr("шт.", "pcs")} • ${tr("сер.закуп.", "avg.purch.")} ${String.format("%,.2f", p.avgPurchasePrice)} ₴",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProductId = p.catalogId
                                        productQuery = p.name
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedProduct != null) {
                    Text(
                        text = "${tr("На складі", "In stock")}: ${selectedProduct.currentStock} ${tr("шт.", "pcs")} (${tr("сер.", "avg.")} ${String.format("%,.2f", selectedProduct.avgPurchasePrice)} ₴)",
                        fontSize = 11.sp,
                        color = AccentBlue
                    )
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(tr("Кількість *", "Quantity *")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = reasonExpanded,
                    onExpandedChange = { reasonExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(tr("Причина *", "Reason *")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = reasonExpanded,
                        onDismissRequest = { reasonExpanded = false }
                    ) {
                        WriteoffReasons.ALL.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason) },
                                onClick = {
                                    selectedReason = reason
                                    reasonExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(tr("Коментар", "Comment")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                if (selectedProduct != null && quantityText.isNotEmpty()) {
                    val qty = quantityText.toIntOrNull() ?: 0
                    val total = selectedProduct.avgPurchasePrice * qty
                    Text(
                        text = "${tr("Сума списання", "Writeoff sum")}: ${String.format("%,.2f", total)} ₴",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = AccentRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 0
                    onSubmit(selectedProductId, qty, selectedReason, comment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text(tr("Списати", "Write off"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Скасувати", "Cancel")) }
        }
    )
}
