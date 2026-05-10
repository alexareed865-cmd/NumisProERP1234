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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
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
import com.numisproerp.ui.components.SortFilterRow
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
                IconButton(onClick = { viewModel.toggleFilterDialog(true) }) {
                    Icon(Icons.Default.FilterList, contentDescription = tr("Фільтр", "Filter"), tint = AccentBlue)
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

            val activeStockFilters = listOfNotNull(
                if (uiState.filterMaterial.isNotEmpty())
                    tr("Матеріал", "Material") + ": " + uiState.filterMaterial to { viewModel.updateFilterMaterial("") }
                else null,
                if (uiState.filterCategory.isNotEmpty())
                    tr("Категорія", "Category") + ": " + uiState.filterCategory to { viewModel.updateFilterCategory("") }
                else null,
                if (uiState.filterQuality.isNotEmpty())
                    tr("Якість", "Quality") + ": " + uiState.filterQuality to { viewModel.updateFilterQuality("") }
                else null,
                if (uiState.filterSeries.isNotEmpty())
                    tr("Серія", "Series") + ": " + uiState.filterSeries to { viewModel.updateFilterSeries("") }
                else null,
                if (uiState.filterNominal.isNotEmpty())
                    tr("Номінал", "Nominal") + ": " + uiState.filterNominal to { viewModel.updateFilterNominal("") }
                else null
            )
            if (activeStockFilters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(activeStockFilters) { (label, onRemove) ->
                        androidx.compose.material3.AssistChip(
                            onClick = { onRemove() },
                            label = { Text(label, fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = tr("Прибрати", "Remove"),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    item {
                        TextButton(onClick = { viewModel.clearAllFilters() }) {
                            Text(tr("Очистити все", "Clear all"), fontSize = 12.sp)
                        }
                    }
                }
            }

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
        val imageUrls = viewModel.getProductImageUrls(product)
        ProductDetailDialog(
            product = product,
            imageUrlFront = imageUrls.first,
            imageUrlBack = imageUrls.second,
            onDismiss = { selectedProduct = null }
        )
    }

    // Sort dialog
    if (uiState.showSortDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSortDialog(false) },
            title = { Text(tr("Сортувати", "Sort")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("name", tr("За назвою", "By name"), Icons.Default.SortByAlpha),
                        Triple("quantity_desc", tr("За кількістю (спадання)", "By quantity (desc)"), Icons.AutoMirrored.Filled.TrendingDown),
                        Triple("quantity_asc", tr("За кількістю (зростання)", "By quantity (asc)"), Icons.AutoMirrored.Filled.TrendingUp),
                        Triple("price_desc", tr("За ціною (спадання)", "By price (desc)"), Icons.AutoMirrored.Filled.TrendingDown),
                        Triple("price_asc", tr("За ціною (зростання)", "By price (asc)"), Icons.AutoMirrored.Filled.TrendingUp),
                        Triple("category", tr("За категорією", "By category"), Icons.Default.Apps),
                        Triple("material", tr("За матеріалом", "By material"), Icons.Default.Layers)
                    ).forEach { (value, label, icon) ->
                        SortFilterRow(
                            icon = icon,
                            label = label,
                            selected = uiState.sortBy == value,
                            onClick = {
                                viewModel.setSortBy(value)
                                viewModel.toggleSortDialog(false)
                            }
                        )
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

    // Tree-style filter dialog (material → sub-options, category → sub-options, etc.)
    if (uiState.showFilterDialog) {
        StockFilterDialog(
            uiState = uiState,
            onDismiss = { viewModel.toggleFilterDialog(false) },
            onMaterialSelected = { viewModel.updateFilterMaterial(it) },
            onCategorySelected = { viewModel.updateFilterCategory(it) },
            onQualitySelected = { viewModel.updateFilterQuality(it) },
            onSeriesSelected = { viewModel.updateFilterSeries(it) },
            onNominalSelected = { viewModel.updateFilterNominal(it) },
            onClearAll = { viewModel.clearAllFilters() }
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

@Composable
private fun StockFilterDialog(
    uiState: com.numisproerp.ui.viewmodel.StockUiState,
    onDismiss: () -> Unit,
    onMaterialSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onQualitySelected: (String) -> Unit,
    onSeriesSelected: (String) -> Unit,
    onNominalSelected: (String) -> Unit,
    onClearAll: () -> Unit
) {
    var activeDimension by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (activeDimension == null) tr("Фільтр", "Filter")
                else tr("Оберіть значення", "Select value")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (activeDimension == null) {
                    StockFilterDimensionRow(
                        tr("Матеріал", "Material"), Icons.Default.Layers,
                        uiState.filterMaterial, uiState.materials.isEmpty()
                    ) { activeDimension = "material" }
                    StockFilterDimensionRow(
                        tr("Категорія", "Category"), Icons.Default.Apps,
                        uiState.filterCategory, uiState.categories.isEmpty()
                    ) { activeDimension = "category" }
                    StockFilterDimensionRow(
                        tr("Якість", "Quality"), Icons.Default.Star,
                        uiState.filterQuality, uiState.qualities.isEmpty()
                    ) { activeDimension = "quality" }
                    StockFilterDimensionRow(
                        tr("Серія", "Series"), Icons.Default.Category,
                        uiState.filterSeries, uiState.seriesList.isEmpty()
                    ) { activeDimension = "series" }
                    StockFilterDimensionRow(
                        tr("Номінал", "Nominal"), Icons.Default.Numbers,
                        uiState.filterNominal, uiState.nominals.isEmpty()
                    ) { activeDimension = "nominal" }
                } else {
                    val (options, currentValue, apply) = when (activeDimension) {
                        "material" -> Triple(uiState.materials, uiState.filterMaterial, onMaterialSelected)
                        "category" -> Triple(uiState.categories, uiState.filterCategory, onCategorySelected)
                        "quality" -> Triple(uiState.qualities, uiState.filterQuality, onQualitySelected)
                        "series" -> Triple(uiState.seriesList, uiState.filterSeries, onSeriesSelected)
                        "nominal" -> Triple(uiState.nominals, uiState.filterNominal, onNominalSelected)
                        else -> Triple(emptyList(), "", {} as (String) -> Unit)
                    }
                    val dimIcon = when (activeDimension) {
                        "material" -> Icons.Default.Layers
                        "category" -> Icons.Default.Apps
                        "quality" -> Icons.Default.Star
                        "series" -> Icons.Default.Category
                        "nominal" -> Icons.Default.Numbers
                        else -> Icons.Default.Tune
                    }

                    SortFilterRow(
                        icon = Icons.Default.FilterList,
                        label = tr("Усі", "All"),
                        selected = currentValue.isEmpty(),
                        onClick = { apply(""); activeDimension = null }
                    )
                    options.forEach { value ->
                        SortFilterRow(
                            icon = dimIcon,
                            label = value,
                            selected = currentValue == value,
                            onClick = { apply(value); activeDimension = null }
                        )
                    }
                    if (options.isEmpty()) {
                        Text(
                            tr("Немає значень для цього критерію", "No values for this criterion"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (activeDimension == null) {
                TextButton(onClick = { onClearAll() }) {
                    Text(tr("Очистити", "Clear all"))
                }
            } else {
                TextButton(onClick = { activeDimension = null }) {
                    Text(tr("Назад", "Back"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Закрити", "Close")) }
        }
    )
}

@Composable
private fun StockFilterDimensionRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: String,
    empty: Boolean,
    onClick: () -> Unit
) {
    SortFilterRow(
        icon = icon,
        label = label,
        selected = selected.isNotEmpty(),
        onClick = if (empty) ({}) else onClick,
        showRadio = false,
        trailing = {
            Text(
                text = when {
                    selected.isNotEmpty() -> selected
                    empty -> tr("немає", "none")
                    else -> "›"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    )
}
