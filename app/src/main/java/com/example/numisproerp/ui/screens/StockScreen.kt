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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.data.entities.Product
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.StockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    navController: NavHostController,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
        viewModel.loadCategories()
        viewModel.loadMaterials()
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
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(80.dp))
                Text(
                    text = tr("Склад", "Stock"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.toggleSortDialog(true) }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = tr("Сортувати", "Sort"), tint = AccentBlue)
                }
                IconButton(onClick = { /* show material filter via dialog */ viewModel.toggleMaterialFilter() }) {
                    Icon(Icons.Default.FilterList, contentDescription = tr("Фільтр матеріал", "Filter material"), tint = AccentBlue)
                }
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(tr("Пошук за назвою або серією...", "Search by name or series...")) },
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

            if (uiState.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory.isBlank(),
                            onClick = { viewModel.updateSelectedCategory("") },
                            label = { Text(tr("Усі", "All")) }
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = {
                                if (uiState.selectedCategory == category) {
                                    viewModel.updateSelectedCategory("")
                                } else {
                                    viewModel.updateSelectedCategory(category)
                                }
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val totalItems = uiState.products.sumOf { it.currentStock }
            val totalValue = uiState.products.sumOf { it.currentStock * it.avgPurchasePrice }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tr("Всього товарів на складі:", "Total items in stock:"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$totalItems шт.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tr("Загальна вартість залишків:", "Total stock value:"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format("%,.2f ₴", totalValue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
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
            } else if (uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("Немає товарів в наявності.\nДодайте товари через Закупівлю", "No items in stock.\nAdd via Purchase"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductCard(
                            product = product,
                            onClick = {
                                scope.launch {
                                    selectedProduct = viewModel.getProductDetails(product.catalogId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    selectedProduct?.let { product ->
        ProductDetailDialog(product = product, onDismiss = { selectedProduct = null })
    }

    // Sort dialog
    if (uiState.showSortDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSortDialog(false) },
            title = { Text(tr("Сортувати", "Sort")) },
            text = {
                Column {
                    listOf(
                        "name" to tr("За назвою", "By name"),
                        "quantity_desc" to tr("За кількістю (спадання)", "By quantity (desc)"),
                        "quantity_asc" to tr("За кількістю (зростання)", "By quantity (asc)"),
                        "price_desc" to tr("За ціною (спадання)", "By price (desc)"),
                        "price_asc" to tr("За ціною (зростання)", "By price (asc)"),
                        "category" to tr("За категорією", "By category"),
                        "material" to tr("За матеріалом", "By material")
                    ).forEach { (value, label) ->
                        TextButton(
                            onClick = {
                                viewModel.setSortBy(value)
                                viewModel.toggleSortDialog(false)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (uiState.sortBy == value) "• $label" else label,
                                fontWeight = if (uiState.sortBy == value) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSortDialog(false) }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }

    // Material filter dialog
    if (uiState.showMaterialDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleMaterialFilter() },
            title = { Text(tr("Фільтр за матеріалом", "Filter by material")) },
            text = {
                Column {
                    TextButton(
                        onClick = { viewModel.updateFilterMaterial("") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tr("Усі матеріали", "All materials"), fontWeight = FontWeight.Bold)
                    }
                    uiState.materials.forEach { material ->
                        TextButton(
                            onClick = { viewModel.updateFilterMaterial(material) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (uiState.filterMaterial == material) "• $material" else material,
                                fontWeight = if (uiState.filterMaterial == material) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleMaterialFilter() }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun ProductCard(
    product: com.numisproerp.data.dao.ProductWithStock,
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (product.series.isNotBlank()) {
                    Text(
                        text = product.series,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (product.category.isNotBlank()) {
                        Text(
                            text = product.category,
                            fontSize = 10.sp,
                            color = AccentBlue,
                            modifier = Modifier
                                .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (product.material.isNotBlank()) {
                        Text(
                            text = product.material,
                            fontSize = 10.sp,
                            color = AccentOrange,
                            modifier = Modifier
                                .background(AccentOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${product.currentStock} ${tr("шт.", "pcs")}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (product.currentStock > 0) AccentGreen else AccentRed
                )
                Text(
                    text = "${tr("Закупівля", "Purchase")}: ${String.format("%,.2f", product.avgPurchasePrice)} ₴",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
